package com.gtocore.common.quest.hot;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestObject;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * 任务章节源码的语法模型：用 javac 语法树把每个任务、复制节点定位到源码里的精确位置，供热修改按位置改写；
 * 并核对源码与构建出的任务书逐个对得上（坐标、复制目标），对不上的写法即视为热修改系统不支持。
 */
public final class QuestSource {

    static final String CHAPTER_DIR = "com/gtocore/data/quest/chapter";

    @Nullable
    private static JavaCompiler compiler;
    @Nullable
    private static StandardJavaFileManager fileManager;
    private static final O2OOpenCacheHashMap<Path, FileModel> CACHE = new O2OOpenCacheHashMap<>();

    private final Path sourceRoot;
    final ObjectArrayList<FileModel> files = new ObjectArrayList<>();
    final O2OOpenCacheHashMap<String, FileModel> byClass = new O2OOpenCacheHashMap<>();
    final O2OOpenCacheHashMap<String, FileModel> byChapter = new O2OOpenCacheHashMap<>();
    final ObjectArrayList<String> parseErrors = new ObjectArrayList<>();

    private QuestSource(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    static synchronized QuestSource load(Path sourceRoot) {
        var source = new QuestSource(sourceRoot);
        var dir = sourceRoot.resolve(CHAPTER_DIR);
        List<Path> paths;
        try (Stream<Path> list = Files.list(dir)) {
            paths = list.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        } catch (IOException e) {
            source.parseErrors.add("无法列出章节源码目录 " + dir + "：" + e);
            return source;
        }
        var stale = new ObjectArrayList<Path>();
        var contents = new ObjectArrayList<String>();
        var stamps = new ObjectArrayList<long[]>();
        for (var path : paths) {
            long[] stamp;
            try {
                var attrs = Files.readAttributes(path, BasicFileAttributes.class);
                stamp = new long[] { attrs.lastModifiedTime().toMillis(), attrs.size() };
            } catch (IOException e) {
                source.parseErrors.add(path.getFileName() + "：读取失败 " + e);
                continue;
            }
            var cached = CACHE.get(path);
            if (cached != null && cached.modified == stamp[0] && cached.size == stamp[1]) {
                source.add(cached);
                continue;
            }
            String content;
            try {
                content = Files.readString(path, StandardCharsets.UTF_8);
            } catch (IOException e) {
                source.parseErrors.add(path.getFileName() + "：读取失败 " + e);
                continue;
            }
            stale.add(path);
            contents.add(content);
            stamps.add(stamp);
        }
        if (!stale.isEmpty()) {
            var models = parse(stale, contents, source.parseErrors);
            for (int i = 0; i < models.size(); i++) {
                var model = models.get(i);
                var stamp = stamps.get(stale.indexOf(model.path));
                model.modified = stamp[0];
                model.size = stamp[1];
                CACHE.put(model.path, model);
                source.add(model);
            }
        }
        source.files.sort((a, b) -> a.path.compareTo(b.path));
        return source;
    }

    QuestSource copy() {
        var copy = new QuestSource(sourceRoot);
        for (var file : files) copy.add(file);
        copy.parseErrors.addAll(parseErrors);
        return copy;
    }

    @Nullable
    String update(FileModel old, String content) {
        var errors = new ObjectArrayList<String>();
        var parsed = parse(List.of(old.path), List.of(content), errors);
        if (!errors.isEmpty() || parsed.isEmpty()) return errors.isEmpty() ? old.name() + "：重新解析失败" : errors.get(0);
        var model = parsed.get(0);
        files.set(files.indexOf(old), model);
        byClass.put(model.className, model);
        if (old.chapterId != null) byChapter.remove(old.chapterId);
        if (model.chapterId != null) byChapter.put(model.chapterId, model);
        return null;
    }

    @Nullable
    Decl find(String identity) {
        if (identity.length() < 3 || identity.charAt(1) != ':') return null;
        var body = identity.substring(2);
        switch (identity.charAt(0)) {
            case 'q' -> {
                int slash = body.indexOf('/');
                if (slash < 0) return null;
                var file = byChapter.get(body.substring(0, slash));
                return file == null ? null : file.questsById.get(body.substring(slash + 1));
            }
            case 'c' -> {
                int colon = body.indexOf(':');
                if (colon < 0) return null;
                var file = byChapter.get(body.substring(0, colon));
                return file != null && file.fields.get(body.substring(colon + 1)) instanceof LinkDecl link ? link : null;
            }
            case 'd' -> {
                var parts = body.split(":", 3);
                if (parts.length != 3) return null;
                var file = byChapter.get(parts[0]);
                if (file == null) return null;
                int ordinal;
                try {
                    ordinal = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    return null;
                }
                for (var link : file.links) {
                    if (link.field == null && resolve(file, link.targetRef) instanceof QuestDecl target && target.key().equals(parts[1]) && ordinal-- == 0) return link;
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    @Nullable
    String identityOf(Decl decl) {
        if (decl instanceof QuestDecl quest) return "q:" + quest.key();
        if (!(decl instanceof LinkDecl link)) return null;
        if (link.field != null) return "c:" + link.file.chapterId + ":" + link.field;
        if (!(resolve(link.file, link.targetRef) instanceof QuestDecl target)) return null;
        int ordinal = 0;
        for (var other : link.file.links) {
            if (other == link) return "d:" + link.file.chapterId + ":" + target.key() + ":" + ordinal;
            if (other.field == null && resolve(link.file, other.targetRef) instanceof QuestDecl t && t.key().equals(target.key())) ordinal++;
        }
        return null;
    }

    static synchronized void forget(Path path) {
        CACHE.remove(path);
    }

    private void add(FileModel model) {
        files.add(model);
        byClass.put(model.className, model);
        if (model.chapterId != null) byChapter.put(model.chapterId, model);
    }

    static boolean compilerAvailable() {
        return ToolProvider.getSystemJavaCompiler() != null;
    }

    private static ObjectArrayList<FileModel> parse(List<Path> paths, List<String> contents, ObjectArrayList<String> errors) {
        var out = new ObjectArrayList<FileModel>(paths.size());
        if (compiler == null) {
            compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                errors.add("当前运行环境没有 Java 编译器（需要用 JDK 启动），无法解析任务源码");
                return out;
            }
            fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        }
        var units = new ObjectArrayList<Source>(paths.size());
        var byUri = new O2OOpenCacheHashMap<URI, Source>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            var source = new Source(paths.get(i), contents.get(i));
            units.add(source);
            byUri.put(source.toUri(), source);
        }
        var task = (JavacTask) compiler.getTask(null, fileManager, d -> {
            if (d.getKind() == javax.tools.Diagnostic.Kind.ERROR) errors.add(d.getSource() + ":" + d.getLineNumber() + " " + d.getMessage(null));
        }, List.of("-proc:none"), null, units);
        Iterable<? extends CompilationUnitTree> parsed;
        try {
            parsed = task.parse();
        } catch (IOException e) {
            errors.add("解析失败：" + e);
            return out;
        }
        var positions = Trees.instance(task).getSourcePositions();
        for (var unit : parsed) {
            var src = byUri.get(unit.getSourceFile().toUri());
            if (src != null) out.add(new FileModel(src.path, src.content, unit, positions));
        }
        return out;
    }

    @Nullable
    FileModel fileOf(QuestChapter chapter) {
        return byChapter.get(chapter.id());
    }

    @Nullable
    QuestDecl declOf(Quest quest) {
        var file = byChapter.get(quest.chapter().id());
        return file == null ? null : file.questsById.get(quest.id());
    }

    @Nullable
    LinkDecl declOf(QuestLink link) {
        var file = byChapter.get(link.chapter().id());
        if (file == null) return null;
        for (int i = 0; i < file.links.size(); i++) {
            var decl = file.links.get(i);
            if (decl.x == link.x() && decl.y == link.y() && resolve(file, decl.targetRef) instanceof QuestDecl target &&
                    target.key().equals(link.target().key())) {
                return decl;
            }
        }
        return null;
    }

    @Nullable
    Decl resolve(FileModel from, @Nullable ExpressionTree ref) {
        if (ref instanceof IdentifierTree id) return from.fields.get(id.getName().toString());
        if (ref instanceof MemberSelectTree select && select.getExpression() instanceof IdentifierTree owner) {
            var file = byClass.get(owner.getName().toString());
            return file == null ? null : file.fields.get(select.getIdentifier().toString());
        }
        return null;
    }

    @Nullable
    static QuestObject objectOf(Decl decl) {
        if (decl instanceof QuestDecl quest) return QuestBook.byKey(quest.key());
        if (decl instanceof ChapterDecl chapter) return QuestBook.byKey(chapter.file.chapterId);
        return null;
    }

    String refText(FileModel from, Decl target) {
        return target.file == from ? target.field : target.file.className + "." + target.field;
    }

    String refText(FileModel from, Decl target, int usePos) {
        if (target.file == from && target.var != null && from.start(target.var) > usePos) return target.file.className + "." + target.field;
        return refText(from, target);
    }

    record Ref(FileModel file, int pos) {}

    List<Ref> referencesTo(Decl target) {
        var out = new ObjectArrayList<Ref>();
        for (var file : files) {
            new TreeScanner<Void, Void>() {

                @Override
                public Void visitIdentifier(IdentifierTree node, Void unused) {
                    if (file == target.file && node.getName().contentEquals(target.field)) note(node);
                    return null;
                }

                @Override
                public Void visitMemberSelect(MemberSelectTree node, Void unused) {
                    if (node.getExpression() instanceof IdentifierTree owner && owner.getName().contentEquals(target.file.className) &&
                            node.getIdentifier().contentEquals(target.field)) {
                        note(node);
                        return null;
                    }
                    return super.visitMemberSelect(node, unused);
                }

                @Override
                public Void visitVariable(VariableTree node, Void unused) {
                    return scan(node.getInitializer(), null);
                }

                private void note(Tree node) {
                    out.add(new Ref(file, file.start(node)));
                }
            }.scan(file.unit, null);
        }
        return out;
    }

    List<String> validate() {
        var errors = new ObjectArrayList<String>(parseErrors);
        if (!errors.isEmpty()) return errors;
        if (!QuestBook.isReady()) {
            errors.add("任务书尚未构建");
            return errors;
        }
        for (var file : files) errors.addAll(file.problems);
        var chapters = QuestBook.chapters();
        for (int c = 0; c < chapters.size(); c++) {
            var chapter = chapters.get(c);
            var file = fileOf(chapter);
            if (file == null) {
                errors.add("章节 " + chapter.id() + "：在 " + CHAPTER_DIR + " 里找不到 QuestChapter.of(\"" + chapter.id() + "\", …) 的声明");
                continue;
            }
            var quests = chapter.quests();
            for (int i = 0; i < quests.size(); i++) {
                var quest = quests.get(i);
                var decl = file.questsById.get(quest.id());
                if (decl == null) {
                    errors.add(file.name() + "：任务 " + quest.id() + " 不是 public static final Quest X = " + file.chapterField + ".quest(\"" + quest.id() + "\", q -> q …) 的写法");
                    continue;
                }
                var at = decl.calls("at");
                if (at.size() != 1 || at.get(0).getArguments().size() != 2) {
                    errors.add(decl.where() + "：需要恰好一个 .at(x, y)");
                    continue;
                }
                var args = at.get(0).getArguments();
                Double x = number(args.get(0)), y = number(args.get(1));
                if (x == null || y == null) errors.add(decl.where() + "：.at 的坐标必须直接写数字");
                else if (x != quest.x() || y != quest.y()) errors.add(decl.where() + "：源码坐标 (" + x + ", " + y + ") 与任务书 (" + quest.x() + ", " + quest.y() + ") 不一致");
                var textProblem = decl.textProblem();
                if (textProblem != null) errors.add(decl.where() + "：" + textProblem);
                for (var deps : decl.calls("dependsOn")) {
                    for (var arg : deps.getArguments()) {
                        var target = resolve(file, arg);
                        if (target == null) errors.add(decl.where() + "：依赖 " + file.text(arg) + " 不是任务、复制节点或章节字段");
                    }
                }
            }
            var links = chapter.links();
            for (int i = 0; i < links.size(); i++) {
                var link = links.get(i);
                if (declOf(link) == null) {
                    errors.add(file.name() + "：复制节点 " + link.target().key() + " @ (" + link.x() + ", " + link.y() +
                            ") 找不到对应的 " + file.chapterField + ".copy(目标, x, y) 或 QuestLink.of(目标, x, y)（坐标须直接写数字）");
                }
            }
        }
        return errors;
    }

    @Nullable
    static Double number(ExpressionTree tree) {
        if (tree instanceof LiteralTree literal && literal.getValue() instanceof Number n) return n.doubleValue();
        if (tree instanceof UnaryTree unary && unary.getKind() == Tree.Kind.UNARY_MINUS && unary.getExpression() instanceof LiteralTree literal &&
                literal.getValue() instanceof Number n) {
            return -n.doubleValue();
        }
        return null;
    }

    private static final class Source extends SimpleJavaFileObject {

        final Path path;
        final String content;

        Source(Path path, String content) {
            super(URI.create("string:///" + path.getFileName()), Kind.SOURCE);
            this.path = path;
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }

        @Override
        public String toString() {
            return path.getFileName().toString();
        }
    }

    static final class FileModel {

        final Path path;
        final String content;
        final CompilationUnitTree unit;
        final SourcePositions positions;
        final String className;
        @Nullable
        String chapterId;
        @Nullable
        String chapterField;
        @Nullable
        ChapterDecl chapter;
        final O2OOpenCacheHashMap<String, Decl> fields = new O2OOpenCacheHashMap<>();
        final O2OOpenCacheHashMap<String, QuestDecl> questsById = new O2OOpenCacheHashMap<>();
        final ObjectArrayList<QuestDecl> quests = new ObjectArrayList<>();
        final ObjectArrayList<LinkDecl> links = new ObjectArrayList<>();
        @Nullable
        MethodInvocationTree mainLine;
        final ObjectArrayList<String> imports = new ObjectArrayList<>();
        final ObjectArrayList<String> staticImports = new ObjectArrayList<>();
        final ObjectArrayList<String> problems = new ObjectArrayList<>();
        int importsEnd = -1;
        int membersEnd = -1;
        long modified = -1;
        long size = -1;

        FileModel(Path path, String content, CompilationUnitTree unit, SourcePositions positions) {
            this.path = path;
            this.content = content;
            this.unit = unit;
            this.positions = positions;
            var name = path.getFileName().toString();
            this.className = name.substring(0, name.length() - ".java".length());
            for (ImportTree imp : unit.getImports()) {
                if (!imp.isStatic()) imports.add(imp.getQualifiedIdentifier().toString());
                else staticImports.add(imp.getQualifiedIdentifier().toString());
                importsEnd = Math.max(importsEnd, end(imp));
            }
            for (var type : unit.getTypeDecls()) {
                if (!(type instanceof com.sun.source.tree.ClassTree cls)) continue;
                for (var member : cls.getMembers()) {
                    if (member instanceof VariableTree var) {
                        index(var);
                        membersEnd = Math.max(membersEnd, end(var));
                    }
                }
                for (var member : cls.getMembers()) {
                    if (!(member instanceof VariableTree)) scanLinks(member);
                }
            }
        }

        private void index(VariableTree var) {
            var field = var.getName().toString();
            var init = var.getInitializer();
            if (!(init instanceof MethodInvocationTree call) || !(call.getMethodSelect() instanceof MemberSelectTree select)) {
                scanLinks(var);
                return;
            }
            var method = select.getIdentifier().toString();
            var args = call.getArguments();
            if (method.equals("of") && select.getExpression() instanceof IdentifierTree owner && owner.getName().contentEquals("QuestChapter") &&
                    !args.isEmpty() && args.get(0) instanceof LiteralTree id && id.getValue() instanceof String chapterIdValue) {
                chapterId = chapterIdValue;
                chapterField = field;
                chapter = new ChapterDecl(this, field, var);
                fields.put(field, chapter);
                return;
            }
            if (method.equals("quest") && select.getExpression() instanceof IdentifierTree owner && chapterField != null && owner.getName().contentEquals(chapterField)) {
                if (args.size() != 2 || !(args.get(0) instanceof LiteralTree id) || !(id.getValue() instanceof String questId) ||
                        !(args.get(1) instanceof LambdaExpressionTree lambda) || !(lambda.getBody() instanceof ExpressionTree body)) {
                    problems.add(name() + ":" + line(start(var)) + "：任务 " + field + " 必须写成 " + chapterField + ".quest(\"id\", q -> q …)");
                    return;
                }
                var decl = new QuestDecl(this, field, var, questId, lambda, body);
                if (!decl.chainValid) problems.add(decl.where() + "：链式调用必须从 lambda 参数 " + decl.param + " 开始逐个 .方法(…)");
                fields.put(field, decl);
                quests.add(decl);
                questsById.put(questId, decl);
                return;
            }
            if (method.equals("copy") && select.getExpression() instanceof IdentifierTree owner && chapterField != null && owner.getName().contentEquals(chapterField)) {
                var decl = LinkDecl.of(this, field, var, call);
                if (decl == null) {
                    problems.add(name() + ":" + line(start(var)) + "：复制节点 " + field + " 必须写成 " + chapterField + ".copy(目标, x, y)，坐标直接写数字");
                    return;
                }
                fields.put(field, decl);
                links.add(decl);
                return;
            }
            scanLinks(var);
        }

        private void scanLinks(Tree tree) {
            new TreeScanner<Void, Void>() {

                @Override
                public Void visitMethodInvocation(MethodInvocationTree node, Void v) {
                    if (node.getMethodSelect() instanceof MemberSelectTree select && select.getIdentifier().contentEquals("of") &&
                            select.getExpression() instanceof IdentifierTree owner && owner.getName().contentEquals("QuestLink")) {
                        var decl = LinkDecl.of(FileModel.this, null, null, node);
                        if (decl != null) links.add(decl);
                        else problems.add(name() + ":" + line(start(node)) + "：QuestLink.of 的坐标必须直接写数字");
                    }
                    if (node.getMethodSelect() instanceof MemberSelectTree select && select.getIdentifier().contentEquals("mainLine") && !node.getArguments().isEmpty()) {
                        mainLine = node;
                    }
                    return super.visitMethodInvocation(node, v);
                }
            }.scan(tree, null);
        }

        String name() {
            return path.getFileName().toString();
        }

        int start(Tree tree) {
            return (int) positions.getStartPosition(unit, tree);
        }

        int end(Tree tree) {
            return (int) positions.getEndPosition(unit, tree);
        }

        String text(Tree tree) {
            return content.substring(start(tree), end(tree));
        }

        long line(int pos) {
            return unit.getLineMap().getLineNumber(pos);
        }

        String indentAt(int pos) {
            int lineStart = content.lastIndexOf('\n', pos - 1) + 1;
            int i = lineStart;
            while (i < content.length() && (content.charAt(i) == ' ' || content.charAt(i) == '\t')) i++;
            return content.substring(lineStart, i);
        }
    }

    abstract static sealed class Decl permits ChapterDecl, QuestDecl, LinkDecl {

        final FileModel file;
        @Nullable
        final String field;
        @Nullable
        final VariableTree var;

        Decl(FileModel file, @Nullable String field, @Nullable VariableTree var) {
            this.file = file;
            this.field = field;
            this.var = var;
        }

        String where() {
            return file.name() + ":" + file.line(var != null ? file.start(var) : 0);
        }
    }

    static final class ChapterDecl extends Decl {

        ChapterDecl(FileModel file, String field, VariableTree var) {
            super(file, field, var);
        }
    }

    static final class QuestDecl extends Decl {

        final String questId;
        final LambdaExpressionTree lambda;
        final ExpressionTree body;
        final String param;
        final ObjectArrayList<MethodInvocationTree> chain = new ObjectArrayList<>();
        final boolean chainValid;

        QuestDecl(FileModel file, String field, VariableTree var, String questId, LambdaExpressionTree lambda, ExpressionTree body) {
            super(file, field, var);
            this.questId = questId;
            this.lambda = lambda;
            this.body = body;
            this.param = lambda.getParameters().isEmpty() ? "" : lambda.getParameters().get(0).getName().toString();
            ExpressionTree e = body;
            while (e instanceof MethodInvocationTree call && call.getMethodSelect() instanceof MemberSelectTree select) {
                chain.add(call);
                e = select.getExpression();
            }
            java.util.Collections.reverse(chain);
            chainValid = e instanceof IdentifierTree id && id.getName().contentEquals(param) && !chain.isEmpty();
        }

        String key() {
            return file.chapterId + "/" + questId;
        }

        @Override
        String where() {
            return file.name() + ":" + file.line(file.start(var)) + "（" + questId + "）";
        }

        @Nullable
        DescChain desc() {
            var calls = calls("desc");
            if (calls.isEmpty()) return null;
            var call = calls.get(0);
            if (call.getArguments().size() != 1 || !(call.getArguments().get(0) instanceof LambdaExpressionTree lambda) ||
                    !(lambda.getBody() instanceof ExpressionTree body) || lambda.getParameters().size() != 1) {
                return new DescChain(call, "", new ObjectArrayList<>(), false);
            }
            var param = lambda.getParameters().get(0).getName().toString();
            var chain = new ObjectArrayList<MethodInvocationTree>();
            ExpressionTree e = body;
            while (e instanceof MethodInvocationTree inner && inner.getMethodSelect() instanceof MemberSelectTree select) {
                chain.add(inner);
                e = select.getExpression();
            }
            java.util.Collections.reverse(chain);
            boolean valid = e instanceof IdentifierTree id && id.getName().contentEquals(param);
            return new DescChain(call, param, chain, valid);
        }

        int nameStart(MethodInvocationTree call) {
            return file.end(call.getMethodSelect()) - name(call).length();
        }

        @Nullable
        String textProblem() {
            for (var part : new String[] { "title", "subtitle" }) {
                for (var call : calls(part)) {
                    if (call.getArguments().isEmpty() || !(call.getArguments().get(0) instanceof LiteralTree literal) || !(literal.getValue() instanceof String)) {
                        return "." + part + "(…) 的第一个参数必须直接写字符串";
                    }
                }
            }
            if (calls("desc").size() > 1) return "只能有一个 .desc(…)";
            var desc = desc();
            if (desc == null) return null;
            if (!desc.valid) return ".desc(…) 必须写成 d -> d.line(…).line().page()… 的链式写法";
            for (var call : desc.chain) {
                if (name(call).equals("line") && !call.getArguments().isEmpty() &&
                        !(call.getArguments().get(0) instanceof LiteralTree literal && literal.getValue() instanceof String)) {
                    return ".desc 里 .line(…) 的第一个参数必须直接写字符串";
                }
            }
            return null;
        }

        ObjectArrayList<MethodInvocationTree> calls(String name) {
            var out = new ObjectArrayList<MethodInvocationTree>(1);
            for (int i = 0; i < chain.size(); i++) {
                if (name(chain.get(i)).equals(name)) out.add(chain.get(i));
            }
            return out;
        }

        static String name(MethodInvocationTree call) {
            return ((MemberSelectTree) call.getMethodSelect()).getIdentifier().toString();
        }

        int receiverEnd(MethodInvocationTree call) {
            return file.end(((MemberSelectTree) call.getMethodSelect()).getExpression());
        }

        int chainEnd() {
            return file.end(chain.get(chain.size() - 1));
        }

        String callIndent() {
            for (int i = 0; i < chain.size(); i++) {
                var call = chain.get(i);
                int dot = file.content.lastIndexOf('.', file.end(call.getMethodSelect()) - name(call).length());
                int lineStart = file.content.lastIndexOf('\n', dot) + 1;
                if (file.content.substring(lineStart, dot).isBlank()) return file.content.substring(lineStart, dot);
            }
            return file.indentAt(file.start(var)) + "        ";
        }
    }

    record DescChain(MethodInvocationTree call, String param, ObjectArrayList<MethodInvocationTree> chain, boolean valid) {}

    static final class LinkDecl extends Decl {

        final MethodInvocationTree call;
        final ExpressionTree targetRef;
        final double x;
        final double y;
        final boolean copy;

        private LinkDecl(FileModel file, @Nullable String field, @Nullable VariableTree var, MethodInvocationTree call, ExpressionTree targetRef, double x, double y) {
            super(file, field, var);
            this.call = call;
            this.targetRef = targetRef;
            this.x = x;
            this.y = y;
            this.copy = var != null;
        }

        @Nullable
        static LinkDecl of(FileModel file, @Nullable String field, @Nullable VariableTree var, MethodInvocationTree call) {
            var args = call.getArguments();
            if (args.size() != 3) return null;
            Double x = number(args.get(1)), y = number(args.get(2));
            if (x == null || y == null) return null;
            return new LinkDecl(file, field, var, call, args.get(0), x, y);
        }

        @Override
        String where() {
            return file.name() + ":" + file.line(file.start(call));
        }
    }
}
