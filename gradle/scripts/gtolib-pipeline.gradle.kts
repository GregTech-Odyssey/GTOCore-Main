/**
 * GTOLib 流水线：源码 → dev 轨 build/gtolib/gtolib-protected.jar（named hollow，本机运行与编译）+ 发行轨 build/gtolib/gtolib-release.jar（类名/成员名改名，嵌入玩家 jar）。
 * 两条轨道用各自规则：dev 轨 proguard-rules.pro（保留类名/包名），发行轨 proguard-release-rules.pro（改名）；gtocore 对 com.gtolib 的引用在打包时按发行 mapping remap。
 * 明文开关 -PgtolibUnprotected=true | -PgtolibDebug=true | -PskipGtolibProtect=true（或 env GTOLIB_DEBUG / PROGUARD_DISABLE / .gtolib-debug）；强制重建 -PgtolibRebuild=true。
 */

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.ow2.asm:asm-tree:9.7.1")
        // ClassRemapper/Remapper：打包时按发行 mapping 改写 gtocore 对 com.gtolib 的引用
        classpath("org.ow2.asm:asm-commons:9.6")
    }
}

val sourceSets = the<SourceSetContainer>()
val mainSourceSet = sourceSets["main"]
val forgeVersion = property("forge_version").toString()
val modId = property("mod_id").toString()
val gtoSealMinVersion = findProperty("gto_seal_min")?.toString() ?: "0"

val gtolibOut: File = layout.buildDirectory.dir("gtolib").get().asFile
fun out(name: String) = gtolibOut.resolve(name)
val unprotectedJar = out("gtolib-unprotected.jar")
val srgJar = out("gtolib-srg.jar")
val proguardDevJar = out("gtolib-proguard-dev.jar")
val proguardReleaseJar = out("gtolib-proguard-release.jar")
val protectedJar = out("gtolib-protected.jar")
val releaseJar = out("gtolib-release.jar")
val modJar = out("gtolib-mod.jar")
val stampFile = out("inputs.stamp")
val releaseMappingFile = out("gtolib-release-mapping.txt")
val forgeSrgLibraryJar = out("forge-1.20.1-$forgeVersion-srg.jar")
val gtocoreNamedLibraryJar = out("gtocore-named-library.jar")
val gtocoreSrgLibraryJar = out("gtocore-srg-library.jar")
val namedForgeJar = layout.buildDirectory.file("moddev/artifacts/forge-1.20.1-$forgeVersion.jar").get().asFile
val namedToIntermediate = layout.buildDirectory.file("moddev/artifacts/namedToIntermediate.tsrg").get().asFile
val mixinOut: File = layout.buildDirectory.dir("mixin").get().asFile
val classesJava: File = layout.buildDirectory.dir("classes/java/main").get().asFile
val classesKotlin: File = layout.buildDirectory.dir("classes/kotlin/main").get().asFile
val resourcesMain: File = layout.buildDirectory.dir("resources/main").get().asFile

val protectDir = file("GTOLib/protect")
val sealPackagerJar = protectDir.resolve("gto-seal-packager.jar")
val sealRuntimeLibsJar = file("libs/gto-seal-runtime-1.0.jar")
val gtolibModStubClass = file("gradle/scripts/gtolib-mod-stub/GTOLibMod.class")
val gtolibMixinConfig = file("GTOLib/src/main/resources/gtolib.mixins.json")
val hasGtosealSources = file("GTOSeal/src/main/java/gto/native0").exists()
val runDirs = listOf("client", "data", "server", "gametest").map { file("run/$it/mods") }

if (!file("GTOLib/src/main/java/com/gtolib").exists()) {
    throw GradleException("缺少 GTOLib 源码（GTOLib/ submodule）。构建 gtocore 需要 GTOLib 权限:\n  git submodule update --init GTOLib")
}

val isGtolibDebugMode = listOf("gtolibUnprotected", "gtolibDebug", "skipGtolibProtect").any { findProperty(it) == "true" } ||
    System.getenv("GTOLIB_DEBUG") == "true" || System.getenv("PROGUARD_DISABLE") == "true" || file(".gtolib-debug").exists()
extra["isGtolibDebugMode"] = isGtolibDebugMode

if (!isGtolibDebugMode) {
    val missing = listOf("proguard-rules.pro", "proguard-release-rules.pro", "my_dict.txt", "gto-seal-packager.jar")
        .filterNot { protectDir.resolve(it).exists() }
    if (missing.isNotEmpty()) {
        throw GradleException("GTOLib 保护工具链缺失: ${missing.joinToString()}\n确认 GTOLib 子模块完整: git submodule update --init GTOLib")
    }
}

// ---------- 缓存：源码指纹 + inputs.stamp ----------

fun sha256(f: File): String {
    if (!f.isFile) return ""
    val md = MessageDigest.getInstance("SHA-256")
    f.inputStream().use { input ->
        val buf = ByteArray(64 * 1024)
        while (true) {
            val n = input.read(buf)
            if (n <= 0) break
            md.update(buf, 0, n)
        }
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}

fun computeGtolibFingerprint(): String {
    val md = MessageDigest.getInstance("SHA-256")
    listOf(file("GTOLib/src"), protectDir).filter { it.exists() }
        .flatMap { root -> root.walkTopDown().filter { it.isFile }.toList() }
        .filterNot { it.name.endsWith(".jks") || it.name.endsWith(".cert") || it.name.endsWith(".jar") && it.name.contains("seal") }
        .sortedBy { it.absolutePath }
        .forEach { f ->
            md.update(f.relativeTo(rootDir).invariantSeparatorsPath.toByteArray())
            md.update(0)
            md.update(f.length().toString().toByteArray())
            md.update(0)
            md.update(f.readBytes())
            md.update(0)
        }
    return md.digest().joinToString("") { "%02x".format(it) }
}

val gtolibFingerprint = computeGtolibFingerprint()

fun readStamp(): Map<String, String> =
    if (!stampFile.isFile) emptyMap()
    else stampFile.readLines().filter { it.indexOf('=') > 0 }.associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }

val isGtolibJarUpToDate: Boolean = run {
    if (isGtolibDebugMode || findProperty("gtolibRebuild") == "true") return@run false
    val stamp = readStamp()
    val reason = when {
        gradle.startParameter.taskNames.any { it == "clean" || it.endsWith(":clean") } -> "本次调用含 clean"
        stamp["fingerprint"] != gtolibFingerprint -> "stamp 源码指纹缺失或过期"
        sha256(protectedJar) != stamp["jarSha256"] -> "build/gtolib/gtolib-protected.jar 缺失或与 stamp 不一致"
        sha256(releaseJar) != stamp["releaseSha256"] -> "build/gtolib/gtolib-release.jar 缺失或与 stamp 不一致"
        !proguardReleaseJar.isFile || !forgeSrgLibraryJar.isFile -> "gtocore 代码保护所需的发行轨中间产物缺失"
        else -> null
    }
    if (reason != null) logger.lifecycle("GTOLib: $reason -> 重建保护产物")
    reason == null
}
val rebuildGtolib = !isGtolibDebugMode && !isGtolibJarUpToDate
// 只有明文调试或需要重建时，才把私有源码交给 main 编译
val compileGtolibSources = isGtolibDebugMode || rebuildGtolib

logger.lifecycle(
    "GTOLib pipeline: jarUpToDate=$isGtolibJarUpToDate, compileSources=$compileGtolibSources, " +
        "debugMode=$isGtolibDebugMode, fingerprint=${gtolibFingerprint.take(12)}…, GTOSeal=$hasGtosealSources"
)

if (compileGtolibSources) {
    mainSourceSet.java.srcDir("GTOLib/src/main/java")
    mainSourceSet.resources.srcDir("GTOLib/src/main/resources")
    // 安装时会从 main 输出删掉 com/gtolib；Kotlin 增量缓存感知不到外部删除，缺类时强制重编
    tasks.named("compileKotlin") { outputs.upToDateWhen { classesKotlin.resolve("com/gtolib").exists() } }
    tasks.named("compileJava") { outputs.upToDateWhen { classesJava.resolve("com/gtolib").list()?.isNotEmpty() == true } }
    logger.lifecycle(if (isGtolibDebugMode) "GTOLib: 显式明文调试 → 编译源码并保留 Java/Kotlin 输出，跳过 ProGuard/Seal" else "GTOLib: 源码有变更 → 将重新编译并走保护流水线")
} else {
    logger.lifecycle("GTOLib: 指纹命中 → 编译复用 build/gtolib/gtolib-protected.jar")
}

tasks.named<ProcessResources>("processResources") {
    from(gtolibMixinConfig.parentFile) {
        include(gtolibMixinConfig.name)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

// 明文调试时 gtolib 直接在 main classes 里，Runtime 若在 classpath 上会把明文类当成待解密类而抛 "GTOLIB init failed"
if (isGtolibDebugMode) {
    listOf("runtimeClasspath", "clientRuntimeClasspath", "dataRuntimeClasspath", "serverRuntimeClasspath", "gametestRuntimeClasspath").forEach {
        configurations.named(it) { exclude(group = "dev", module = "gto-seal-runtime") }
    }
}

val proguardCp: Configuration = configurations.create("proguardCp")
val artTool: Configuration = configurations.findByName("artTool") ?: configurations.create("artTool")
dependencies {
    add("proguardCp", "com.guardsquare:proguard-base:7.8.0")
    if (artTool.dependencies.isEmpty()) add("artTool", "net.neoforged:AutoRenamingTool:2.0.17:all")
    if (!compileGtolibSources) add("implementation", files(protectedJar).builtBy("buildGtolibProtected"))
    // 加密模式下 main 输出里的 com/gtolib 会在安装时删掉，测试编译改用 dev 轨 named hollow（测试只在明文模式运行）
    if (!isGtolibDebugMode) add("testCompileOnly", files(protectedJar).builtBy("buildGtolibProtected"))
}

// ---------- jar 工具 ----------

/** 逐条改写 jar：transform 返回 null 删除该条目；extra 里的条目替换同名原条目并追加到末尾。 */
fun rewriteJar(src: File, dest: File = src, extra: Map<String, ByteArray> = emptyMap(), transform: (String, ByteArray) -> ByteArray? = { _, b -> b }) {
    val tmp = File(dest.path + ".tmp")
    ZipInputStream(src.inputStream().buffered()).use { zis ->
        ZipOutputStream(tmp.outputStream().buffered()).use { zos ->
            fun put(name: String, bytes: ByteArray) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(bytes)
                zos.closeEntry()
            }
            generateSequence { zis.nextEntry }.forEach { e ->
                if (e.name !in extra) transform(e.name, zis.readAllBytes())?.let { put(e.name, it) }
            }
            extra.forEach(::put)
        }
    }
    Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

fun jarEntryNames(jar: File): Set<String> = ZipFile(jar).use { zf -> zf.entries().asSequence().map { it.name }.toSet() }

fun readJarEntry(jar: File, name: String): ByteArray? = ZipFile(jar).use { zf -> zf.getEntry(name)?.let { zf.getInputStream(it).readBytes() } }

fun latin1(bytes: ByteArray) = String(bytes, Charsets.ISO_8859_1)

fun requireJar(f: File, label: String, minSize: Long = 1) {
    if (!f.isFile || f.length() < minSize) throw GradleException("$label 无效: $f")
}

fun BaseExecSpec.captureToolOutput(): java.io.ByteArrayOutputStream {
    val buf = java.io.ByteArrayOutputStream()
    standardOutput = buf
    errorOutput = buf
    isIgnoreExitValue = true
    return buf
}

fun finishToolOutput(name: String, exitValue: Int, buf: java.io.ByteArrayOutputStream) {
    val log = out("logs/$name.log")
    log.parentFile.mkdirs()
    log.writeBytes(buf.toByteArray())
    if (exitValue != 0) {
        val tail = log.readLines().takeLast(40).joinToString("\n")
        throw GradleException("$name 失败（exit $exitValue），完整输出见 $log\n$tail")
    }
    logger.lifecycle("$name 工具输出已写入 ${log.relativeTo(projectDir)}")
}

val SEAL_BIN_DIR = "native0/native/"

/** Packager 读取 Manifest 里的 Salt：prod/dev 两份输入必须盖同一个 Salt。 */
fun stampSealManifest(src: File, dest: File, salt: String) {
    val keys = listOf("GTO-Seal", "GTO-Seal-Salt", "GTO-Seal-Version", "NTPT-Salt", "NTPT-Version")
    val sealLines = listOf("GTO-Seal: 1", "GTO-Seal-Salt: $salt", "GTO-Seal-Version: $gtoSealMinVersion", "NTPT-Salt: $salt", "NTPT-Version: $gtoSealMinVersion")
    fun manifest(old: String?): ByteArray {
        val kept = old.orEmpty().lines().filter { l -> keys.none { l.trim().startsWith("$it:") } }.dropLastWhile { it.isBlank() }
        val head = if (kept.firstOrNull()?.startsWith("Manifest-Version") == true) kept else listOf("Manifest-Version: 1.0") + kept
        return (head + sealLines + "").joinToString("\n").toByteArray()
    }
    val hasManifest = jarEntryNames(src).any { it.equals("META-INF/MANIFEST.MF", ignoreCase = true) }
    rewriteJar(src, dest, if (hasManifest) emptyMap() else mapOf("META-INF/MANIFEST.MF" to manifest(null))) { name, bytes ->
        if (name.equals("META-INF/MANIFEST.MF", ignoreCase = true)) manifest(String(bytes)) else bytes
    }
}

/** 每个加密体（.dev.bin / .prod.bin）都必须有同路径 hollow class，否则运行时 ClassNotFound。 */
fun verifySealShells(jar: File, label: String) {
    val names = jarEntryNames(jar)
    val missing = names.filter { it.startsWith(SEAL_BIN_DIR) && (it.endsWith(".dev.bin") || it.endsWith(".prod.bin")) }
        .map { it.removePrefix(SEAL_BIN_DIR).substringBeforeLast('.').substringBeforeLast('.') + ".class" }
        .filterNot { it in names }
    if (missing.isNotEmpty()) {
        throw GradleException("$label: ${missing.size} 个加密体缺少 hollow class:\n  ${missing.take(12).joinToString("\n  ")}")
    }
    logger.lifecycle(
        "$label: ${jar.name} (${jar.length()} bytes) class=${names.count { it.startsWith("com/gtolib/") && it.endsWith(".class") }} " +
            "dev.bin=${names.count { it.endsWith(".dev.bin") }} prod.bin=${names.count { it.endsWith(".prod.bin") }}"
    )
}

// ---------- 校验 ----------

/**
 * 明文 mixin 在 Seal Runtime 生效之前就被 Mixin 读取，必须按消费方选名字：dev 轨 named（userdev 官方名），发行轨 SRG。
 * 且必须出自 ProGuard 产物（会剥 LocalVariableTable）；出现 LVT 说明混进了未保护的明文字节。
 */
fun verifyMixinNamespace(jar: File, expectNamed: Boolean, label: String) {
    requireJar(jar, label)
    val srg = Regex("[mf]_\\d+_")
    var total = 0
    val srgSamples = mutableListOf<String>()
    var srgClasses = 0
    var lvtClasses = 0
    ZipFile(jar).use { zf ->
        zf.entries().asSequence().filter { it.name.startsWith("com/gtolib/mixin/") && it.name.endsWith(".class") }.forEach { e ->
            total++
            val text = latin1(zf.getInputStream(e).readBytes())
            srg.find(text)?.let { m ->
                srgClasses++
                if (srgSamples.size < 6) srgSamples += "${e.name} → ${m.value}"
            }
            if ("LocalVariableTable" in text) lvtClasses++
        }
    }
    if (total == 0) throw GradleException("$label: 没有明文 mixin 类 → $jar")
    if (lvtClasses > 0) throw GradleException("$label: 明文 mixin 含 LocalVariableTable（$lvtClasses/$total），字节不是 ProGuard 产物 → $jar")
    if ((srgClasses == 0) != expectNamed) {
        throw GradleException(
            "$label: mixin 命名空间错误，期望 ${if (expectNamed) "named" else "SRG"}，$srgClasses/$total 个类含 SRG 成员名" +
                (if (srgSamples.isEmpty()) "" else "（${srgSamples.joinToString()}）") + " → $jar"
        )
    }
}

val gtolibImportRegex = Regex("""^import\s+(static\s+)?(com\.gtolib\.[A-Za-z0-9_.*]+)\s*;?\s*$""")

fun gtocoreSourceImports(): Sequence<MatchResult> =
    listOf(file("src/main/java"), file("src/main/kotlin")).filter { it.exists() }.asSequence()
        .flatMap { it.walkTopDown().filter { f -> f.isFile && (f.name.endsWith(".java") || f.name.endsWith(".kt")) } }
        .flatMap { it.readLines().asSequence() }
        .mapNotNull { gtolibImportRegex.find(it.trim()) }

/**
 * 把 @KeepName 展开成具体类名的 -keepnames（类级保类名+包名；成员级保成员名+所属类名）。
 * 必须扫描该轨的 ProGuard 输入 jar：dev 轨是 named、发行轨是 SRG，成员名按各自命名空间取。
 * 不能在规则文件里写 `-keepnames class * { @KeepName <fields>; }`：ProGuard 7.8 实测会把
 * 「class * + 成员说明」当成命中全部类，改名类数直接变 0。
 */
fun writeKeepNameRules(inputJar: File, target: File): File {
    requireJar(inputJar, "writeKeepNameRules 输入")
    val keepNameDesc = "Lcom/gtolib/api/annotation/KeepName;"
    fun annotated(annotations: List<AnnotationNode>?) = annotations?.any { it.desc == keepNameDesc } == true
    val classLevel = sortedSetOf<String>()
    val memberLevel = sortedMapOf<String, MutableSet<String>>()
    ZipFile(inputJar).use { zf ->
        zf.entries().asSequence().filter { it.name.startsWith("com/gtolib/") && it.name.endsWith(".class") }.forEach { e ->
            val node = ClassNode(Opcodes.ASM9)
            ClassReader(zf.getInputStream(e).readBytes()).accept(node, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            val fqn = node.name.replace('/', '.')
            if (annotated(node.visibleAnnotations) || annotated(node.invisibleAnnotations)) classLevel += fqn
            val specs = linkedSetOf<String>()
            node.fields.forEach { f ->
                if (annotated(f.visibleAnnotations) || annotated(f.invisibleAnnotations)) specs += "*** ${f.name};"
            }
            node.methods.forEach { m ->
                if (m.name != "<init>" && m.name != "<clinit>" && (annotated(m.visibleAnnotations) || annotated(m.invisibleAnnotations))) {
                    specs += "*** ${m.name}(...);"
                }
            }
            if (specs.isNotEmpty()) memberLevel[fqn] = specs
        }
    }
    target.writeText(
        "# generated: ${inputJar.name} 里的 @KeepName（类级保类名+包名；成员级保成员名+所属类名）\n" +
            classLevel.joinToString("") { "-keepnames class $it\n" } +
            memberLevel.entries.joinToString("") { (cls, specs) -> "-keepnames class $cls {\n" + specs.joinToString("") { "    $it\n" } + "}\n" }
    )
    logger.lifecycle("GTOLib @KeepName: 类级 ${classLevel.size} 项、成员级 ${memberLevel.size} 项 → ${target.name} ← ${inputJar.name}")
    return target
}

/** gtocore 主源码 import 的 com.gtolib 类型必须出现在发行 mapping 中，否则打包时无法对齐引用。 */
fun verifyGtocoreImportsCoveredByMapping(mapping: GtolibReleaseMapping) {
    val classes = HashSet<String>()
    val packages = HashSet<String>()
    mapping.classNames.keys.forEach { internal ->
        classes += internal.replace('/', '.')
        var i = internal.lastIndexOf('/')
        while (i > 0) {
            packages += internal.substring(0, i).replace('/', '.')
            i = internal.lastIndexOf('/', i - 1)
        }
    }
    fun hasClass(fqn: String) = fqn in classes
    fun hasPackageOrClass(fqn: String) = hasClass(fqn) || fqn in packages
    val missing = linkedSetOf<String>()
    var scanned = 0
    gtocoreSourceImports().forEach { m ->
        scanned++
        val fqn = m.groupValues[2]
        val parts = fqn.split('.')
        val ok = when {
            fqn.endsWith(".*") -> hasPackageOrClass(fqn.removeSuffix(".*"))
            // static / Kotlin 成员 import：从右剥离直到命中 class
            (parts.size downTo 3).any { hasClass(parts.subList(0, it).joinToString(".")) } -> true
            // Kotlin 顶层函数：父段小写视为包
            parts.size >= 4 && !parts[parts.size - 2][0].isUpperCase() -> hasPackageOrClass(parts.dropLast(1).joinToString("."))
            else -> false
        }
        if (!ok) missing += (if (m.groupValues[1].isNotEmpty()) "static " else "") + fqn
    }
    if (missing.isNotEmpty()) {
        throw GradleException(
            "GTOLib 引用面校验失败：gtocore 主源码 import 了发行 mapping 中不存在的 com.gtolib 类型（mapping 过期，或该类型已被删除/改名却没同步）。\n" +
                "缺失 ${missing.size} 项：\n" + missing.take(30).joinToString("\n") { "  - $it" }
        )
    }
    logger.lifecycle("GTOLib 引用面校验 OK: 主源码 com.gtolib import $scanned 条均可在发行 mapping 中解析")
}

// ---------- refmap ----------

@Suppress("UNCHECKED_CAST")
fun Any?.asMutableMap(): MutableMap<String, Any?>? = this as? MutableMap<String, Any?>

val resourceLocationMixnKey = "com/gtolib/mixin/mc/ResourceLocationMixn"
val resourceLocationMixnRefmap = mapOf(
    "namespace" to "Lnet/minecraft/resources/ResourceLocation;f_135804_:Ljava/lang/String;",
    "path" to "Lnet/minecraft/resources/ResourceLocation;f_135805_:Ljava/lang/String;",
    "toString" to "Lnet/minecraft/resources/ResourceLocation;toString()Ljava/lang/String;",
    "assertValidNamespace" to "Lnet/minecraft/resources/ResourceLocation;m_245413_(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
    "assertValidPath" to "Lnet/minecraft/resources/ResourceLocation;m_245185_(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
    "withPath" to "Lnet/minecraft/resources/ResourceLocation;m_247449_(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
)
// tsrg 字段行不带描述符（与 Mixin AP 一致，带了 ART 不改名）；方法行：类 方法名 描述符 srg名
val resourceLocationMixnTsrgLines = listOf(
    "$resourceLocationMixnKey namespace f_135804_",
    "$resourceLocationMixnKey path f_135805_",
    "$resourceLocationMixnKey toString ()Ljava/lang/String; toString",
    "$resourceLocationMixnKey assertValidNamespace (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String; m_245413_",
    "$resourceLocationMixnKey assertValidPath (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String; m_245185_",
    "$resourceLocationMixnKey withPath (Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation; m_247449_",
)
// Mixin AP 不为带完整描述符的 @Inject selector 生成 refmap，生产环境必须显式补成 SRG
val levelMixinKey = "com/gtolib/mixin/mc/world/LevelMixin"
val levelMixinCriticalRefmap = mapOf(
    "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z" to
        "Lnet/minecraft/world/level/Level;m_6933_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
)

/**
 * 按类、按成员合并所有 refmap 候选写入 build/mixin/gtolib.refmap.json，优先级 build/resources/main < 预置 src/main/resources < 本次 AP 产物。
 * 不能整份二选一：未编译 GTOLib 源码时 AP 只含 gtocore 的 Mixin，而 AP 新增的映射又必须进最终 refmap。
 */
fun resolveGtolibRefmapFile(): File? {
    val apOut = mixinOut.resolve("gtolib.refmap.json")
    val candidates = listOf(resourcesMain.resolve("gtolib.refmap.json"), file("src/main/resources/gtolib.refmap.json"), apOut)
        .filter { it.isFile && it.length() > 64 }
    if (candidates.isEmpty()) return null
    fun mergeInto(dst: MutableMap<String, Any?>, src: Map<String, Any?>) = src.forEach { (cls, members) ->
        val m = members.asMutableMap() ?: return@forEach
        (dst.getOrPut(cls) { linkedMapOf<String, Any?>() }.asMutableMap() ?: linkedMapOf<String, Any?>().also { dst[cls] = it }).putAll(m)
    }
    val mappings = linkedMapOf<String, Any?>()
    val data = linkedMapOf<String, Any?>()
    var parsed = 0
    candidates.forEach { f ->
        val root = runCatching { JsonSlurper().parse(f) }.onFailure { logger.warn("GTOLib refmap: 跳过无法解析的 $f: ${it.message}") }.getOrNull().asMutableMap()
            ?: return@forEach
        parsed++
        root["mappings"].asMutableMap()?.let { mergeInto(mappings, it) }
        root["data"].asMutableMap()?.forEach { (env, envMappings) ->
            val src = envMappings.asMutableMap() ?: return@forEach
            mergeInto(data.getOrPut(env) { linkedMapOf<String, Any?>() }.asMutableMap()!!, src)
        }
    }
    // 全部候选都坏了就别写出空 refmap（重建时会回写进 src/main/resources）
    if (parsed == 0 || mappings.isEmpty()) return null
    apOut.parentFile.mkdirs()
    apOut.writeText(JsonOutput.toJson(linkedMapOf("mappings" to mappings, "data" to data)), Charsets.UTF_8)
    logger.lifecycle("GTOLib refmap: 已合并 ${candidates.size} 份候选（AP 优先），共 ${mappings.size} 个 Mixin 类")
    return apOut
}

fun patchCriticalGtolibRefmapEntries(refmapFile: File) {
    val root = JsonSlurper().parse(refmapFile).asMutableMap() ?: return
    val mappings = root["mappings"].asMutableMap() ?: linkedMapOf<String, Any?>().also { root["mappings"] = it }
    val dataRoot = root["data"].asMutableMap() ?: linkedMapOf<String, Any?>().also { root["data"] = it }
    val searge = dataRoot["searge"].asMutableMap() ?: linkedMapOf<String, Any?>().also { dataRoot["searge"] = it }
    val cur = mappings[resourceLocationMixnKey].asMutableMap()
    if (cur == null || "namespace" !in cur || "assertValidNamespace" !in cur || "getNamespace" in cur ||
        cur["namespace"]?.toString()?.contains("f_135804_") != true || cur["assertValidNamespace"]?.toString()?.startsWith("L") != true
    ) {
        mappings[resourceLocationMixnKey] = LinkedHashMap(resourceLocationMixnRefmap)
        searge[resourceLocationMixnKey] = LinkedHashMap(resourceLocationMixnRefmap)
        logger.lifecycle("GTOLib refmap: 已写入 $resourceLocationMixnKey 字段+Overwrite 全量映射")
    }
    var levelChanged = false
    listOf(mappings, searge).forEach { ns ->
        val entries = ns[levelMixinKey].asMutableMap() ?: linkedMapOf<String, Any?>().also { ns[levelMixinKey] = it }
        levelMixinCriticalRefmap.forEach { (selector, target) ->
            if (entries[selector] != target) {
                entries[selector] = target
                levelChanged = true
            }
        }
    }
    val json = JsonOutput.prettyPrint(JsonOutput.toJson(root)).lines().joinToString("\n") { line ->
        val indent = line.takeWhile { it == ' ' }.length
        " ".repeat(indent / 2) + line.substring(indent)
    } + "\n"
    if (refmapFile.readText(Charsets.UTF_8).replace("\r\n", "\n") != json) refmapFile.writeText(json, Charsets.UTF_8)
    if (levelChanged) logger.lifecycle("GTOLib refmap: 已补全 LevelMixin setBlock → m_6933_ 注入映射")
}

fun verifyCriticalGtolibRefmapEntries(refmapFile: File) {
    val root = JsonSlurper().parse(refmapFile).asMutableMap()
    mapOf("mappings" to root?.get("mappings"), "searge" to root?.get("data").asMutableMap()?.get("searge")).forEach { (ns, all) ->
        val entries = all.asMutableMap()?.get(levelMixinKey).asMutableMap()
        levelMixinCriticalRefmap.forEach { (selector, target) ->
            if (entries?.get(selector) != target) throw GradleException("GTOLib refmap: $ns.$levelMixinKey 缺少生产 SRG 映射 $selector → $target")
        }
    }
}

/** ART/reobfJar 使用的 tsrg 必须含 ResourceLocationMixn（未编译 GTOLib 源码时没有 AP 产物） */
fun ensureResourceLocationMixnTsrg(tsrgFile: File) {
    tsrgFile.parentFile.mkdirs()
    val body = (if (tsrgFile.exists()) tsrgFile.readLines() else emptyList()).filterNot { resourceLocationMixnKey in it }
    val header = body.takeWhile { it.startsWith("tsrg") || it.isBlank() }
    tsrgFile.writeText((header + resourceLocationMixnTsrgLines + body.drop(header.size)).joinToString("\n") + "\n")
}

/**
 * 合并、补丁并发布完整 refmap 到 build/mixin 与 build/resources/main；writeToRepoResources 时同时回写 src/main/resources，
 * 供下次未编译 GTOLib 源码时使用。返回要注入 jar 的两份 refmap 内容。
 */
fun publishGtolibRefmaps(writeToRepoResources: Boolean): Map<String, ByteArray>? {
    val src = resolveGtolibRefmapFile() ?: run {
        logger.warn("GTOLib refmap: 未找到有效 gtolib.refmap.json（AP 与 resources 均无）")
        return null
    }
    patchCriticalGtolibRefmapEntries(src)
    verifyCriticalGtolibRefmapEntries(src)
    val bytes = src.readBytes()
    // gtocore.mixins.json 声明 refmap=gtocore.refmap.json：与完整映射同步
    val targets = mutableListOf(mixinOut, resourcesMain)
    if (writeToRepoResources) targets += file("src/main/resources")
    targets.forEach { dir ->
        dir.mkdirs()
        listOf("gtolib.refmap.json", "gtocore.refmap.json").forEach { dir.resolve(it).writeBytes(bytes) }
    }
    if (writeToRepoResources) logger.lifecycle("GTOLib refmap: 已写入 src/main/resources ${bytes.size} bytes")
    return mapOf("gtolib.refmap.json" to bytes, "gtocore.refmap.json" to bytes)
}

/** 封装后的 jar 往往没有 refmap 与明文 mixin 配置：重新注入两份 refmap 与 gtolib.mixins.json */
fun injectMixinResources(jar: File, writeToRepoResources: Boolean) {
    val refmaps = publishGtolibRefmaps(writeToRepoResources) ?: throw GradleException("无法向 ${jar.name} 注入 gtolib.refmap.json")
    rewriteJar(jar, extra = refmaps + ("gtolib.mixins.json" to gtolibMixinConfig.readBytes()))
}

// ---------- 打包与 reobf ----------

val packageGtolibJar = tasks.register<Jar>("packageGtolibJar") {
    group = "gtolib"
    description = "从编译产物打包 com.gtolib → named 明文 gtolib-unprotected.jar（只在 build/ 内）"
    onlyIf { rebuildGtolib }
    dependsOn("classes")
    archiveFileName = unprotectedJar.name
    destinationDirectory = gtolibOut
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(mainSourceSet.output) {
        include("com/gtolib/**", "gtolib.mixins.json", "META-INF/*.kotlin_module")
    }
    doFirst { publishGtolibRefmaps(true) }
}

fun JavaExec.art(vararg args: String) {
    classpath = artTool
    mainClass = "net.neoforged.art.Main"
    setArgs(args.toList())
}

/** ART 继承库：Forge named + 运行时依赖里提供父类的模组 jar（useOwnClasses 时含主工程自己的 class 输出）。 */
fun artInheritanceLibs(includeOwnClasses: Boolean): List<File> {
    val libs = mutableListOf(namedForgeJar)
    if (includeOwnClasses) {
        libs += classesJava
        libs += classesKotlin
    }
    configurations["runtimeClasspath"].files
        .filter { f ->
            val n = f.name.lowercase()
            n.endsWith(".jar") && listOf("gtceu", "gtmthings", "datasynclib", "ae2", "appliedenergistics", "ex-pattern-provider").any { it in n }
        }
        .sortedBy { it.name }
        .forEach { libs += it }
    return libs.filter { it.exists() }.distinct()
}

/** ART 把 named 明文 reobf 成 SRG（发行轨 ProGuard/Seal 的输入） */
val reobfGtolib = tasks.register<JavaExec>("reobfGtolib") {
    group = "gtolib"
    description = "ART 把 named 明文 reobf 成 SRG（发行轨 ProGuard/Seal 的输入）"
    onlyIf { rebuildGtolib }
    dependsOn(packageGtolibJar, "classes", "createMinecraftArtifacts")
    outputs.file(srgJar)
    outputs.upToDateWhen { false }
    classpath = artTool
    mainClass = "net.neoforged.art.Main"
    val toolOutput = captureToolOutput()
    doFirst {
        requireJar(namedToIntermediate, "reobfGtolib: named→SRG 映射（先运行 createMinecraftArtifacts）")
        val mixinTsrg = mixinOut.resolve("gtolib.refmap.json.mappings.tsrg")
        val repoTsrg = file("src/main/resources/gtolib.refmap.json.mappings.tsrg")
        if ((!mixinTsrg.isFile || mixinTsrg.length() < 32) && repoTsrg.isFile) repoTsrg.copyTo(mixinTsrg, overwrite = true)
        ensureResourceLocationMixnTsrg(mixinTsrg)
        srgJar.delete()
        // 必须带上继承库，否则 gtocore 里重写的 Minecraft 方法（如 getSerializedName → m_7912_）不会被改名
        val libs = artInheritanceLibs(includeOwnClasses = true)
        setArgs(
            listOf("--input", unprotectedJar.absolutePath, "--output", srgJar.absolutePath, "--names", namedToIntermediate.absolutePath, "--names", mixinTsrg.absolutePath) +
                libs.flatMap { listOf("--lib", it.absolutePath) }
        )
    }
    doLast {
        finishToolOutput(name, executionResult.get().exitValue, toolOutput)
        requireJar(srgJar, "reobfGtolib: ART 输出", 1024)
        val rlm = latin1(readJarEntry(srgJar, "$resourceLocationMixnKey.class") ?: throw GradleException("reobfGtolib: 缺 ResourceLocationMixn.class"))
        if ("f_135804_" !in rlm || "m_245413_" !in rlm || "assertValidNamespace" in rlm) {
            throw GradleException("reobfGtolib: ResourceLocationMixn 未正确 reobf 成 SRG")
        }
        // Me2in1Menu 经 ex-pattern-provider 继承 getType()，缺这条继承库时生产环境看到的是 named 名
        readJarEntry(srgJar, "com/gtolib/api/ae2/me2in1/ExtendedEncodingMenu.class")?.let {
            if ("Me2in1Menu.getType" in latin1(it)) throw GradleException("reobfGtolib: ExtendedEncodingMenu 仍引用 named Me2in1Menu.getType，检查 ex-pattern-provider 是否作为继承库传给 ART")
        }
        logger.lifecycle("GTOLib ART reobf OK → ${srgJar.name}")
    }
}

// ModDevGradle 只生成 named Forge jar 与 named→SRG 映射；在 build/ 内确定性地生成 SRG library 供发行 ProGuard
val prepareForgeSrgLibrary = tasks.register<JavaExec>("prepareForgeSrgLibrary") {
    group = "gtolib"
    description = "用 ModDevGradle named 产物生成项目内 SRG Forge library（供 GTOLib 发行 ProGuard）"
    dependsOn("createMinecraftArtifacts")
    inputs.files(namedForgeJar, namedToIntermediate)
    outputs.file(forgeSrgLibraryJar)
    art("--input", namedForgeJar.absolutePath, "--output", forgeSrgLibraryJar.absolutePath, "--names", namedToIntermediate.absolutePath)
    doFirst {
        requireJar(namedForgeJar, "prepareForgeSrgLibrary: named Forge jar", 1024)
        forgeSrgLibraryJar.delete()
    }
    doLast {
        val rl = latin1(readJarEntry(forgeSrgLibraryJar, "net/minecraft/resources/ResourceLocation.class") ?: throw GradleException("prepareForgeSrgLibrary: 输出缺少 ResourceLocation.class"))
        if ("f_135804_" !in rl || "m_245413_" !in rl) throw GradleException("prepareForgeSrgLibrary: 输出仍是 named")
    }
}

/**
 * GTOLib ProGuard 必须能看到主工程（gtocore）的类：gtolib 会引用 gtocore 的字段/方法，而 ProGuard 对
 * 「owner 既不在 program 也不在 library」的成员引用**不会改写其描述符里的程序类名** —— 于是
 * `com.gtolib.*` 的原始名字会残留在 gtolib 自己的常量池里，发行包运行时 NoClassDefFoundError
 * （例如 `ICrossRecipeElectricMachine` 读 `GTORules.TIME_TWISTER_ENERGY:Lcom/gtolib/api/rule/IntRule;`）。
 * 这里把主工程已编译类（排除 com/gtolib）打包成库：dev 轨用 named，发行轨用 ART reobf 后的 SRG。
 */
val prepareGtocoreNamedLibrary = tasks.register<Jar>("prepareGtocoreNamedLibrary") {
    group = "gtolib"
    description = "打包主工程已编译类（com/gtolib 除外）为 named 库，供 GTOLib ProGuard 解析 gtocore 成员"
    onlyIf { rebuildGtolib }
    dependsOn("classes")
    archiveFileName = gtocoreNamedLibraryJar.name
    destinationDirectory = gtolibOut
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(classesJava) { exclude("com/gtolib/**") }
    from(classesKotlin) { exclude("com/gtolib/**") }
}

val prepareGtocoreSrgLibrary = tasks.register<JavaExec>("prepareGtocoreSrgLibrary") {
    group = "gtolib"
    description = "主工程库转成 SRG，供发行 ProGuard 使用"
    onlyIf { rebuildGtolib }
    dependsOn(prepareGtocoreNamedLibrary, packageGtolibJar, "createMinecraftArtifacts")
    classpath = artTool
    mainClass = "net.neoforged.art.Main"
    val toolOutput = captureToolOutput()
    doFirst {
        requireJar(gtocoreNamedLibraryJar, "prepareGtocoreSrgLibrary: 主工程 named 库", 1024)
        gtocoreSrgLibraryJar.delete()
        setArgs(
            listOf("--input", gtocoreNamedLibraryJar.absolutePath, "--output", gtocoreSrgLibraryJar.absolutePath, "--names", namedToIntermediate.absolutePath) +
                (listOf(unprotectedJar) + artInheritanceLibs(includeOwnClasses = false)).flatMap { listOf("--lib", it.absolutePath) }
        )
    }
    doLast {
        finishToolOutput(name, executionResult.get().exitValue, toolOutput)
        val converted = ZipFile(gtocoreSrgLibraryJar).use { zf ->
            zf.entries().asSequence().any { it.name.endsWith(".class") && Regex("m_\\d+_").containsMatchIn(latin1(zf.getInputStream(it).readBytes())) }
        }
        if (!converted) throw GradleException("prepareGtocoreSrgLibrary: 主工程类库未转成 SRG")
        logger.lifecycle("GTOLib ProGuard 库: 主工程 ${zipEntryCount(gtocoreSrgLibraryJar)} class → ${gtocoreSrgLibraryJar.name}")
    }
}

/** zip 里的 class 条目数（日志用）。 */
fun zipEntryCount(jar: File): Int = ZipFile(jar).use { zf -> zf.entries().asSequence().count { it.name.endsWith(".class") } }

// ---------- ProGuard ----------

/** ProGuard libraryjars：JDK + （SRG 输入时）SRG Forge + 各运行/编译 classpath 上的依赖 jar。 */
fun proguardLibraryJars(srgInput: Boolean): LinkedHashSet<File> {
    val libs = linkedSetOf<File>()
    File(System.getProperty("java.home")).let { home -> listOf(home.resolve("lib/rt.jar"), home.resolve("jmods/java.base.jmod")).firstOrNull { it.exists() } }?.let(libs::add)
    if (srgInput) {
        requireJar(forgeSrgLibraryJar, "ProGuard: SRG Forge library（先运行 prepareForgeSrgLibrary）", 1024)
        libs += forgeSrgLibraryJar
    }
    configurations.filter { it.name.endsWith("Classpath") && it.isCanBeResolved }.forEach { cfg ->
        runCatching { cfg.resolve() }.getOrNull()?.forEach { f ->
            // SRG 输入不能再混进 named Forge jar，否则 ProGuard 按 classpath 顺序解析到错误的父类方法名
            // 自家 build/gtolib 产物（如测试编译用的 dev 轨 jar）不能当库，否则会读到上一轮的旧 gtolib 或不存在的文件
            if (f.name.endsWith(".jar") && !(srgInput && f.name == namedForgeJar.name) && !f.canonicalFile.startsWith(gtolibOut.canonicalFile)) libs += f.canonicalFile
        }
    }
    return libs
}

/** 写 ProGuard 附加配置：libraryjars + 每次构建随机打乱的代码保护字典 + mapping 输出 */
fun writeProguardExtraFile(taskName: String, libs: Collection<File>, mapping: File, dir: File = gtolibOut): File {
    val dict = protectDir.resolve("my_dict.txt").readLines()
    val rnd = SecureRandom()
    val dicts = listOf("class", "package", "member").associateWith { kind ->
        dir.resolve("$taskName-$kind-dict.txt").also { it.writeText(dict.shuffled(rnd).joinToString("\n") + "\n") }
    }
    fun path(f: File) = "'${f.absolutePath.replace('\\', '/')}'"
    return dir.resolve("$taskName-extra.pro").also { extraFile ->
        extraFile.writeText(
            (libs.map { "-libraryjars ${path(it)}" } + listOf(
                "-classobfuscationdictionary ${path(dicts.getValue("class"))}",
                "-packageobfuscationdictionary ${path(dicts.getValue("package"))}",
                "-obfuscationdictionary ${path(dicts.getValue("member"))}",
                "-printmapping ${path(mapping)}",
            )).joinToString("\n") + "\n"
        )
    }
}

/**
 * gtocore 字节码与源码 import 引用的 com.gtolib 是发行轨的链接面：用 -keepclassmembers,allowobfuscation
 * 固定 public/protected 成员的可见性与可优化性（不会被优化成 private 或被 code/merging 内联掉），
 * 但类名与成员名照常改名 —— 打包时由 prepareGtolibRefRemapper 按 mapping 改写 gtocore 侧引用。
 * 注意不要改成 -keep（ProGuard 7.8 实测会连类名一起钉住，改名类数变 0）。
 */
fun writeReleaseKeepFromGtocoreRefs(target: File) {
    val refs = sortedSetOf<String>()
    val reSlash = Regex("com/gtolib/[A-Za-z0-9_$/]+")
    val reDot = Regex("com\\.gtolib\\.[A-Za-z0-9_$.]+")
    var scanned = 0
    listOf(classesJava, classesKotlin).filter { it.exists() }.forEach { root ->
        root.walkTopDown().filter { it.isFile && it.name.endsWith(".class") && !it.relativeTo(root).invariantSeparatorsPath.startsWith("com/gtolib/") }.forEach { f ->
            scanned++
            val text = latin1(f.readBytes())
            reSlash.findAll(text).forEach { refs += it.value.replace('/', '.') }
            reDot.findAll(text).forEach { refs += it.value }
        }
    }
    if (scanned == 0) throw GradleException("writeReleaseKeepFromGtocoreRefs: 找不到已编译的 gtocore 类")
    gtocoreSourceImports().map { it.groupValues[2] }.filterNot { it.endsWith(".*") }.forEach { fqn ->
        // 成员 import 的末段可能是成员名：父级一并纳入（父级是包名时 keepnames 空匹配无害）
        val i = fqn.lastIndexOf('.')
        if (i > "com.gtolib".length) refs += fqn.substring(0, i)
        refs += fqn
    }
    val keeps = refs.map { it.trimEnd('.', '$') }.filter { it.length > "com.gtolib.".length && ".." !in it }.distinct()
    target.writeText(
        "# generated: gtocore 字节码+import 对 com.gtolib 的引用面（发行轨固定成员可见性，名字照常改、由 remap 对齐）\n" +
            keeps.joinToString("") { "-keepclassmembers,allowobfuscation class $it { public protected *; }\n" }
    )
    logger.lifecycle("GTOLib release: gtocore 引用面 ${keeps.size} 项 → ${target.name}（扫描 $scanned class）")
}

/**
 * ProGuard 对「owner 既不在 program 也不在 library」的成员引用不会改写描述符里的程序类名，
 * 于是 com.gtolib.* 的原始名字会残留在 gtolib 自己的常量池里（发行包运行时 NoClassDefFoundError）。
 * 这里用 ASM 走一遍 ProGuard 输出：任何解析到「已改名类原始名」的引用直接报错，避免再次漏到生产。
 */
fun verifyNoStaleGtolibRefs(jar: File, mappingFile: File, label: String) {
    val mapping = parseGtolibReleaseMapping(mappingFile)
    val renamed = mapping.classNames.filter { (original, obf) -> original != obf }.keys
    if (renamed.isEmpty()) return
    val rawNames = (renamed + renamed.map { it.replace('/', '.') }).sortedByDescending { it.length }
    val rawPattern = Regex("(?<![A-Za-z0-9_$/.])(?:" + rawNames.joinToString("|") { Regex.escape(it) } + ")(?![A-Za-z0-9_$])")
    val stale = linkedSetOf<String>()
    var scanned = 0
    ZipFile(jar).use { zf ->
        zf.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { e ->
            scanned++
            val bytes = zf.getInputStream(e).readBytes()
            val checker = object : Remapper() {
                override fun map(internalName: String): String {
                    if (internalName in renamed) stale += "${e.name} 仍引用 $internalName"
                    return internalName
                }
            }
            ClassReader(bytes).accept(ClassRemapper(ClassWriter(0), checker), 0)
            val text = latin1(bytes)
            if ("com/gtolib" in text || "com.gtolib" in text) rawPattern.find(text)?.let { stale += "${e.name} 常量池仍含 ${it.value}" }
        }
    }
    if (stale.isNotEmpty()) {
        throw GradleException(
            "$label: ProGuard 输出里残留 ${stale.size} 处未改名的 com.gtolib 引用（检查 libraryjars 是否缺主工程类库）:\n" +
                stale.take(10).joinToString("\n") { "  - $it" }
        )
    }
    logger.lifecycle("$label: 无残留原始类名引用（扫描 $scanned class）")
}

fun registerProguard(name: String, desc: String, input: File, output: File, srgInput: Boolean, mapping: File, rules: () -> List<File>, verify: () -> Unit) =
    tasks.register<JavaExec>(name) {
        group = "gtolib"
        description = desc
        onlyIf { rebuildGtolib }
        outputs.file(output)
        outputs.upToDateWhen { false }
        classpath = proguardCp
        mainClass = "proguard.ProGuard"
        val toolOutput = captureToolOutput()
        doFirst {
            requireJar(input, "$name 输入")
            output.delete()
            val gtocoreLib = (if (srgInput) gtocoreSrgLibraryJar else gtocoreNamedLibraryJar).also { requireJar(it, "GTOLib ProGuard: 主工程类库（gtocore）", 1024) }
            val extra = writeProguardExtraFile(name, proguardLibraryJars(srgInput) + gtocoreLib, mapping)
            setArgs((rules() + extra).map { "@${it.absolutePath}" } + listOf("-injars", input.absolutePath, "-outjars", output.absolutePath))
        }
        doLast {
            finishToolOutput(name, executionResult.get().exitValue, toolOutput)
            requireJar(output, "$name 输出", 1024)
            verify()
        }
    }

val proguardGtolibDev = registerProguard(
    "proguardGtolibDev", "dev 轨 ProGuard：named 输入，同样按发行规则改名，只做方法体内优化",
    unprotectedJar, proguardDevJar, srgInput = false, mapping = out("gtolib-dev-mapping.txt"),
    rules = { listOf(protectDir.resolve("proguard-rules.pro"), writeKeepNameRules(unprotectedJar, out("keepname-dev.pro"))) },
) {
    verifyMixinNamespace(proguardDevJar, true, "GTOLib dev 轨 ProGuard")
    verifyNoStaleGtolibRefs(proguardDevJar, out("gtolib-dev-mapping.txt"), "GTOLib dev 轨 ProGuard")
    logger.lifecycle("GTOLib dev ProGuard OK → ${proguardDevJar.name}")
}
proguardGtolibDev { dependsOn(packageGtolibJar, "classes", prepareGtocoreNamedLibrary) }

val proguardGtolibRelease = registerProguard(
    "proguardGtolibRelease", "发行轨 ProGuard：SRG 输入，内部类名/包名改名",
    srgJar, proguardReleaseJar, srgInput = true, mapping = releaseMappingFile,
    rules = {
        listOf(
            protectDir.resolve("proguard-release-rules.pro"),
            writeKeepNameRules(srgJar, out("keepname-release.pro")),
            out("proguard-release-keep.pro").also(::writeReleaseKeepFromGtocoreRefs),
        )
    },
) {
    // SRG 输入必须配 SRG libraryjars，否则 Minecraft override 会被当普通方法改名（运行时 AbstractMethodError）
    val srgMethod = Regex("""\b(m_\d+_)\([^)]*\)\s+->\s+(\S+)\s*$""")
    val srgLines = releaseMappingFile.readLines().mapNotNull { srgMethod.find(it) }
    val renamed = srgLines.filter { it.groupValues[1] != it.groupValues[2] }
    if (srgLines.isEmpty()) throw GradleException("GTOLib release mapping 中没有 SRG 方法，无法验证继承契约")
    if (renamed.isNotEmpty()) throw GradleException("GTOLib release ProGuard 错误重命名了 Minecraft override:\n  " + renamed.take(12).joinToString("\n  ") { "${it.groupValues[1]} → ${it.groupValues[2]}" })
    // 代码保护必须真的生效：与 named 明文比对类路径集合
    fun classes(jar: File) = jarEntryNames(jar).filter { it.startsWith("com/gtolib") && it.endsWith(".class") }.toSet()
    val rel = classes(proguardReleaseJar)
    val dev = classes(unprotectedJar)
    val hidden = dev - rel
    if ((rel - dev).isEmpty() || hidden.isEmpty()) throw GradleException("proguardGtolibRelease: 类名改名未生效，检查 proguard-release-rules.pro 的 keep 面是否过宽")
    verifyNoStaleGtolibRefs(proguardReleaseJar, releaseMappingFile, "GTOLib 发行轨 ProGuard")
    logger.lifecycle("GTOLib release ProGuard OK: ${rel.size} class，改名/隐藏 ${hidden.size}，保名 ${rel.size - (rel - dev).size}，SRG 方法 ${srgLines.size} 个保持原名")
}
proguardGtolibRelease { dependsOn(reobfGtolib, "classes", prepareForgeSrgLibrary, prepareGtocoreSrgLibrary) }

// ---------- gtocore 引用面 remap ----------

/**
 * gtolib 发行 mapping：原始内部名 → 保护后内部名，以及每个原始类的成员改名表（键为 名字 + 原始描述符）。
 * 键用原始描述符：gtocore 常量池与 ProGuard 的 mapping 都是原始名字与原始描述符。
 */
class GtolibReleaseMapping(
    val classNames: Map<String, String>,
    val members: Map<String, Map<String, String>>,
) {
    val obfToOriginal: Map<String, String> = classNames.entries.associate { (original, obf) -> obf to original }

    /** 原始类 -> 方法名 -> 保护后名字（含所有重载），用于 SAM 名在描述符对不上时的兜底解析。 */
    val methodNames: Map<String, Map<String, Set<String>>> = run {
        val byOwner = LinkedHashMap<String, MutableMap<String, MutableSet<String>>>()
        members.forEach { (owner, table) ->
            table.forEach { (key, obf) ->
                val paren = key.indexOf('(')
                if (paren <= 0) return@forEach // 字段：描述符里没有括号
                val name = key.substring(0, paren)
                byOwner.getOrPut(owner) { LinkedHashMap() }.getOrPut(name) { linkedSetOf() } += obf
            }
        }
        byOwner.mapValues { (_, v) -> v.mapValues { (_, s) -> s.toSet() } }
    }
}

fun typeDescriptor(type: String): String = when (type) {
    "void" -> "V"
    "boolean" -> "Z"
    "byte" -> "B"
    "char" -> "C"
    "short" -> "S"
    "int" -> "I"
    "long" -> "J"
    "float" -> "F"
    "double" -> "D"
    else -> if (type.endsWith("[]")) "[" + typeDescriptor(type.dropLast(2)) else "L" + type.replace('.', '/') + ";"
}

/** 解析 ProGuard -printmapping 输出：类头 `a.b.C -> a.b.X:`，成员行 `    int field -> a` / `29:29:void m(int) -> b`。 */
fun parseGtolibReleaseMapping(file: File): GtolibReleaseMapping {
    requireJar(file, "gtolib 发行 mapping")
    val classHeader = Regex("""^(\S+) -> (\S+):$""")
    val memberLine = Regex("""^\s+(?:\d+:\d+:)?(\S+)\s+([^\s(]+)(?:\(([^)]*)\))?(?::\d+(?::\d+)?)?\s+->\s+(\S+)$""")
    val classNames = LinkedHashMap<String, String>()
    val members = LinkedHashMap<String, MutableMap<String, String>>()
    var current: String? = null
    file.readLines(Charsets.UTF_8).forEach { raw ->
        val header = classHeader.find(raw)
        if (header != null) {
            val original = header.groupValues[1].replace('.', '/')
            current = original
            classNames[original] = header.groupValues[2].replace('.', '/')
            members.getOrPut(original) { LinkedHashMap() }
            return@forEach
        }
        val owner = current ?: return@forEach
        val m = memberLine.find(raw) ?: return@forEach
        val args = m.groups[3]
        val descriptor = if (args != null) {
            "(" + args.value.split(',').map { it.trim() }.filter { it.isNotEmpty() }.joinToString("") { typeDescriptor(it) } +
                ")" + typeDescriptor(m.groupValues[1])
        } else {
            typeDescriptor(m.groupValues[1])
        }
        members.getValue(owner)[m.groupValues[2] + descriptor] = m.groupValues[4]
    }
    if (classNames.isEmpty()) throw GradleException("gtolib 发行 mapping 解析为空: $file")
    return GtolibReleaseMapping(classNames, members)
}

/** 读 jar 里满足 filter 的类的直接父类/接口（内部名）。 */
fun readClassHierarchy(jar: File, filter: (String) -> Boolean): Map<String, List<String>> {
    val result = LinkedHashMap<String, List<String>>()
    ZipFile(jar).use { zf ->
        zf.entries().asSequence().filter { it.name.endsWith(".class") && filter(it.name) }.forEach { e ->
            ClassReader(zf.getInputStream(e).readBytes()).accept(object : ClassVisitor(Opcodes.ASM9) {
                override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
                    result[name] = listOfNotNull(superName) + (interfaces?.toList() ?: emptyList())
                }
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        }
    }
    return result
}

/**
 * gtocore 与 gtolib 分两次编译，ProGuard 看不到 gtocore 的继承关系：gtocore 里对 com.gtolib 的引用
 * （含从 gtolib 父类/接口继承的成员与覆盖方法）必须在打包时按发行 mapping 对齐。
 * 按层（BFS）在原始命名空间的类层次里找最近的 gtolib 声明；同一层出现两个不同改名即冲突。
 */
class GtolibRefRemapper(
    private val mapping: GtolibReleaseMapping,
    gtolibHierarchy: Map<String, List<String>>,
    programHierarchy: Map<String, List<String>>,
) : Remapper() {
    private val hierarchy: Map<String, List<String>> = HashMap<String, List<String>>().apply {
        putAll(gtolibHierarchy)
        putAll(programHierarchy)
    }
    val conflicts = LinkedHashSet<String>()
    val declarationViolations = LinkedHashSet<String>()
    val usedGtolibClasses = linkedSetOf<String>()
    var stringRefs = 0
        private set
    var samNamesFixed = 0

    override fun map(internalName: String): String {
        if (internalName.startsWith("com/gtolib/")) usedGtolibClasses += mapping.classNames[internalName] ?: internalName
        return mapping.classNames[internalName] ?: internalName
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String =
        resolveMember(owner, name, descriptor) ?: name

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        if (name == "<init>" || name == "<clinit>") return name
        return resolveMember(owner, name, descriptor) ?: name
    }

    /** 类名字符串跟着改名（对齐 ProGuard 的 -adaptclassstrings）。 */
    override fun mapValue(value: Any?): Any? {
        if (value is String && value.startsWith("com.gtolib.")) {
            val mapped = mapping.classNames[value.replace('.', '/')]
            if (mapped != null) {
                stringRefs++
                return mapped.replace('/', '.')
            }
        }
        return super.mapValue(value)
    }

    /**
     * LambdaMetafactory 的 SAM 名字：invokedynamic 的返回类型就是那个函数式接口，bsmArgs[0] 是擦除后的 SAM 类型。
     * ASM 的 ClassRemapper 不会碰 SAM 名（它拿不到 bsmArgs），必须在这里单独解析。
     */
    fun resolveSamName(samName: String, indyDescriptor: String, bsm: Handle, bsmArgs: Array<out Any?>): String {
        if (bsm.owner != "java/lang/invoke/LambdaMetafactory") return samName
        val iface = Type.getReturnType(indyDescriptor)
        if (iface.sort != Type.OBJECT || !iface.internalName.startsWith("com/gtolib/")) return samName
        val samType = bsmArgs.firstOrNull() as? Type ?: return samName
        resolveMember(iface.internalName, samName, samType.descriptor)?.let { return it }
        return resolveMemberByName(iface.internalName, samName) ?: samName
    }

    /** 泛型擦除导致 SAM 描述符对不上时，按名字在接口层次里找唯一的方法改名。 */
    private fun resolveMemberByName(owner: String, name: String): String? {
        var frontier = listOf(owner)
        val visited = HashSet<String>()
        while (frontier.isNotEmpty()) {
            val found = LinkedHashSet<String>()
            val next = ArrayList<String>()
            frontier.forEach { cls ->
                if (!visited.add(cls)) return@forEach
                mapping.methodNames[cls]?.get(name)?.let { found += it }
                hierarchy[cls]?.let { next += it }
            }
            if (found.size == 1) return found.first()
            if (found.size > 1) {
                conflicts += "$owner.$name() -> ${found.joinToString(" / ")}（SAM 兜底解析不唯一）"
                return null
            }
            frontier = next
        }
        return null
    }

    fun resolveMember(owner: String, name: String, descriptor: String): String? {
        val key = name + descriptor
        var frontier = listOf(owner)
        val visited = HashSet<String>()
        while (frontier.isNotEmpty()) {
            val found = LinkedHashSet<String>()
            val next = ArrayList<String>()
            frontier.forEach { cls ->
                if (!visited.add(cls)) return@forEach
                mapping.members[cls]?.get(key)?.let { found += it }
                hierarchy[cls]?.let { next += it }
            }
            if (found.size > 1) conflicts += "$owner.$key -> ${found.joinToString(" / ")}"
            if (found.isNotEmpty()) return found.first()
            frontier = next
        }
        return null
    }

    /** 自检：原始类里解析到改名的成员，代码保护后必须真的以新名字+新描述符出现（否则覆盖会悄悄失效）。 */
    fun verifyDeclarations(before: ClassNode, after: ClassNode) {
        before.methods.forEach { m ->
            val expected = resolveMember(before.name, m.name, m.desc) ?: return@forEach
            if (expected != m.name && after.methods.none { it.name == expected && it.desc == mapMethodDesc(m.desc) }) {
                declarationViolations += "${before.name}.${m.name}${m.desc} (method) 应为 $expected"
            }
        }
        before.fields.forEach { f ->
            val expected = resolveMember(before.name, f.name, f.desc) ?: return@forEach
            if (expected != f.name && after.fields.none { it.name == expected && it.desc == mapDesc(f.desc) }) {
                declarationViolations += "${before.name}.${f.name}${f.desc} (field) 应为 $expected"
            }
        }
        // 两个不同父类/接口里描述符相同、名字不同的成员可能被改成同一个名字：同名同描述符在一个类里是非法字节码
        val methodKeys = HashSet<String>()
        after.methods.forEach { m ->
            if (!methodKeys.add(m.name + m.desc)) {
                conflicts += "${before.name} 代码保护后重复方法 ${m.name}${m.desc}（两个父类/接口的成员被改成同名，给其中一个加 @KeepName）"
            }
        }
        val fieldKeys = HashSet<String>()
        after.fields.forEach { f ->
            if (!fieldKeys.add(f.name + f.desc)) {
                conflicts += "${before.name} 代码保护后重复字段 ${f.name}${f.desc}（同上）"
            }
        }
    }
}

/** 打包前准备 remap：从发行包按 mapping 反推 gtolib 的原始命名空间层次，从待打包 jar 读 gtocore 侧层次。 */
fun prepareGtolibRefRemapper(jar: File, releaseJar: File, mapping: GtolibReleaseMapping): GtolibRefRemapper {
    val gtolibHierarchy = readClassHierarchy(releaseJar) { it.startsWith("com/gtolib/") && it.endsWith(".class") }
        .mapKeys { (name, _) -> mapping.obfToOriginal[name] ?: name }
        .mapValues { (_, supers) -> supers.map { mapping.obfToOriginal[it] ?: it } }
    val programHierarchy = readClassHierarchy(jar) { it.endsWith(".class") && !it.startsWith("com/gtolib/") }
    if (gtolibHierarchy.isEmpty() || programHierarchy.isEmpty()) {
        throw GradleException("prepareGtolibRefRemapper: 类层次为空（gtolib=${gtolibHierarchy.size}, gtocore=${programHierarchy.size}）")
    }
    logger.lifecycle("GTOLib remap: 类层次 gtolib=${gtolibHierarchy.size} gtocore=${programHierarchy.size}")
    return GtolibRefRemapper(mapping, gtolibHierarchy, programHierarchy)
}

fun remapGtolibRefs(remapper: GtolibRefRemapper, bytes: ByteArray): ByteArray {
    // 含方法体读取：LambdaMetafactory 的 SAM 名必须自己改（ClassRemapper 不覆盖）
    val before = ClassNode(Opcodes.ASM9).also { ClassReader(bytes).accept(it, 0) }
    var samFixed = 0
    before.methods.forEach { m ->
        m.instructions?.toArray()?.forEach { insn ->
            if (insn is InvokeDynamicInsnNode) {
                val mapped = remapper.resolveSamName(insn.name, insn.desc, insn.bsm, insn.bsmArgs)
                if (mapped != insn.name) {
                    insn.name = mapped
                    samFixed++
                }
            }
        }
    }
    val writer = ClassWriter(0)
    before.accept(ClassRemapper(writer, remapper))
    val remapped = writer.toByteArray()
    val after = ClassNode(Opcodes.ASM9).also {
        ClassReader(remapped).accept(it, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
    }
    remapper.verifyDeclarations(before, after)
    if (samFixed > 0) remapper.samNamesFixed += samFixed
    return remapped
}

/** remap 收尾：冲突/自检失败直接中断打包；引用到的 com.gtolib 类必须真的在发行包里存在。 */
fun finishGtolibRefRemap(remapper: GtolibRefRemapper, jar: File, classes: Int) {
    if (remapper.conflicts.isNotEmpty()) {
        throw GradleException(
            "GTOLib remap 冲突：${remapper.conflicts.size} 个成员在多个 gtolib 父类/接口里有不同的保护后名字，gtocore 侧无法同时满足。\n" +
                "给其中一个成员加 @KeepName（或补 -keepnames）后重建：\n" +
                remapper.conflicts.take(20).joinToString("\n") { "  - $it" }
        )
    }
    if (remapper.declarationViolations.isNotEmpty()) {
        throw GradleException(
            "GTOLib remap 自检失败：${remapper.declarationViolations.size} 个成员没有按 mapping 改名（覆盖会悄悄失效）。\n" +
                remapper.declarationViolations.take(20).joinToString("\n") { "  - $it" }
        )
    }
    val entries = jarEntryNames(jar)
    val missing = remapper.usedGtolibClasses.filterNot { it + ".class" in entries }
    if (missing.isNotEmpty()) {
        throw GradleException("GTOLib remap 后引用不存在的 com.gtolib 类型：\n" + missing.take(20).joinToString("\n") { "  - $it" })
    }
    logger.lifecycle(
        "GTOLib remap: gtocore 侧 $classes 个 class 对齐发行代码保护后的名字" +
            "（类名字符串 ${remapper.stringRefs} 处，函数式接口 SAM 名 ${remapper.samNamesFixed} 处）"
    )
}

/**
 * 函数式接口 lambda 自检：invokedynamic 的 SAM 名必须能在（代码保护后的）gtolib 接口层次里解析到。
 * 否则 LambdaMetafactory 生成的类实现的是旧名字，运行时 AbstractMethodError
 * （ASM ClassRemapper 不改 SAM 名，remapGtolibRefs 负责改，这里校验结果）。
 */
fun verifyGtolibSamTargets(jar: File) {
    data class Cls(val supers: List<String>, val members: Set<String>)

    val index = HashMap<String, Cls>()
    ZipFile(jar).use { zf ->
        zf.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { e ->
            val node = ClassNode(Opcodes.ASM9)
            ClassReader(zf.getInputStream(e).readBytes()).accept(node, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            index[node.name] = Cls(
                listOfNotNull(node.superName) + node.interfaces,
                (node.methods.map { it.name + it.desc } + node.fields.map { it.name + it.desc }).toSet(),
            )
        }
    }
    fun resolves(owner: String, name: String, desc: String): Boolean {
        var frontier = listOf(owner)
        val visited = HashSet<String>()
        while (frontier.isNotEmpty()) {
            var found = false
            val next = ArrayList<String>()
            frontier.forEach { cls ->
                if (!visited.add(cls)) return@forEach
                val info = index[cls] ?: return@forEach
                if (name + desc in info.members) found = true else next += info.supers
            }
            if (found) return true
            frontier = next
        }
        return false
    }
    val bad = linkedSetOf<String>()
    var checked = 0
    ZipFile(jar).use { zf ->
        zf.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { e ->
            val node = ClassNode(Opcodes.ASM9)
            ClassReader(zf.getInputStream(e).readBytes()).accept(node, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            node.methods.forEach { m ->
                m.instructions?.toArray()?.forEach { insn ->
                    if (insn !is InvokeDynamicInsnNode) return@forEach
                    val iface = Type.getReturnType(insn.desc)
                    if (iface.sort != Type.OBJECT || !iface.internalName.startsWith("com/gtolib/")) return@forEach
                    val sam = insn.bsmArgs.firstOrNull() as? Type ?: return@forEach
                    checked++
                    if (!resolves(iface.internalName, insn.name, sam.descriptor)) {
                        bad += "${node.name}.${m.name}: ${iface.internalName}.${insn.name}${sam.descriptor}"
                    }
                }
            }
        }
    }
    if (bad.isNotEmpty()) {
        throw GradleException(
            "GTOLib remap: ${bad.size} 个函数式接口 lambda 的 SAM 名在发行包里解析不到（运行时会 AbstractMethodError）:\n" +
                bad.take(10).joinToString("\n") { "  - $it" }
        )
    }
    logger.lifecycle("GTOLib remap: 函数式接口 lambda SAM 校验 OK（$checked 个 -> com/gtolib 接口）")
}

/**
 * 第三种漏法：gtolib 接口的抽象方法被改名，但它的实现来自 library（gtceu/AE2 等）里**同名同签名**的方法
 * —— Mixin 把 gtolib 接口挂到 library 类上，实现继承自父类，library 名字不会被改名，
 * 于是接口方法成了"没有实现的抽象方法"，运行时 AbstractMethodError。
 * 判定：jar+library 层次里找不到新名字的具体实现，但 library 里有旧名字的同签名实现。
 * 修法：给该接口方法加 @KeepName（library 实现保名，接口也得保名）。
 */
fun verifyLibraryBackedInterfaceMethods(jar: File, extraProFile: File, mapping: GtolibReleaseMapping) {
    // 保护后类 -> (保护后方法名+描述符 -> 原始方法名)，只保留改过名的方法
    val renamedMembers = HashMap<String, MutableMap<String, String>>()
    mapping.members.forEach { (origClass, table) ->
        val obfClass = mapping.classNames[origClass] ?: return@forEach
        table.forEach { (key, obfName) ->
            val paren = key.indexOf('(')
            if (paren <= 0) return@forEach
            val origName = key.substring(0, paren)
            if (origName == obfName) return@forEach
            renamedMembers.getOrPut(obfClass) { HashMap() }[obfName + key.substring(paren)] = origName
        }
    }
    if (renamedMembers.isEmpty()) return

    class Cls(val iface: Boolean) {
        val supers = ArrayList<String>()
        val members = HashSet<String>()
        val abstractMembers = HashSet<String>()
    }
    val index = HashMap<String, Cls>()
    val jarClasses = ArrayList<String>()
    fun add(file: File, wantList: Boolean) {
        if (!file.isFile || !file.name.endsWith(".jar")) return
        ZipFile(file).use { zf ->
            zf.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { e ->
                val node = ClassNode(Opcodes.ASM9)
                ClassReader(zf.getInputStream(e).readBytes()).accept(node, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                val cls = Cls((node.access and Opcodes.ACC_INTERFACE) != 0)
                listOfNotNull(node.superName).forEach { cls.supers += it }
                cls.supers += node.interfaces
                node.methods.forEach { m ->
                    cls.members += m.name + m.desc
                    if (m.access and Opcodes.ACC_ABSTRACT != 0) cls.abstractMembers += m.name + m.desc
                }
                index[node.name] = cls
                if (wantList) jarClasses += node.name
            }
        }
    }
    add(jar, true)
    extraProFile.takeIf { it.isFile }?.readLines(Charsets.UTF_8)?.forEach { line ->
        if (line.startsWith("-libraryjars")) add(File(line.substringAfter('\'').substringBeforeLast('\'')), false)
    }

    fun hierarchy(start: String): Set<String> {
        val seen = LinkedHashSet<String>()
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val n = queue.removeFirst()
            if (!seen.add(n)) continue
            index[n]?.supers?.forEach { queue += it }
        }
        return seen
    }
    fun concrete(owner: String, key: String) =
        index[owner]?.let { key in it.members && key !in it.abstractMembers } == true

    val broken = linkedSetOf<String>()
    jarClasses.forEach { caller ->
        val hier = hierarchy(caller)
        hier.forEach { name ->
            val iface = index[name] ?: return@forEach
            if (!iface.iface || !name.startsWith("com/gtolib/")) return@forEach
            val renamed = renamedMembers[name] ?: return@forEach
            renamed.forEach { (obfKey, origName) ->
                if (hier.any { concrete(it, obfKey) }) return@forEach
                val origKey = origName + obfKey.substring(obfKey.indexOf('('))
                val libImpl = hier.firstOrNull { !jarClasses.contains(it) && concrete(it, origKey) }
                if (libImpl != null) broken += "$caller 实现 $name.$obfKey（原 $origName），实现在 library $libImpl → 给 $name.$origName 加 @KeepName"
            }
        }
    }
    if (broken.isNotEmpty()) {
        throw GradleException(
            "GTOLib remap: ${broken.size} 个接口方法被改名、实现却来自 library（运行时会 AbstractMethodError）:\n" +
                broken.take(10).joinToString("\n") { "  - $it" }
        )
    }
    logger.lifecycle("GTOLib remap: library 实现型接口方法校验 OK（扫描 ${index.size} class）")
}

/** 最终自检：非 gtolib 类里不允许再出现已改名 gtolib 类的原始名字（字符串、注解、描述符一并覆盖）。 */
fun verifyNoOriginalGtolibNames(jar: File, mapping: GtolibReleaseMapping) {
    val renamed = mapping.classNames.filter { (original, obf) -> original != obf }.keys
    if (renamed.isEmpty()) return
    val pattern = Regex("(?<![A-Za-z0-9_$/])(?:" + renamed.joinToString("|") { Regex.escape(it) } + ")(?![A-Za-z0-9_$])")
    val hits = linkedSetOf<String>()
    ZipFile(jar).use { zf ->
        zf.entries().asSequence().filter { it.name.endsWith(".class") && !it.name.startsWith("com/gtolib/") }.forEach { e ->
            val text = latin1(zf.getInputStream(e).readBytes())
            if (!text.contains("com/gtolib")) return@forEach
            pattern.find(text)?.let { hits += "${e.name} 仍引用 ${it.value}" }
        }
    }
    if (hits.isNotEmpty()) {
        throw GradleException("GTOLib remap 遗漏 ${hits.size} 处原始引用：\n" + hits.take(20).joinToString("\n") { "  - $it" })
    }
}

// ---------- Seal ----------

/**
 * 发行空壳压缩：运行时由 JVMTI 用解密体整体替换类字节，空壳只需保留结构。
 * 去掉常量值、泛型签名、throws、不可见注解与调试信息；含真实代码的空壳（枚举）原样保留。
 */
fun compactShell(bytes: ByteArray): ByteArray? {
    fun read(b: ByteArray, flags: Int) = ClassNode(Opcodes.ASM9).also { ClassReader(b).accept(it, flags) }
    fun fmt(v: Any?): String = when {
        v == null -> "null"
        v is List<*> -> v.map(::fmt).toString()
        v.javaClass.isArray -> (0 until java.lang.reflect.Array.getLength(v)).map { fmt(java.lang.reflect.Array.get(v, it)) }.toString()
        v is AnnotationNode -> "@${v.desc}${fmt(v.values)}"
        else -> v.toString()
    }
    fun shape(n: ClassNode) = buildString {
        appendLine("${n.access} ${n.name} ${n.superName} ${n.interfaces} ${n.outerClass} ${n.outerMethod} ${n.outerMethodDesc} ${n.nestHostClass} ${n.nestMembers} ${n.permittedSubclasses} ${fmt(n.visibleAnnotations)}")
        n.innerClasses.forEach { appendLine("I ${it.name} ${it.outerName} ${it.innerName} ${it.access}") }
        n.fields.forEach { appendLine("F ${it.access} ${it.name} ${it.desc} ${fmt(it.visibleAnnotations)}") }
        n.methods.forEach { appendLine("M ${it.access} ${it.name}${it.desc} ${fmt(it.visibleAnnotations)} ${fmt(it.visibleParameterAnnotations)} ${fmt(it.annotationDefault)}") }
    }
    val original = read(bytes, 0)
    if (original.access and Opcodes.ACC_ENUM != 0 || original.methods.any { m -> m.instructions.toArray().count { it.opcode >= 0 } > 5 }) return null
    val node = read(bytes, ClassReader.SKIP_DEBUG)
    node.signature = null
    node.invisibleAnnotations = null
    node.invisibleTypeAnnotations = null
    node.fields.forEach {
        it.value = null
        it.signature = null
        it.invisibleAnnotations = null
        it.invisibleTypeAnnotations = null
    }
    node.methods.forEach {
        it.signature = null
        it.exceptions = mutableListOf()
        it.invisibleAnnotations = null
        it.invisibleTypeAnnotations = null
        it.invisibleParameterAnnotations = null
        it.invisibleAnnotableParameterCount = 0
    }
    val compacted = ClassWriter(0).also { node.accept(it) }.toByteArray()
    if (shape(read(compacted, 0)) != shape(original)) throw GradleException("发行空壳压缩改变了类结构: ${original.name}")
    return compacted
}

/** Packager 约定 java -jar gto-seal-packager.jar <prod.jar> <dev.jar> <output.jar>；两条轨道都用同一份 ProGuard 产物作 prod 与 dev。 */
fun registerSeal(name: String, desc: String, input: File, output: File, finish: () -> Unit) = tasks.register<Exec>(name) {
    group = "gtolib"
    description = desc
    onlyIf { rebuildGtolib }
    outputs.file(output)
    outputs.upToDateWhen { false }
    val toolOutput = captureToolOutput()
    doFirst {
        requireJar(input, "$name 输入")
        val stamped = out("$name-input.jar")
        val salt = java.util.Random().nextLong().toString()
        stampSealManifest(input, stamped, salt)
        output.delete()
        logger.lifecycle("GTO Seal [$name]: Salt=$salt ← ${input.name}")
        commandLine(File(System.getProperty("java.home"), "bin/java").absolutePath, "-jar", sealPackagerJar.absolutePath, stamped.absolutePath, stamped.absolutePath, output.absolutePath)
    }
    doLast {
        finishToolOutput(name, executionResult.get().exitValue, toolOutput)
        requireJar(output, "$name 输出", 1024)
        finish()
    }
}

val sealGtolibDev = registerSeal("sealGtolibDev", "Seal 加密 dev 轨（named hollow、同样改名，只在本机使用）", proguardDevJar, protectedJar) {
    verifySealShells(protectedJar, "GTO Seal(dev)")
    injectMixinResources(protectedJar, true)
    verifyMixinNamespace(protectedJar, true, "GTOLib dev 轨")
}
sealGtolibDev { dependsOn(proguardGtolibDev) }

val sealGtolibRelease = registerSeal("sealGtolibRelease", "Seal 加密发行轨（类名改名），剥除 .dev.bin 并压缩空壳", proguardReleaseJar, releaseJar) {
    var stripped = 0
    var compacted = 0
    var before = 0L
    var after = 0L
    rewriteJar(releaseJar) { entry, bytes ->
        when {
            entry.endsWith(".dev.bin") -> null.also { stripped++ }
            entry.startsWith("com/gtolib/") && entry.endsWith(".class") && !entry.startsWith("com/gtolib/mixin/") ->
                compactShell(bytes)?.also {
                    compacted++
                    before += bytes.size
                    after += it.size
                } ?: bytes
            else -> bytes
        }
    }
    if (compacted == 0) throw GradleException("发行空壳压缩未处理任何类")
    logger.lifecycle("GTO Seal(release) 空壳压缩: $compacted 个 $before → $after bytes；已剥除 dev.bin=$stripped")
    if (jarEntryNames(releaseJar).any { it.endsWith(".dev.bin") }) throw GradleException("发行包剥除 dev.bin 失败")
    verifySealShells(releaseJar, "GTO Seal(release) OK")
    injectMixinResources(releaseJar, false)
    verifyMixinNamespace(releaseJar, false, "GTOLib 发行单轨")
    if ("f_135804_" !in latin1(readJarEntry(releaseJar, "$resourceLocationMixnKey.class") ?: ByteArray(0))) {
        throw GradleException("发行包缺少 SRG 的明文 ResourceLocationMixn（keep 面或 reobf 链断了）")
    }
    verifyGtocoreImportsCoveredByMapping(parseGtolibReleaseMapping(releaseMappingFile))
}
sealGtolibRelease { dependsOn(proguardGtolibRelease) }

tasks.register("buildGtolibProtected") {
    group = "gtolib"
    description = "生成加密 gtolib（dev 轨 protected + 发行轨 release）到 build/gtolib；明文仅 -PgtolibUnprotected"
    if (rebuildGtolib) dependsOn(sealGtolibDev, sealGtolibRelease)
    dependsOn("syncSealRuntimeToLibs")
    // 只声明自己写的 stamp：jar 由 Seal 任务生成，在这里声明会被 Gradle 当作来历不明的过期输出提前删掉
    outputs.file(stampFile)
    outputs.upToDateWhen { isGtolibJarUpToDate }
    doLast {
        if (isGtolibDebugMode) {
            logger.lifecycle("GTOLib: 【未加密/本地】直接使用 main 编译输出；protected jar 与 stamp 保持不变")
            return@doLast
        }
        if (isGtolibJarUpToDate) {
            logger.lifecycle("GTOLib: stamp 命中，跳过 ProGuard/Seal")
            verifyMixinNamespace(protectedJar, true, "GTOLib dev 轨（stamp 命中）")
            verifyMixinNamespace(releaseJar, false, "GTOLib 发行单轨（stamp 命中）")
            verifyGtocoreImportsCoveredByMapping(parseGtolibReleaseMapping(releaseMappingFile))
        }
        requireJar(protectedJar, "gtolib-protected.jar")
        requireJar(releaseJar, "gtolib-release.jar")
        val jarSha = sha256(protectedJar)
        stampFile.writeText("fingerprint=$gtolibFingerprint\njarSha256=$jarSha\nreleaseSha256=${sha256(releaseJar)}\n")
        logger.lifecycle(
            "GTOLib protected (ProGuard+Seal): ${protectedJar.name} ${protectedJar.length()} bytes, release ${releaseJar.name} ${releaseJar.length()} bytes; " +
                "stamp fp=${gtolibFingerprint.take(16)}… jarSha=${jarSha.take(16)}…"
        )
    }
}

// ---------- GTO Seal Runtime ----------

if (hasGtosealSources) {
    val sealRuntime = sourceSets.create("sealRuntime") {
        java.srcDir("GTOSeal/src/main/java")
        resources.srcDir("GTOSeal/src/main/resources")
        compileClasspath += mainSourceSet.compileClasspath
    }
    tasks.named<JavaCompile>("compileSealRuntimeJava") {
        options.encoding = "UTF-8"
        options.release = 21
        classpath = mainSourceSet.compileClasspath
        mustRunAfter("compileJava")
    }
    // 纯 Java Runtime 仅作开发对照，输出到 build/，不覆盖 libs
    tasks.register<Jar>("packSealRuntimeJar") {
        group = "gtolib"
        description = "GTOSeal 源码 → 纯 Java Runtime（build/ 对照，不覆盖 libs）"
        dependsOn("compileSealRuntimeJava")
        archiveFileName = "gto-seal-runtime-java.jar"
        destinationDirectory = layout.buildDirectory.dir("seal-runtime")
        from(sealRuntime.output)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mustRunAfter("compileJava", "classes")
    }
    // 完整 Seal Runtime（含平台库）→ libs：改 gtolib 不必有 GTOSeal 源码，只在改了 Runtime 构建产物时从 GTOSeal/jni|dist 覆盖
    tasks.register("syncSealRuntimeToLibs") {
        group = "gtolib"
        description = "同步完整 GTO Seal Runtime → libs/gto-seal-runtime-1.0.jar（目标被占用时自动跳过）"
        val candidates = listOf(file("GTOSeal/jni/gto-seal-runtime-1.0.jar"), file("GTOSeal/dist/gto-seal-runtime-1.0.jar"))
        // 本机已开着 runServer/runClient 时可手动跳过
        outputs.upToDateWhen { findProperty("skipSealSync") != "true" }
        doLast {
            if (findProperty("skipSealSync") == "true") {
                logger.lifecycle("GTO Seal Runtime: -PskipSealSync=true → 跳过同步")
                return@doLast
            }
            val src = candidates.firstOrNull { it.exists() && it.length() > 100_000 }
            if (src != null) {
                // 内容一致时不触碰目标文件，runServer/runClient 锁定时也安全
                if (sealRuntimeLibsJar.length() == src.length() && sha256(sealRuntimeLibsJar) == sha256(src)) {
                    logger.lifecycle("GTO Seal Runtime: 内容一致，跳过同步 (${sealRuntimeLibsJar.length()} bytes)")
                    return@doLast
                }
                try {
                    src.copyTo(sealRuntimeLibsJar, overwrite = true)
                } catch (e: java.io.IOException) {
                    // 典型场景：runServer / runClient 仍持有目标文件
                    if (sealRuntimeLibsJar.length() > 100_000) {
                        logger.warn("GTO Seal Runtime: 目标被占用，跳过同步（沿用现有 libs/${sealRuntimeLibsJar.name} ${sealRuntimeLibsJar.length()} bytes）：${e.message}")
                        return@doLast
                    }
                    throw e
                }
                logger.lifecycle("GTO Seal Runtime (full) → $sealRuntimeLibsJar (${sealRuntimeLibsJar.length()} bytes) ← $src")
                return@doLast
            }
            if (sealRuntimeLibsJar.length() > 100_000) {
                logger.lifecycle("GTO Seal Runtime: 无 GTOSeal 构建产物，沿用 libs (${sealRuntimeLibsJar.length()} bytes) —— 改 gtolib 无需 Seal 权限")
                return@doLast
            }
            throw GradleException(
                "缺少完整 GTO Seal Runtime（通常 >100KB，含平台库）。\n优先: libs/gto-seal-runtime-1.0.jar（Main 公开预构建）\n" +
                    "或有 GTOSeal 权限时: GTOSeal/jni/gto-seal-runtime-1.0.jar\n禁止用纯 Java stub 顶替。"
            )
        }
    }
} else {
    tasks.register("syncSealRuntimeToLibs") {
        group = "gtolib"
        description = "确认 libs 中已有完整 Seal Runtime（无需 GTOSeal 权限）"
        doLast {
            if (sealRuntimeLibsJar.length() <= 100_000) {
                throw GradleException("libs/gto-seal-runtime-1.0.jar 缺失或过小。\n改 gtolib 不需要 GTOSeal 权限，但 Main 仓库必须带完整 Runtime 预构建。")
            }
            logger.lifecycle("GTO Seal Runtime: 沿用 libs (${sealRuntimeLibsJar.length()} bytes) —— 改 gtolib 只需 GTOLib + Main libs Runtime")
        }
    }
}

fun requireSealRuntime() {
    if (sealRuntimeLibsJar.length() < 100_000) throw GradleException("需要完整 GTO Seal Runtime（独立 jar，勿内嵌）: $sealRuntimeLibsJar（./gradlew syncSealRuntimeToLibs）")
}

// Seal Runtime 复制到 build/libs，与 gtocore 并列分发
val stageSealRuntimeArtifact = tasks.register<Copy>("stageSealRuntimeArtifact") {
    group = "gtolib"
    description = "复制 gto-seal-runtime 到 build/libs（与 gtocore 并列产物）"
    dependsOn("syncSealRuntimeToLibs")
    from(sealRuntimeLibsJar)
    into(layout.buildDirectory.dir("libs"))
    doFirst { requireSealRuntime() }
}

// ---------- 开发运行 ----------

/** Seal 抽空会丢注解，FML 扫描不到 @Mod：独立 gtolib 模组 jar 需要明文 stub（源码在 gradle/scripts/gtolib-mod-stub/src） */
val prepareGtolibModJar = tasks.register("prepareGtolibModJar") {
    group = "gtolib"
    description = "准备 run/*/mods 用的独立 gtolib 模组 jar（dev 轨加密体 + mods.toml + 明文 @Mod stub）"
    dependsOn("buildGtolibProtected")
    inputs.files(protectedJar, gtolibModStubClass)
    inputs.property("gtolibModVersion", project.version.toString())
    outputs.file(modJar)
    doLast {
        requireJar(protectedJar, "gtolib-protected.jar")
        requireJar(gtolibModStubClass, "GTOLibMod stub", 50)
        val toml = """
            modLoader="javafml"
            loaderVersion="[47,)"
            license="All Rights Reserved"

            [[mods]]
            modId="gtolib"
            version="${project.version}"
            displayName="GTOLib"
            description='''Seal-protected GTO library (embedded companion of gtocore).'''
        """.trimIndent() + "\n"
        // 1.20.1 客户端为每个 mod 建 ResourcePackInfo，缺 pack.mcmeta 会报 Missing metadata in pack mod:gtolib
        val packMeta = """{"pack": {"description": "gtolib (Seal-protected companion)", "pack_format": 15}}""" + "\n"
        rewriteJar(
            protectedJar, modJar,
            mapOf(
                "META-INF/mods.toml" to toml.toByteArray(),
                "pack.mcmeta" to packMeta.toByteArray(),
                "com/gtolib/GTOLibMod.class" to gtolibModStubClass.readBytes(),
            ),
        )
        logger.lifecycle("GTOLib mod jar → $modJar (${modJar.length()} bytes)")
    }
}

/** 加密：独立 gtolib 模组 jar + 独立 Seal Runtime 放进各 run 的 mods 目录，main 输出不留明文；明文调试直接使用 main 编译输出 */
val installGtolibIntoModOutput = tasks.register("installGtolibIntoModOutput") {
    group = "gtolib"
    description = "加密→run/mods 独立 gtolib；显式调试→直接使用并保留 main 编译输出"
    dependsOn("classes", "processResources")
    if (!isGtolibDebugMode) dependsOn(prepareGtolibModJar, "syncSealRuntimeToLibs")
    doLast {
        runDirs.forEach { dir ->
            dir.listFiles()?.filter { f -> f.name.endsWith(".jar") && listOf("gtolib", "gto-seal-runtime", "gtonativelib").any { f.name.startsWith(it) } }?.forEach { it.delete() }
        }
        if (isGtolibDebugMode) {
            logger.lifecycle("GTOLib: 【未加密】直接使用并保留 main Java/Kotlin 编译输出")
            return@doLast
        }
        listOf(classesJava, classesKotlin).forEach { delete(it.resolve("com/gtolib")) }
        requireJar(modJar, "gtolib-mod.jar")
        requireSealRuntime()
        runDirs.forEach { dir ->
            dir.mkdirs()
            modJar.copyTo(dir.resolve("gtolib-protected.jar"), overwrite = true)
            sealRuntimeLibsJar.copyTo(dir.resolve("gto-seal-runtime-1.0.jar"), overwrite = true)
        }
        logger.lifecycle("GTOLib: 【加密】独立 gtolib 模组 + 独立 Seal Runtime → ${runDirs.joinToString { it.parentFile.name }}")
    }
}

// 会拉起 MC 的任务都要先安装 gtolib，否则 main 明文 com.gtolib 与 run/mods 里的 gtolib 同时导出包（JPMS 冲突）。
// IDEA Gradle Sync 也会跑 prepareClientRun 建模，此时不能把 classes 依赖带进来，否则编译失败会阻断 IDE 解析依赖。
val isIdeaGradleSync = listOf(System.getProperty("idea.sync.active"), System.getProperty("idea.gradle.sync"), findProperty("idea.sync.active")?.toString())
    .any { it.equals("true", ignoreCase = true) }

fun isGtolibLaunchTask(t: Task): Boolean {
    val n = t.name
    return n in setOf("runClient", "runServer", "runData", "runGameTestServer") ||
        "devlaunch" in n || "DevLaunch" in n || "Main.main" in n || n.endsWith(".main()") || ".main_" in n ||
        n.startsWith("prepare") && n.endsWith("Run") || n.startsWith("run") && "forge" in n.lowercase()
}

val wiredLaunchTasks = mutableSetOf<String>()
fun wireGtolibInstallTo(t: Task) {
    if (isIdeaGradleSync || !isGtolibLaunchTask(t) || !wiredLaunchTasks.add(t.path)) return
    // IDEA 的临时 JavaExec 直接读 build/moddev/clientRun*Args.txt，clean 后要先重新生成
    if (t.name == "net.neoforged.devlaunch.Main.main()") t.dependsOn("prepareClientRun")
    t.dependsOn("installGtolibIntoModOutput")
    t.doFirst { logger.lifecycle("GTOLib run [${t.name}]: upToDate=$isGtolibJarUpToDate → ${if (isGtolibDebugMode) "未加密(unprotected)" else "加密(protected)"}") }
}
tasks.configureEach { wireGtolibInstallTo(this) }

// ---------- 发行 jar ----------

tasks.register("syncGtolibRefmaps") {
    group = "gtolib"
    description = "同步 Mixin refmap：编译 GTOLib 源码时从 AP 写入 resources；始终保证 build 资源与 jar 可消费"
    if (compileGtolibSources) dependsOn("classes")
    outputs.upToDateWhen { false }
    doLast {
        if (publishGtolibRefmaps(compileGtolibSources) == null && !compileGtolibSources) {
            val res = file("src/main/resources/gtolib.refmap.json")
            if (res.length() < 64) throw GradleException("未编译 GTOLib 源码时必须有预置 src/main/resources/gtolib.refmap.json（重建 gtolib 时写入）。")
        }
        val rm = mixinOut.resolve("gtolib.refmap.json").takeIf { it.exists() } ?: file("src/main/resources/gtolib.refmap.json")
        if (rm.length() > 64 && "ResourceLocationMixn" !in rm.readText()) {
            logger.warn("GTOLib refmap: 缺少 ResourceLocationMixn 映射（生产可能 Shadow 失败）— 请 -PgtolibRebuild=true 全量重编")
        }
    }
}

val dualModsToml = out("mods-dual.toml")
val writeDualModsToml = tasks.register("writeDualModsToml") {
    group = "gtolib"
    description = "写出玩家包双 modId mods.toml（gtocore + gtolib，保留完整依赖）"
    dependsOn("processResources")
    val expanded = resourcesMain.resolve("META-INF/mods.toml")
    inputs.file(expanded)
    outputs.file(dualModsToml)
    doLast {
        var text = expanded.readText()
        if ("modId=\"gtolib\"" !in text && "modId = \"gtolib\"" !in text) {
            val gtolibMod = "\n\n[[mods]]\nmodId=\"gtolib\"\nversion=\"${project.version}\"\ndisplayName=\"GTOLib\"\ndescription='''Seal-protected GTO library (bundled).'''\n"
            // [[mods]] 必须在 [[dependencies]] 之前
            val i = text.indexOf("[[dependencies")
            text = if (i >= 0) text.substring(0, i) + gtolibMod + text.substring(i) else text + gtolibMod
        }
        // datasynclib 由 jarJar 提供；deeperdarker 必须 AFTER，否则 sculk_stone 注册时 DD 未就绪
        listOf("datasynclib", "deeperdarker").filterNot { it in text }.takeIf { it.isNotEmpty() }?.let {
            throw GradleException("writeDualModsToml: mods.toml 模板缺少依赖 ${it.joinToString()}")
        }
        dualModsToml.writeText(text)
    }
}

fun readSealSalt(jar: File): Pair<String, String> {
    val mf = java.util.jar.Manifest((readJarEntry(jar, "META-INF/MANIFEST.MF") ?: ByteArray(0)).inputStream()).mainAttributes
    val salt = mf.getValue("NTPT-Salt") ?: mf.getValue("GTO-Seal-Salt") ?: throw GradleException("未从 ${jar.name} 读到 Seal Salt，Runtime 将无法解密")
    return salt to (mf.getValue("NTPT-Version") ?: mf.getValue("GTO-Seal-Version") ?: gtoSealMinVersion)
}

tasks.named<Jar>("jar") {
    // ensureMixinRefmapMappings 只挂 reobfJar（必须在 jar 之后补 tsrg，避免循环依赖）
    dependsOn(installGtolibIntoModOutput, "buildGtolibProtected", "syncGtolibRefmaps", writeDualModsToml)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // 不要在这里 exclude com/gtolib/**：顶层 exclude 作用于所有 from()，会把发行包里的 hollow class 一起丢掉
    exclude("META-INF/mods.toml")
    from({ if (releaseJar.isFile && releaseJar.length() > 0) zipTree(releaseJar) else emptyList<File>() }) {
        exclude("META-INF/mods.toml", "com/gtolib/GTOLibMod.class")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    // 明文 @Mod("gtolib")：Seal 抽空会丢注解，发行包必须带未加密入口
    from(gtolibModStubClass) {
        into("com/gtolib")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    from(dualModsToml) {
        into("META-INF")
        rename { "mods.toml" }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    doFirst {
        requireSealRuntime()
        if (isGtolibDebugMode) throw GradleException("明文模式（-PgtolibUnprotected 等）不能打包 gtocore jar：发行包只能嵌入加密的发行轨 gtolib")
        requireJar(releaseJar, "发行 jar 需要 build/gtolib/gtolib-release.jar（请 ./gradlew -PgtolibRebuild=true buildGtolibProtected）")
        verifyGtocoreImportsCoveredByMapping(parseGtolibReleaseMapping(releaseMappingFile))
        logger.lifecycle("GTO jar: 嵌入 gtolib 包体 ← ${releaseJar.name}")
        publishGtolibRefmaps(compileGtolibSources)
    }
    doLast {
        // 先按发行 mapping 改写 gtocore 侧对 com.gtolib 的引用（类名、成员、继承成员、类名字符串），
        // 再打入最终 jar 根目录唯一一份 refmap（含关键 Shadow 补丁）
        val outJar = archiveFile.get().asFile
        val mapping = parseGtolibReleaseMapping(releaseMappingFile)
        val remapper = prepareGtolibRefRemapper(outJar, releaseJar, mapping)
        var remapped = 0
        rewriteJar(outJar, extra = publishGtolibRefmaps(false) ?: throw GradleException("无法将 gtolib.refmap.json 注入发行 jar")) { entry, bytes ->
            if (entry.endsWith(".class") && !entry.startsWith("com/gtolib/")) {
                remapped++
                remapGtolibRefs(remapper, bytes)
            } else {
                bytes
            }
        }
        finishGtolibRefRemap(remapper, outJar, remapped)
        verifyNoOriginalGtolibNames(outJar, mapping)
        verifyGtolibSamTargets(outJar)
        verifyLibraryBackedInterfaceMethods(outJar, out("proguardGtolibRelease-extra.pro"), mapping)
        val text = readJarEntry(outJar, "gtolib.refmap.json")?.toString(Charsets.UTF_8).orEmpty()
        val levelTarget = levelMixinCriticalRefmap.values.first()
        if ("ResourceLocationMixn" !in text || levelTarget !in text) throw GradleException("发行 jar 内 gtolib.refmap.json 缺少关键 SRG 映射")
        logger.lifecycle("GTO jar: Seal 独立; refmap 已注入且含关键 SRG 映射；gtocore 引用面已 remap")
    }
}

// 主 jar 的 Manifest Salt 必须取自实际嵌入的发行包，与加密体 Salt 一致，否则 Runtime 解密必失败
tasks.withType<Jar>().matching { it.name == "jar" || it.name == "reobfJar" }.configureEach {
    doFirst {
        if (!releaseJar.isFile) return@doFirst
        val (salt, version) = readSealSalt(releaseJar)
        manifest.attributes("GTO-Seal" to "1", "GTO-Seal-Salt" to salt, "GTO-Seal-Version" to version, "NTPT-Salt" to salt, "NTPT-Version" to version)
        logger.lifecycle("GTO Seal: $name Manifest Salt 与发行包对齐")
    }
}
tasks.named("jar") { finalizedBy(stageSealRuntimeArtifact) }
tasks.named("build") { dependsOn(stageSealRuntimeArtifact) }

// Mixin AP 写出的 *.mappings.tsrg 决定 reobfJar/ART 能否把 mixin 成员改成 f_/m_；未编译 GTOLib 源码 → 无 AP → 从 resources 预置
val ensureMixinRefmapMappings = tasks.register("ensureMixinRefmapMappings") {
    group = "gtolib"
    // 必须在 jar 之后、reobfJar 之前；不可 dependsOn jar（jar 会间接依赖本任务链）
    mustRunAfter("classes", "jar")
    val gtocoreMap = mixinOut.resolve("gtocore.refmap.json.mappings.tsrg")
    val gtolibMap = mixinOut.resolve("gtolib.refmap.json.mappings.tsrg")
    val repoGtolibTsrg = file("src/main/resources/gtolib.refmap.json.mappings.tsrg")
    val repoGtocoreTsrg = file("src/main/resources/gtocore.refmap.json.mappings.tsrg")
    outputs.files(gtocoreMap, gtolibMap)
    doLast {
        mixinOut.mkdirs()
        if ((!compileGtolibSources || gtolibMap.length() < 32) && repoGtolibTsrg.isFile) {
            repoGtolibTsrg.copyTo(gtolibMap, overwrite = true)
            logger.lifecycle("Mixin: gtolib tsrg ← src/main/resources（未编译 GTOLib 源码）")
        }
        if (gtocoreMap.length() < 32 && repoGtocoreTsrg.isFile) repoGtocoreTsrg.copyTo(gtocoreMap, overwrite = true)
        if (gtocoreMap.length() < 32 && gtolibMap.length() > 0) gtolibMap.copyTo(gtocoreMap, overwrite = true)
        listOf(gtolibMap, gtocoreMap).forEach {
            if (it.length() < 32) it.writeText("")
            ensureResourceLocationMixnTsrg(it)
        }
        // 编译 GTOLib 源码时回写 resources，供下次未编译 GTOLib 源码时使用
        if (compileGtolibSources && gtolibMap.length() > 32) {
            gtolibMap.copyTo(repoGtolibTsrg, overwrite = true)
            gtocoreMap.copyTo(repoGtocoreTsrg, overwrite = true)
            logger.lifecycle("Mixin: 已同步 tsrg → src/main/resources")
        }
        publishGtolibRefmaps(compileGtolibSources)
        if (!gtolibMap.readText().let { "ResourceLocationMixn" in it && "m_245413_" in it }) {
            throw GradleException("gtolib.refmap.json.mappings.tsrg 缺少 ResourceLocationMixn/m_245413_，reobf 将失败")
        }
    }
}
tasks.matching { it.name == "reobfJar" }.configureEach {
    dependsOn("buildGtolibProtected", ensureMixinRefmapMappings, "jar")
    mustRunAfter("jar", ensureMixinRefmapMappings)
}

val verifyResourceLocationMixnReobf = tasks.register("verifyResourceLocationMixnReobf") {
    group = "gtolib"
    description = "校验 reobf 后 ResourceLocationMixn 已为 SRG 名"
    dependsOn("reobfJar")
    doLast {
        val libs = layout.buildDirectory.dir("libs").get().asFile
        val jar = libs.resolve("$modId-forge-1.20.1-${project.version}.jar").takeIf { it.exists() }
            ?: libs.listFiles()?.firstOrNull { it.name.startsWith("gtocore") && it.name.endsWith(".jar") && "sources" !in it.name && "javadoc" !in it.name }
            ?: throw GradleException("verifyResourceLocationMixnReobf: 找不到 gtocore jar")
        val text = latin1(readJarEntry(jar, "$resourceLocationMixnKey.class") ?: throw GradleException("reobf 产物缺少 ResourceLocationMixn.class"))
        if ("f_135804_" !in text || "m_245413_" !in text || "assertValidNamespace" in text) {
            throw GradleException("reobf 后 ResourceLocationMixn 仍非 SRG 名，检查 build/mixin/*.mappings.tsrg 是否含 ResourceLocationMixn")
        }
        logger.lifecycle("verify: ResourceLocationMixn SRG OK ← ${jar.name}")
    }
}
tasks.named("build") { dependsOn(verifyResourceLocationMixnReobf) }
tasks.matching { it.name == "signJar" }.configureEach { dependsOn(verifyResourceLocationMixnReobf) }
tasks.matching { it.name == "sourcesJar" }.configureEach { (this as Jar).exclude("com/gtolib/**", "**/GTOLib/**") }

// ---------- gtocore 发行代码保护 ----------
// reobfJar 之后、签名之前，把 jar 里 gtocore 自身类交给 ProGuard 改名；gtolib 发行包与资源只作 library 原样保留。

val gtocoreObfDir: File = layout.buildDirectory.dir("gtocore-obf").get().asFile
val gtocoreObfRules = file("gradle/scripts/proguard-gtocore-rules.pro")
val gtocoreMappingFile = gtocoreObfDir.resolve("gtocore-release-mapping.txt")
val gtocoreObfMarker = "GTO-Core-Obfuscated"
val gtocoreNameAnnotationPrefixes = listOf("Lcom/gto/datasynclib/", "Ldev/toma/configuration/", "Lcom/lowdragmc/lowdraglib/")
val gtocoreClassToken = Regex("""com[./](?:gtocore|hepdd)[./][A-Za-z0-9_$./]+""")
val fieldDescriptor = Regex("""^\[*(?:[BCDFIJSZ]|L[^;]+;)$""")
val srgMemberName = Regex("""^[mf]_\d+_""")

// 构建编号：写进发行 jar 的 MANIFEST 与两份 mapping 文件头，玩家日志/崩溃报告里的编号据此对回 mapping
val gtoBuildIdKey = "GTO-Build-Id"
val gtoBuildId: String = run {
    val sha = runCatching {
        val p = ProcessBuilder("git", "rev-parse", "--short=10", "HEAD").directory(rootDir).redirectErrorStream(true).start()
        p.inputStream.bufferedReader().readText().trim().takeIf { p.waitFor() == 0 && it.isNotEmpty() }
    }.getOrNull() ?: "nogit"
    val runId = System.getenv("GITHUB_RUN_ID")
    if (!runId.isNullOrBlank()) {
        "ci-$runId.${System.getenv("GITHUB_RUN_ATTEMPT") ?: "1"}-$sha"
    } else {
        "local-${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))}-$sha"
    }
}

fun stampMappingBuildId(file: File) {
    requireJar(file, "构建编号: mapping")
    val body = file.readLines(Charsets.UTF_8).dropWhile { it.startsWith("# $gtoBuildIdKey:") }
    file.writeText((listOf("# $gtoBuildIdKey: $gtoBuildId") + body).joinToString("\n", postfix = "\n"), Charsets.UTF_8)
}

fun gtocoreReleaseJar(): File = layout.buildDirectory.dir("libs").get().asFile.resolve("$modId-forge-1.20.1-${project.version}.jar").takeIf { it.isFile }
    ?: throw GradleException("找不到 reobf 后的 gtocore 发行 jar: build/libs/$modId-forge-1.20.1-${project.version}.jar")

fun isGtocoreProgramEntry(name: String) = name.endsWith(".class") && !name.startsWith("com/gtolib/") && !name.startsWith("META-INF/")

fun readJarManifest(jar: File) = java.util.jar.Manifest((readJarEntry(jar, "META-INF/MANIFEST.MF") ?: ByteArray(0)).inputStream())

fun readClassNodes(jar: File): Map<String, ClassNode> = ZipFile(jar).use { zf ->
    zf.entries().asSequence().filter { it.name.endsWith(".class") }.associate { e ->
        val node = ClassNode(Opcodes.ASM9)
        ClassReader(zf.getInputStream(e).readBytes()).accept(node, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        node.name to node
    }
}

fun annotationValueStrings(value: Any?): Sequence<String> = when (value) {
    is String -> sequenceOf(value)
    is AnnotationNode -> annotationValueStrings(value.values)
    is List<*> -> value.asSequence().flatMap { annotationValueStrings(it) }
    else -> emptySequence()
}

fun annotationStrings(vararg lists: List<AnnotationNode>?): Sequence<String> =
    lists.asSequence().flatMap { it.orEmpty().asSequence() }.flatMap { annotationValueStrings(it.values) }

fun hasNameAnnotation(vararg lists: List<AnnotationNode>?) =
    lists.any { list -> list.orEmpty().any { a -> gtocoreNameAnnotationPrefixes.any { a.desc.startsWith(it) } } }

fun classTokenCandidates(token: String): Sequence<String> {
    val path = token.replace('.', '/').trimEnd('/')
    return generateSequence(path) { p -> p.lastIndexOf('/').takeIf { it > 0 }?.let { p.substring(0, it) } }
}

fun memberKeptName(key: String, obf: String): Boolean {
    if (!key.startsWith(obf)) return false
    val rest = key.substring(obf.length)
    return rest.startsWith("(") || fieldDescriptor.matches(rest)
}

fun writeGtocoreKeepRules(program: Map<String, ClassNode>, target: File): Set<String> {
    requireJar(proguardReleaseJar, "gtocore 代码保护: GTOLib 发行轨 ProGuard 产物（-PgtolibRebuild=true 重建）", 1024)
    val keepClasses = sortedSetOf<String>()
    fun keepWithAncestors(name: String) {
        val node = program[name] ?: return
        if (!keepClasses.add(name)) return
        node.superName?.let(::keepWithAncestors)
        node.interfaces.forEach(::keepWithAncestors)
    }
    val collector = object : Remapper() {
        override fun map(internalName: String): String {
            keepWithAncestors(internalName)
            return internalName
        }
    }
    ZipFile(proguardReleaseJar).use { zf ->
        zf.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { e ->
            val bytes = zf.getInputStream(e).readBytes()
            ClassReader(bytes).accept(ClassRemapper(ClassWriter(0), collector), 0)
            gtocoreClassToken.findAll(latin1(bytes)).forEach { m ->
                val path = m.value.replace('.', '/')
                val exact = classTokenCandidates(m.value).firstOrNull { it in program }
                if (exact != null) {
                    keepWithAncestors(exact)
                } else if (!path.endsWith('/')) {
                    program.keys.filter { it.startsWith(path) && it.indexOf('/', path.length) < 0 }.forEach(::keepWithAncestors)
                }
            }
        }
    }
    val fromGtolib = keepClasses.size
    program.values.filter { it.name.startsWith("com/gtocore/mixin/") }.forEach { n ->
        n.superName?.let(::keepWithAncestors)
        n.interfaces.forEach(::keepWithAncestors)
    }
    val nestHosts = (keepClasses.asSequence().mapNotNull { program[it] } + program.values.asSequence().filter { n -> n.methods.any { it.access and Opcodes.ACC_NATIVE != 0 } })
        .mapNotNull { it.nestHostClass }
        .filter { it in program && it !in keepClasses }
        .toSortedSet()

    val memberKeeps = sortedMapOf<String, MutableSet<String>>()
    fun keepMember(owner: String, spec: String) = memberKeeps.getOrPut(owner) { sortedSetOf() }.add(spec)
    fun programHierarchy(start: String): Collection<ClassNode> {
        val seen = LinkedHashMap<String, ClassNode>()
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val node = program[queue.removeFirst()] ?: continue
            if (seen.put(node.name, node) != null) continue
            node.superName?.let(queue::add)
            queue.addAll(node.interfaces)
        }
        return seen.values
    }
    program.values.forEach { n ->
        n.fields.filter { hasNameAnnotation(it.visibleAnnotations, it.invisibleAnnotations) }.forEach { keepMember(n.name, "*** ${it.name};") }
        n.methods.filter { it.name[0] != '<' && hasNameAnnotation(it.visibleAnnotations, it.invisibleAnnotations) }.forEach { keepMember(n.name, "*** ${it.name}(...);") }
        val strings = (
            annotationStrings(n.visibleAnnotations, n.invisibleAnnotations) +
                n.fields.asSequence().flatMap { annotationStrings(it.visibleAnnotations, it.invisibleAnnotations) } +
                n.methods.asSequence().flatMap { annotationStrings(it.visibleAnnotations, it.invisibleAnnotations) }
            ).toSet()
        if (strings.isEmpty()) return@forEach
        programHierarchy(n.name).forEach { owner ->
            owner.fields.filter { it.name in strings }.forEach { keepMember(owner.name, "*** ${it.name};") }
            owner.methods.filter { it.name[0] != '<' && it.name in strings }.forEach { keepMember(owner.name, "*** ${it.name}(...);") }
        }
    }

    target.writeText(
        "# generated: GTOLib 引用 / mixin 祖先整类保名；名字型注解与注解字符串引用的成员保名\n" +
            keepClasses.joinToString("") { "-keep class ${it.replace('/', '.')} { *; }\n" } +
            nestHosts.joinToString("") { "-keepnames class ${it.replace('/', '.')}\n" } +
            memberKeeps.entries.joinToString("") { (owner, specs) ->
                "-keepclassmembernames class ${owner.replace('/', '.')} {\n" + specs.joinToString("") { "    $it\n" } + "}\n"
            }
    )
    logger.lifecycle(
        "gtocore 代码保护: 整类保名 ${keepClasses.size}（GTOLib 引用 $fromGtolib，mixin 祖先 ${keepClasses.size - fromGtolib}），同 nest 外部类保类名 ${nestHosts.size}，" +
            "成员保名 ${memberKeeps.values.sumOf { it.size }} 项 / ${memberKeeps.size} 类 → ${target.name}"
    )
    return keepClasses
}

/**
 * gtocore 禁止使用 @SubscribeEvent：EventBus.register(Object) 为每个 @SubscribeEvent 方法生成一个 IEventListener 子类，
 * 类名由 IEventListenerFactory.getUniqueName 拼成 `<包名>.__<类简单名>_<方法名>_<事件类简单名>`（不含参数描述符，
 * 源码标记 .dynamic）。改名后两个参数不同、方法名与事件简单名相同的方法（ScreenEvent$MouseDragged$Pre 与
 * ScreenEvent$MouseButtonReleased$Pre 的简单名都是 Pre）落到同一混淆名、解析到同一生成类 → invoke 的 CHECKCAST
 * 对不上 → 运行时 ClassCastException。
 * 统一改成显式注册：MinecraftForge.EVENT_BUS.addListener(...) / FMLJavaModLoadingContext.get().getModEventBus().addListener(...)
 * ——该路径用 NamedEventListener 包装，不派生子类。
 * mixin 包除外：它的处理器方法要合并进被注入的类，且 mixin 类在保护里整类保名。
 */
fun verifyNoSubscribeEvent(program: Map<String, ClassNode>) {
    val hits = linkedSetOf<String>()
    program.values.forEach { node ->
        // 随包发布的 com.hepdd 与 gtocore 同属 program、一起改名，同受此约束；mixin 包除外。
        val owned = node.name.startsWith("com/gtocore/") || node.name.startsWith("com/hepdd/")
        if (!owned || node.name.startsWith("com/gtocore/mixin/")) return@forEach
        node.methods.forEach { m ->
            val annotated = (m.visibleAnnotations.orEmpty() + m.invisibleAnnotations.orEmpty())
                .any { it.desc == "Lnet/minecraftforge/eventbus/api/SubscribeEvent;" }
            if (annotated) hits += "${node.name.replace('/', '.')}.${m.name}"
        }
    }
    if (hits.isNotEmpty()) {
        throw GradleException(
            "gtocore 代码保护: ${hits.size} 处仍在使用 @SubscribeEvent（保护改名后 EventBus 生成的监听器类名会撞车）。" +
                "改用 MinecraftForge.EVENT_BUS.addListener(...) / FMLJavaModLoadingContext.get().getModEventBus().addListener(...) 显式注册:\n" +
                hits.take(20).joinToString("\n") { "  - $it" }
        )
    }
    logger.lifecycle("gtocore 代码保护: 无 @SubscribeEvent 用法 OK（扫描 ${program.size} class）")
}

fun verifyGtocoreLibraryOverrides(program: Map<String, ClassNode>, libs: Collection<File>, mapping: GtolibReleaseMapping) {
    val location = HashMap<String, File>()
    libs.filter { it.isFile && it.name.endsWith(".jar") }.forEach { jar ->
        ZipFile(jar).use { zf -> zf.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { location.putIfAbsent(it.name.removeSuffix(".class"), jar) } }
    }
    val opened = HashMap<File, ZipFile>()
    val cache = HashMap<String, ClassNode?>()
    fun libraryNode(name: String): ClassNode? = cache.getOrPut(name) {
        val jar = location[name] ?: return@getOrPut null
        val zf = opened.getOrPut(jar) { ZipFile(jar) }
        ClassNode(Opcodes.ASM9).also { ClassReader(zf.getInputStream(zf.getEntry("$name.class")).readBytes()).accept(it, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES) }
    }
    val broken = linkedSetOf<String>()
    var checked = 0
    try {
        program.values.forEach { cls ->
            val table = mapping.members[cls.name] ?: return@forEach
            val renamed = cls.methods.asSequence()
                .filter { it.name[0] != '<' && it.access and (Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC) == 0 }
                .map { it.name + it.desc }
                .filter { key -> table[key]?.let { !memberKeptName(key, it) } == true }
                .toSet()
            if (renamed.isEmpty()) return@forEach
            checked += renamed.size
            val seen = HashSet<String>()
            val queue = ArrayDeque<String>()
            cls.superName?.let(queue::add)
            queue.addAll(cls.interfaces)
            while (queue.isNotEmpty()) {
                val name = queue.removeFirst()
                if (!seen.add(name)) continue
                val node = program[name] ?: libraryNode(name) ?: continue
                if (name !in program) {
                    node.methods.forEach { m ->
                        if (m.access and (Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC) == 0 && (m.name + m.desc) in renamed) broken += "${cls.name}.${m.name}${m.desc} 覆盖 library $name 却被改名"
                    }
                }
                node.superName?.let(queue::add)
                queue.addAll(node.interfaces)
            }
        }
    } finally {
        opened.values.forEach { it.close() }
    }
    if (broken.isNotEmpty()) {
        throw GradleException("gtocore 代码保护: ${broken.size} 个覆盖 library 的方法被改名（运行时会 AbstractMethodError）:\n" + broken.take(15).joinToString("\n") { "  - $it" })
    }
    logger.lifecycle("gtocore 代码保护: library 覆盖校验 OK（改名方法 $checked 个，library 索引 ${location.size} class）")
}

fun internalPackage(name: String) = name.substringBeforeLast('/', "")

fun verifyGtocoreNoNameClash(mapping: GtolibReleaseMapping, renamed: Set<String>, gtolibJar: File, releaseJar: File) {
    val gtolibNames = ZipFile(gtolibJar).use { zf -> zf.entries().asSequence().filter { it.name.endsWith(".class") }.map { it.name.removeSuffix(".class") }.toHashSet() }
    jarEntryNames(releaseJar).filter { it.startsWith(SEAL_BIN_DIR) && it.endsWith(".prod.bin") }
        .mapTo(gtolibNames) { it.removePrefix(SEAL_BIN_DIR).removeSuffix(".prod.bin") }
    val newNames = renamed.map { mapping.classNames.getValue(it) }
    val clash = newNames.filter { it in gtolibNames || it in mapping.classNames } + newNames.groupBy { it }.filterValues { it.size > 1 }.keys
    if (clash.isNotEmpty()) {
        throw GradleException("gtocore 代码保护: ${clash.size} 个 gtocore 新类名与已有类名重名（GTOLib 发行包或 gtocore 原有类）:\n" + clash.take(15).joinToString("\n") { "  - $it" })
    }
    logger.lifecycle("gtocore 代码保护: 新类名不重名 OK（改名 ${newNames.size} 类，对照 GTOLib ${gtolibNames.size} 个名字、gtocore 原有 ${mapping.classNames.size} 类）")
}

fun verifyGtocoreNestPackages(obfNodes: Map<String, ClassNode>) {
    val broken = obfNodes.values.flatMap { n ->
        (listOfNotNull(n.nestHostClass) + n.nestMembers.orEmpty())
            .filter { internalPackage(it) != internalPackage(n.name) }
            .map { "${n.name} ↔ $it" }
    }
    if (broken.isNotEmpty()) {
        throw GradleException("gtocore 代码保护: ${broken.size} 处同一 nest 的类不在同一个包（运行时私有访问 IllegalAccessError）:\n" + broken.take(15).joinToString("\n") { "  - $it" })
    }
    logger.lifecycle("gtocore 代码保护: nest 同包 OK（${obfNodes.values.count { it.nestHostClass != null }} 个嵌套类）")
}

fun verifyNoOriginalGtocoreNames(jar: File, renamed: Set<String>, obfEntries: Set<String>) {
    val hits = linkedSetOf<String>()
    ZipFile(jar).use { zf ->
        zf.entries().asSequence().filter { e ->
            e.name !in obfEntries && !isGtocoreProgramEntry(e.name) && !e.isDirectory && !e.name.endsWith(".jar") &&
                (e.name.endsWith(".class") || e.name.startsWith("META-INF/") || e.name.startsWith("coremods/") || '/' !in e.name)
        }.forEach { e ->
            gtocoreClassToken.findAll(latin1(zf.getInputStream(e).readBytes())).forEach { m ->
                classTokenCandidates(m.value).firstOrNull { it in renamed }?.let { hits += "${e.name} → $it" }
            }
        }
    }
    if (hits.isNotEmpty()) {
        throw GradleException("gtocore 代码保护: ${hits.size} 处未随改名同步的原始类名引用（补 proguard-gtocore-rules.pro 的 keep）:\n" + hits.take(20).joinToString("\n") { "  - $it" })
    }
}

val obfuscateGtocoreRelease = tasks.register<JavaExec>("obfuscateGtocoreRelease") {
    group = "gtolib"
    description = "发行 jar：对 gtocore 自身类名与成员名做代码保护（gtolib 发行包与资源原样保留）"
    dependsOn("reobfJar")
    onlyIf { !isGtolibDebugMode }
    outputs.upToDateWhen { false }
    classpath = proguardCp
    mainClass = "proguard.ProGuard"
    maxHeapSize = "4g"
    val toolOutput = captureToolOutput()
    val programJar = gtocoreObfDir.resolve("program.jar")
    val embeddedLibraryJar = gtocoreObfDir.resolve("embedded-library.jar")
    val obfJar = gtocoreObfDir.resolve("program-obf.jar")
    val generatedKeep = gtocoreObfDir.resolve("keep-generated.pro")
    var program: Map<String, ClassNode> = emptyMap()
    var keptClasses: Set<String> = emptySet()
    var libs: Collection<File> = emptyList()
    doFirst {
        val jar = gtocoreReleaseJar()
        if (readJarManifest(jar).mainAttributes.getValue(gtocoreObfMarker) != null) {
            throw GradleException("${jar.name} 已经过代码保护，重新运行 reobfJar 后再处理（例如 clean build）")
        }
        gtocoreObfDir.deleteRecursively()
        gtocoreObfDir.mkdirs()
        ZipFile(jar).use { zf ->
            ZipOutputStream(programJar.outputStream().buffered()).use { programOut ->
                ZipOutputStream(embeddedLibraryJar.outputStream().buffered()).use { libraryOut ->
                    zf.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { e ->
                        val target = if (isGtocoreProgramEntry(e.name)) programOut else libraryOut
                        target.putNextEntry(ZipEntry(e.name))
                        target.write(zf.getInputStream(e).readBytes())
                        target.closeEntry()
                    }
                }
            }
        }
        program = readClassNodes(programJar)
        if (program.isEmpty()) throw GradleException("gtocore 代码保护: ${jar.name} 里没有 gtocore 类")
        verifyNoSubscribeEvent(program)
        keptClasses = writeGtocoreKeepRules(program, generatedKeep)
        libs = proguardLibraryJars(true) + embeddedLibraryJar
        val extra = writeProguardExtraFile(name, libs, gtocoreMappingFile, gtocoreObfDir)
        setArgs(listOf(gtocoreObfRules, generatedKeep, extra).map { "@${it.absolutePath}" } + listOf("-injars", programJar.absolutePath, "-outjars", obfJar.absolutePath))
    }
    doLast {
        finishToolOutput(name, executionResult.get().exitValue, toolOutput)
        requireJar(obfJar, "gtocore 代码保护输出", 1024)
        val mapping = parseGtolibReleaseMapping(gtocoreMappingFile)
        val renamedClasses = mapping.classNames.filter { (original, obf) -> original != obf }.keys
        val renamedMembers = mapping.members.values.sumOf { table -> table.count { (key, obf) -> !memberKeptName(key, obf) } }
        if (renamedClasses.isEmpty()) throw GradleException("gtocore 代码保护未生效：没有类被改名，检查 proguard-gtocore-rules.pro 的 keep 面")
        val srgRenamed = mapping.members.flatMap { (owner, table) ->
            table.filter { (key, obf) -> srgMemberName.containsMatchIn(key) && !memberKeptName(key, obf) }.map { "$owner.${it.key} → ${it.value}" }
        }
        if (srgRenamed.isNotEmpty()) throw GradleException("gtocore 代码保护错误重命名了 Minecraft 成员:\n  " + srgRenamed.take(12).joinToString("\n  "))
        val keptBroken = keptClasses.filter { c -> mapping.classNames[c] != c || mapping.members[c].orEmpty().any { (key, obf) -> !memberKeptName(key, obf) } }
        if (keptBroken.isNotEmpty()) throw GradleException("gtocore 代码保护: GTOLib 引用的 gtocore 类被改名:\n  " + keptBroken.take(12).joinToString("\n  "))
        verifyGtocoreLibraryOverrides(program, libs, mapping)

        val jar = gtocoreReleaseJar()
        val obfClasses = ZipFile(obfJar).use { zf -> zf.entries().asSequence().filter { it.name.endsWith(".class") }.associate { it.name to zf.getInputStream(it).readBytes() } }
        if (obfClasses.size != program.size) throw GradleException("gtocore 代码保护: 输出 ${obfClasses.size} 个类，输入 ${program.size} 个")
        verifyGtocoreNoNameClash(mapping, renamedClasses, embeddedLibraryJar, jar)
        verifyGtocoreNestPackages(readClassNodes(obfJar))
        val flattened = renamedClasses.filter { internalPackage(mapping.classNames.getValue(it)) == "com/gtocore" }
        val strayed = renamedClasses.filter { internalPackage(mapping.classNames.getValue(it)).let { p -> p != "com/gtocore" && p != internalPackage(it) } }
        if (strayed.isNotEmpty()) throw GradleException("gtocore 代码保护: ${strayed.size} 个类被挪进了 com.gtocore 以外的包:\n  " + strayed.take(12).joinToString("\n  "))
        val liveFiles = jarEntryNames(jar).filterTo(HashSet()) { !it.endsWith("/") && !isGtocoreProgramEntry(it) } + obfClasses.keys
        val liveDirs = liveFiles.flatMapTo(HashSet()) { f -> generateSequence(f.substringBeforeLast('/', "")) { it.substringBeforeLast('/', "").ifEmpty { null } }.filter { it.isNotEmpty() }.map { "$it/" } }
        var emptyDirs = 0
        rewriteJar(jar, extra = obfClasses) { entry, bytes ->
            when {
                isGtocoreProgramEntry(entry) -> null
                entry.endsWith("/") && (entry.startsWith("com/gtocore/") || entry.startsWith("com/hepdd/")) && entry !in liveDirs -> null.also { emptyDirs++ }
                entry == "META-INF/MANIFEST.MF" -> {
                    val mf = java.util.jar.Manifest(bytes.inputStream())
                    mf.mainAttributes.putValue(gtocoreObfMarker, "1")
                    mf.mainAttributes.putValue(gtoBuildIdKey, gtoBuildId)
                    java.io.ByteArrayOutputStream().also { mf.write(it) }.toByteArray()
                }
                else -> bytes
            }
        }
        verifyNoOriginalGtocoreNames(jar, renamedClasses, obfClasses.keys)
        val lvt = obfClasses.count { (entry, bytes) -> !entry.startsWith("com/gtocore/mixin/") && "LocalVariableTable" in latin1(bytes) }
        if (lvt > 0) throw GradleException("gtocore 代码保护: $lvt 个非 mixin 类仍带 LocalVariableTable")
        logger.lifecycle(
            "gtocore 代码保护 OK: ${program.size} class，改名类 ${renamedClasses.size}、改名成员 $renamedMembers，整类保名 ${keptClasses.size}，" +
                "平铺进 com.gtocore ${flattened.size}（原 com.gtocore ${flattened.count { it.startsWith("com/gtocore/") }}、原 com.hepdd ${flattened.count { it.startsWith("com/hepdd/") }}），" +
                "留在原包 ${program.size - flattened.size}，移除空目录 $emptyDirs → ${jar.name}"
        )

        if (readJarManifest(jar).mainAttributes.getValue(gtoBuildIdKey) != gtoBuildId) throw GradleException("构建编号: ${jar.name} 的 MANIFEST 缺少 $gtoBuildIdKey")
        listOf(gtocoreMappingFile, releaseMappingFile).forEach(::stampMappingBuildId)
        logger.lifecycle(
            "GTO 构建编号 $gtoBuildId → jar build/libs/${jar.name}；mapping ${gtocoreMappingFile.relativeTo(projectDir).invariantSeparatorsPath}、" +
                releaseMappingFile.relativeTo(projectDir).invariantSeparatorsPath
        )
    }
}
tasks.matching { it.name == "reobfJar" }.configureEach { finalizedBy(obfuscateGtocoreRelease) }
tasks.matching { it.name == "signJar" || it.name == "verifyResourceLocationMixnReobf" }.configureEach { dependsOn(obfuscateGtocoreRelease) }
tasks.named("build") { dependsOn(obfuscateGtocoreRelease) }
