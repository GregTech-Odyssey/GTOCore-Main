/**
 * GTOLib 流水线：源码 → dev 轨 build/gtolib/gtolib-protected.jar（keepnames，本地运行与编译）+ 发行轨 build/gtolib/gtolib-release.jar（混淆，嵌入玩家 jar）。
 * 明文开关 -PgtolibUnprotected=true | -PgtolibDebug=true | -PskipGtolibProtect=true（或 env GTOLIB_DEBUG / PROGUARD_DISABLE / .gtolib-debug）；强制重建 -PgtolibRebuild=true。
 */

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
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
    dependencies { classpath("org.ow2.asm:asm-tree:9.7.1") }
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
val gtmthingsNamedLibraryJar = out("gtmthings-named-library.jar")
val gtmthingsSrgLibraryJar = out("gtmthings-srg-library.jar")
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

/** dev 轨也是缓存命中时的编译 classpath，hollow API 必须是 named。 */
fun verifyDevCompileNamespace(jar: File, label: String) {
    val text = latin1(readJarEntry(jar, "com/gtolib/api/misc/FastSavedData.class") ?: throw GradleException("$label: 缺少 FastSavedData hollow class"))
    if ("m_7176_" in text || "m_77757_" in text || "save" !in text) {
        throw GradleException("$label: FastSavedData hollow API 不是 named，无法供 javac 使用")
    }
}

val gtolibImportRegex = Regex("""^import\s+(static\s+)?(com\.gtolib\.[A-Za-z0-9_.*]+)\s*;?\s*$""")

fun gtocoreSourceImports(): Sequence<MatchResult> =
    listOf(file("src/main/java"), file("src/main/kotlin")).filter { it.exists() }.asSequence()
        .flatMap { it.walkTopDown().filter { f -> f.isFile && (f.name.endsWith(".java") || f.name.endsWith(".kt")) } }
        .flatMap { it.readLines().asSequence() }
        .mapNotNull { gtolibImportRegex.find(it.trim()) }

/** gtocore 主源码 import 的 com.gtolib 类型必须能在 jar 里按名找到；发行轨混淆 keep 面不足时在这里报错。 */
fun verifyMainGtolibImportsAgainstJar(jar: File) {
    requireJar(jar, "verifyMainGtolibImportsAgainstJar")
    val entries = jarEntryNames(jar)
    fun hasClass(fqn: String) = fqn.replace('.', '/') + ".class" in entries
    fun hasPackageOrClass(fqn: String) = hasClass(fqn) || (fqn.replace('.', '/') + "/").let { p -> entries.any { it.startsWith(p) && it.endsWith(".class") } }
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
            "GTOLib API 校验失败：gtocore 主源码 import 了 ${jar.name} 中不存在的 com.gtolib 类型（通常是发行混淆 keep 面不足）。\n" +
                "缺失 ${missing.size} 项：\n" + missing.take(30).joinToString("\n") { "  - $it" }
        )
    }
    logger.lifecycle("GTOLib API 校验 OK: 主源码 com.gtolib import $scanned 条均可在 ${jar.name} 解析")
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

val reobfGtolib = tasks.register<JavaExec>("reobfGtolib") {
    group = "gtolib"
    description = "ART 把 named 明文 reobf 成 SRG（发行轨 ProGuard/Seal 的输入）"
    onlyIf { rebuildGtolib }
    dependsOn(packageGtolibJar, "classes", "createMinecraftArtifacts")
    classpath = artTool
    mainClass = "net.neoforged.art.Main"
    doFirst {
        requireJar(namedToIntermediate, "reobfGtolib: named→SRG 映射（先运行 createMinecraftArtifacts）")
        val mixinTsrg = mixinOut.resolve("gtolib.refmap.json.mappings.tsrg")
        val repoTsrg = file("src/main/resources/gtolib.refmap.json.mappings.tsrg")
        if ((!mixinTsrg.isFile || mixinTsrg.length() < 32) && repoTsrg.isFile) repoTsrg.copyTo(mixinTsrg, overwrite = true)
        ensureResourceLocationMixnTsrg(mixinTsrg)
        srgJar.delete()
        // 必须带上继承库，否则 gtocore 里重写的 Minecraft 方法（如 getSerializedName → m_7912_）不会被改名
        val libs = (listOf(namedForgeJar, classesJava, classesKotlin) +
            configurations["runtimeClasspath"].files.filter { f ->
                val n = f.name.lowercase()
                n.endsWith(".jar") && listOf("gtceu", "gtmthings", "datasynclib", "ae2", "appliedenergistics", "ex-pattern-provider").any { it in n }
            }).filter { it.exists() }.distinct()
        setArgs(
            listOf("--input", unprotectedJar.absolutePath, "--output", srgJar.absolutePath, "--names", namedToIntermediate.absolutePath, "--names", mixinTsrg.absolutePath) +
                libs.flatMap { listOf("--lib", it.absolutePath) }
        )
    }
    doLast {
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

val prepareGtmthingsNamedLibrary = tasks.register<Jar>("prepareGtmthingsNamedLibrary") {
    group = "gtolib"
    description = "只打包主工程内的 GTMThings 类，供 GTOLib ProGuard 识别父类"
    onlyIf { rebuildGtolib }
    dependsOn("classes")
    archiveFileName = gtmthingsNamedLibraryJar.name
    destinationDirectory = gtolibOut
    from(classesJava) { include("com/hepdd/gtmthings/**") }
    from(classesKotlin) { include("com/hepdd/gtmthings/**") }
}

val prepareGtmthingsSrgLibrary = tasks.register<JavaExec>("prepareGtmthingsSrgLibrary") {
    group = "gtolib"
    description = "GTMThings 父类库转成 SRG，供发行 ProGuard 使用"
    onlyIf { rebuildGtolib }
    dependsOn(prepareGtmthingsNamedLibrary, packageGtolibJar, "createMinecraftArtifacts")
    art(
        "--input", gtmthingsNamedLibraryJar.absolutePath, "--output", gtmthingsSrgLibraryJar.absolutePath,
        "--names", namedToIntermediate.absolutePath, "--lib", namedForgeJar.absolutePath, "--lib", unprotectedJar.absolutePath,
    )
    doFirst { gtmthingsSrgLibraryJar.delete() }
    doLast {
        val converted = ZipFile(gtmthingsSrgLibraryJar).use { zf ->
            zf.entries().asSequence().any { it.name.endsWith(".class") && Regex("m_\\d+_").containsMatchIn(latin1(zf.getInputStream(it).readBytes())) }
        }
        if (!converted) throw GradleException("prepareGtmthingsSrgLibrary: GTMThings 类库未转成 SRG")
    }
}

// ---------- ProGuard ----------

/** 生成 ProGuard 附加配置：libraryjars + 每次构建随机打乱的混淆字典 + mapping 输出 */
fun writeProguardExtraFile(taskName: String, srgInput: Boolean, mapping: File): File {
    val dict = protectDir.resolve("my_dict.txt").readLines()
    val rnd = SecureRandom()
    val dicts = listOf("class", "package", "member").associateWith { kind ->
        out("$taskName-$kind-dict.txt").also { it.writeText(dict.shuffled(rnd).joinToString("\n") + "\n") }
    }
    val libs = linkedSetOf<File>()
    File(System.getProperty("java.home")).let { home -> listOf(home.resolve("lib/rt.jar"), home.resolve("jmods/java.base.jmod")).firstOrNull { it.exists() } }?.let(libs::add)
    if (srgInput) {
        requireJar(forgeSrgLibraryJar, "GTOLib ProGuard: SRG Forge library（先运行 prepareForgeSrgLibrary）", 1024)
        libs += forgeSrgLibraryJar
    }
    (if (srgInput) gtmthingsSrgLibraryJar else gtmthingsNamedLibraryJar).also { requireJar(it, "GTOLib ProGuard: GTMThings 父类库", 1024) }.let(libs::add)
    configurations.filter { it.name.endsWith("Classpath") && it.isCanBeResolved }.forEach { cfg ->
        runCatching { cfg.resolve() }.getOrNull()?.forEach { f ->
            // SRG 输入不能再混进 named Forge jar，否则 ProGuard 按 classpath 顺序解析到错误的父类方法名
            if (f.name.endsWith(".jar") && !(srgInput && f.name == namedForgeJar.name)) libs += f.canonicalFile
        }
    }
    fun path(f: File) = "'${f.absolutePath.replace('\\', '/')}'"
    return out("$taskName-extra.pro").also { extraFile ->
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

/** gtocore 字节码与源码 import 引用的 com.gtolib 类是发行轨的链接面，必须保住类名与 public/protected 成员名。 */
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
    target.writeText("# generated: gtocore 字节码+import 对 com.gtolib 的引用面（发行单轨保名）\n" + keeps.joinToString("") { "-keepnames class $it { public protected *; }\n" })
    logger.lifecycle("GTOLib release: gtocore 引用面 ${keeps.size} 项 → ${target.name}（扫描 $scanned class）")
}

fun registerProguard(name: String, desc: String, input: File, output: File, srgInput: Boolean, mapping: File, rules: () -> List<File>, verify: () -> Unit) =
    tasks.register<JavaExec>(name) {
        group = "gtolib"
        description = desc
        onlyIf { rebuildGtolib }
        outputs.upToDateWhen { false }
        classpath = proguardCp
        mainClass = "proguard.ProGuard"
        doFirst {
            requireJar(input, "$name 输入")
            output.delete()
            val extra = writeProguardExtraFile(name, srgInput, mapping)
            setArgs((rules() + extra).map { "@${it.absolutePath}" } + listOf("-injars", input.absolutePath, "-outjars", output.absolutePath))
        }
        doLast {
            requireJar(output, "$name 输出", 1024)
            verify()
        }
    }

val proguardGtolibDev = registerProguard(
    "proguardGtolibDev", "dev 轨 ProGuard：named 输入、保留全部名字，只做方法体内优化",
    unprotectedJar, proguardDevJar, srgInput = false, mapping = out("gtolib-dev-mapping.txt"),
    rules = { listOf(protectDir.resolve("proguard-rules.pro")) },
) {
    verifyMixinNamespace(proguardDevJar, true, "GTOLib dev 轨 ProGuard")
    logger.lifecycle("GTOLib dev ProGuard OK → ${proguardDevJar.name}")
}
proguardGtolibDev { dependsOn(packageGtolibJar, "classes", prepareGtmthingsNamedLibrary) }

val proguardGtolibRelease = registerProguard(
    "proguardGtolibRelease", "发行轨 ProGuard：SRG 输入，内部类名/包名混淆",
    srgJar, proguardReleaseJar, srgInput = true, mapping = releaseMappingFile,
    rules = { listOf(protectDir.resolve("proguard-release-rules.pro"), out("proguard-release-keep.pro").also(::writeReleaseKeepFromGtocoreRefs)) },
) {
    // SRG 输入必须配 SRG libraryjars，否则 Minecraft override 会被当普通方法改名（运行时 AbstractMethodError）
    val srgMethod = Regex("""\b(m_\d+_)\([^)]*\)\s+->\s+(\S+)\s*$""")
    val srgLines = releaseMappingFile.readLines().mapNotNull { srgMethod.find(it) }
    val renamed = srgLines.filter { it.groupValues[1] != it.groupValues[2] }
    if (srgLines.isEmpty()) throw GradleException("GTOLib release mapping 中没有 SRG 方法，无法验证继承契约")
    if (renamed.isNotEmpty()) throw GradleException("GTOLib release ProGuard 错误重命名了 Minecraft override:\n  " + renamed.take(12).joinToString("\n  ") { "${it.groupValues[1]} → ${it.groupValues[2]}" })
    // 混淆必须真的发生：与 named 明文比对类路径集合
    fun classes(jar: File) = jarEntryNames(jar).filter { it.startsWith("com/gtolib") && it.endsWith(".class") }.toSet()
    val rel = classes(proguardReleaseJar)
    val dev = classes(unprotectedJar)
    val hidden = dev - rel
    if ((rel - dev).isEmpty() || hidden.isEmpty()) throw GradleException("proguardGtolibRelease: 类名混淆未生效，检查 proguard-release-rules.pro 的 keep 面是否过宽")
    logger.lifecycle("GTOLib release ProGuard OK: ${rel.size} class，改名/隐藏 ${hidden.size}，保名 ${rel.size - (rel - dev).size}，SRG 方法 ${srgLines.size} 个保持原名")
}
proguardGtolibRelease { dependsOn(reobfGtolib, "classes", prepareForgeSrgLibrary, prepareGtmthingsSrgLibrary) }

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
    outputs.upToDateWhen { false }
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
        requireJar(output, "$name 输出", 1024)
        finish()
    }
}

val sealGtolibDev = registerSeal("sealGtolibDev", "Seal 加密 dev 轨（named、keepnames，只在本机使用）", proguardDevJar, protectedJar) {
    verifySealShells(protectedJar, "GTO Seal(dev)")
    injectMixinResources(protectedJar, true)
    verifyMixinNamespace(protectedJar, true, "GTOLib dev 轨")
    verifyDevCompileNamespace(protectedJar, "GTOLib dev 轨")
}
sealGtolibDev { dependsOn(proguardGtolibDev) }

val sealGtolibRelease = registerSeal("sealGtolibRelease", "Seal 加密发行轨（类名混淆），剥除 .dev.bin 并压缩空壳", proguardReleaseJar, releaseJar) {
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
    verifyMainGtolibImportsAgainstJar(releaseJar)
}
sealGtolibRelease { dependsOn(proguardGtolibRelease) }

tasks.register("buildGtolibProtected") {
    group = "gtolib"
    description = "生成加密 gtolib（dev 轨 protected + 发行轨 release）到 build/gtolib；明文仅 -PgtolibUnprotected"
    if (rebuildGtolib) dependsOn(sealGtolibDev, sealGtolibRelease)
    dependsOn("syncSealRuntimeToLibs")
    outputs.files(protectedJar, releaseJar)
    outputs.upToDateWhen { isGtolibJarUpToDate }
    doLast {
        when {
            isGtolibDebugMode -> logger.lifecycle("GTOLib: 【未加密/本地】直接使用 main 编译输出；protected jar 与 stamp 保持不变")
            isGtolibJarUpToDate -> {
                logger.lifecycle("GTOLib: stamp 命中，跳过 ProGuard/Seal")
                verifyMixinNamespace(protectedJar, true, "GTOLib dev 轨（stamp 命中）")
                verifyDevCompileNamespace(protectedJar, "GTOLib dev 轨（stamp 命中）")
                verifyMixinNamespace(releaseJar, false, "GTOLib 发行单轨（stamp 命中）")
                verifyMainGtolibImportsAgainstJar(releaseJar)
            }
            else -> {
                val jarSha = sha256(protectedJar)
                stampFile.writeText("fingerprint=$gtolibFingerprint\njarSha256=$jarSha\nreleaseSha256=${sha256(releaseJar)}\n")
                logger.lifecycle(
                    "GTOLib protected (ProGuard+Seal): ${protectedJar.name} ${protectedJar.length()} bytes, release ${releaseJar.name} ${releaseJar.length()} bytes; " +
                        "stamp fp=${gtolibFingerprint.take(16)}… jarSha=${jarSha.take(16)}…"
                )
            }
        }
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
        verifyMainGtolibImportsAgainstJar(releaseJar)
        logger.lifecycle("GTO jar: 嵌入 gtolib 包体 ← ${releaseJar.name}")
        publishGtolibRefmaps(compileGtolibSources)
    }
    doLast {
        // 打入最终 jar 根目录唯一一份 refmap（含关键 Shadow 补丁）
        val outJar = archiveFile.get().asFile
        rewriteJar(outJar, extra = publishGtolibRefmaps(false) ?: throw GradleException("无法将 gtolib.refmap.json 注入发行 jar"))
        val text = readJarEntry(outJar, "gtolib.refmap.json")?.toString(Charsets.UTF_8).orEmpty()
        val levelTarget = levelMixinCriticalRefmap.values.first()
        if ("ResourceLocationMixn" !in text || levelTarget !in text) throw GradleException("发行 jar 内 gtolib.refmap.json 缺少关键 SRG 映射")
        logger.lifecycle("GTO jar: Seal 独立; refmap 已注入且含关键 SRG 映射")
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
