/**
 * 发行包冒烟：把发行 jar 换进已装好模组的整合包服务端，起 Forge 专用服进入世界即停，扫日志判定代码保护是否漏保名。
 * Forge 服务端运行库用官方安装器装一次，缓存在 Gradle 用户目录；Seal 只放行云端签名产物，本地 jar 会被拒。
 */

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

val smokeMcVersion = property("minecraft_version").toString()
val smokeForgeVersion = property("forge_version").toString()
val smokeModId = property("mod_id").toString()
val smokeDir: File = layout.buildDirectory.dir("release-smoke").get().asFile
val smokeServerDir = smokeDir.resolve("server")
val forgeServerHome: File = gradle.gradleUserHomeDir.resolve("caches/gto-release-smoke/forge-$smokeMcVersion-$smokeForgeVersion")
val smokeFailureSignatures = Regex(
    "ClassNotFoundException|NoClassDefFoundError|NoSuchFieldException|NoSuchFieldError|NoSuchMethodException|NoSuchMethodError|" +
        "AbstractMethodError|IncompatibleClassChangeError|IllegalAccessError|Mixin apply failed|MixinApplyError|MixinTransformerError|InvalidMixinException|" +
        "InvalidInjectionException|InjectionError|Critical injection failure|failed injection check|Error encountered reading mixin config"
)
val smokeRelated = Regex("""com[./](?:gtocore|gtolib|hepdd)|gtocore|gtolib|[\u3164\u0300-\u036F\u1AB0-\u1AFF\u20D0-\u20FF]""", RegexOption.IGNORE_CASE)
val smokeLogRecord = Regex("""^\[[^\]]*\d{2}:\d{2}:\d{2}(?:\.\d+)?] \[""")

fun smokeJava(): File = the<JavaToolchainService>().launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) }.get().executablePath.asFile

fun runAndLog(command: List<String>, dir: File, log: File): Int {
    val process = ProcessBuilder(command).directory(dir).redirectErrorStream(true).redirectOutput(log).start()
    return process.waitFor()
}

fun ensureForgeServer(java: File): File {
    val argsFile = forgeServerHome.resolve("libraries/net/minecraftforge/forge/$smokeMcVersion-$smokeForgeVersion/" + if (org.gradle.internal.os.OperatingSystem.current().isWindows) "win_args.txt" else "unix_args.txt")
    val marker = forgeServerHome.resolve("installed.ok")
    if (marker.isFile && argsFile.isFile) return argsFile
    val installer = findProperty("gtoForgeInstaller")?.let { file(it.toString()) } ?: runCatching {
        configurations.detachedConfiguration(dependencies.create("net.minecraftforge:forge:$smokeMcVersion-$smokeForgeVersion:installer")).singleFile
    }.getOrElse {
        throw GradleException("发行包冒烟: 拿不到 Forge $smokeForgeVersion 安装器（离线且未缓存）。联网跑一次，或用 -PgtoForgeInstaller=<forge-$smokeMcVersion-$smokeForgeVersion-installer.jar 路径> 指定", it)
    }
    forgeServerHome.deleteRecursively()
    forgeServerHome.mkdirs()
    val log = smokeDir.resolve("forge-install.log").also { it.parentFile.mkdirs() }
    logger.lifecycle("发行包冒烟: 安装 Forge $smokeForgeVersion 服务端运行库 → $forgeServerHome（需联网下载原版服务端，只做一次）")
    val exit = runAndLog(listOf(java.absolutePath, "-jar", installer.absolutePath, "--installServer", forgeServerHome.absolutePath), forgeServerHome, log)
    if (exit != 0 || !argsFile.isFile) throw GradleException("发行包冒烟: Forge 安装失败（exit=$exit；安装器网络握手中断时也返回 0，重跑即可），见 ${log.relativeTo(projectDir)}")
    marker.writeText(smokeForgeVersion)
    return argsFile
}

fun absoluteForgeArgs(argsFile: File): List<String> {
    val libs = forgeServerHome.resolve("libraries").invariantSeparatorsPath
    return argsFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        .map { it.replace("-DlibraryDirectory=libraries", "-DlibraryDirectory=$libs").replace(Regex("""(?<=^|[\s;:=])libraries/"""), "$libs/") }
        .flatMap { line -> if (line.startsWith("-") && ' ' in line && !line.startsWith("-D")) line.split(' ', limit = 2) else listOf(line) }
}

fun smokeServerPack(): File {
    val pack = findProperty("gtoSmokeServerPack")?.let { file(it.toString()) }
        ?: throw GradleException("发行包冒烟: 用 -PgtoSmokeServerPack=<目录> 指定已装好模组的整合包服务端（服务端包解压后跑 install-mods）。开发 classpath 的模组集合过不了 Seal 校验")
    if (pack.resolve("mods").listFiles { f -> f.name.endsWith(".jar") }.isNullOrEmpty()) throw GradleException("发行包冒烟: $pack/mods 里没有模组，先跑整合包服务端包的 install-mods")
    return pack
}

fun prepareSmokeInstance(pack: File, acceptEula: Boolean) {
    smokeServerDir.deleteRecursively()
    val mods = pack.resolve("mods").listFiles { f -> f.name.endsWith(".jar") && !f.name.startsWith("$smokeModId-") && !f.name.startsWith("gto-seal-runtime") }.orEmpty().sortedBy { it.name }
    val modsDir = smokeServerDir.resolve("mods").also { it.mkdirs() }
    val libs = layout.buildDirectory.dir("libs").get().asFile
    val gtocoreJar = findProperty("gtoSmokeJar")?.let { file(it.toString()) } ?: libs.resolve("$smokeModId-forge-$smokeMcVersion-${project.version}.jar")
    val sealJar = findProperty("gtoSmokeSealRuntime")?.let { file(it.toString()) } ?: libs.resolve("gto-seal-runtime-1.0.jar")
    listOf(gtocoreJar, sealJar).forEach { if (!it.isFile) throw GradleException("发行包冒烟: 缺少发行产物 ${it.relativeTo(projectDir)}") }
    (mods + gtocoreJar + sealJar).forEach { it.copyTo(modsDir.resolve(it.name), overwrite = true) }
    logger.lifecycle("发行包冒烟: 依赖模组 ${mods.size} 个 ← $pack；待测 ${gtocoreJar.name}（${gtocoreJar.parentFile}）")
    listOf("config", "defaultconfigs").map { pack.resolve(it) }.filter { it.isDirectory }.forEach { it.copyRecursively(smokeServerDir.resolve(it.name), overwrite = true) }
    smokeServerDir.resolve("config/gtocore-rules.world.txt").delete()
    smokeServerDir.resolve("server.properties").writeText(
        listOf(
            "level-name=smoke-world", "online-mode=false", "server-port=25599", "enable-rcon=false", "enable-query=false",
            "spawn-protection=0", "view-distance=4", "simulation-distance=4", "max-tick-time=-1", "motd=gtocore release smoke",
        ).joinToString("\n", postfix = "\n")
    )
    if (acceptEula) smokeServerDir.resolve("eula.txt").writeText("eula=true\n")
}

data class SmokeHit(val related: Boolean, val head: String)

fun scanSmokeLog(log: File): List<SmokeHit> {
    if (!log.isFile) return emptyList()
    val hits = mutableListOf<SmokeHit>()
    val record = StringBuilder()
    fun flush() {
        if (record.isNotEmpty() && smokeFailureSignatures.containsMatchIn(record)) {
            val head = record.lineSequence().first().take(300)
            val signature = smokeFailureSignatures.find(record)!!.value
            hits += SmokeHit(smokeRelated.containsMatchIn(record), "$signature | $head")
        }
        record.setLength(0)
    }
    log.forEachLine(Charsets.UTF_8) { line ->
        if (smokeLogRecord.containsMatchIn(line)) flush()
        record.append(line).append('\n')
    }
    flush()
    return hits
}

tasks.register("runReleaseSmokeServer") {
    group = "gtolib"
    description = "把发行 jar（默认 build/libs，-PgtoSmokeJar 指定云端签名产物）换进整合包服务端（-PgtoSmokeServerPack）起专用服，进入世界即停，按日志判定代码保护漏保名；-PacceptMinecraftEula=true 才会进入世界"
    if (findProperty("gtoSmokeJar") == null) dependsOn("obfuscateGtocoreRelease", "stageSealRuntimeArtifact")
    mustRunAfter("signJar")
    outputs.upToDateWhen { false }
    doLast {
        val pack = smokeServerPack()
        val acceptEula = findProperty("acceptMinecraftEula") == "true"
        val timeoutMinutes = findProperty("smokeTimeoutMinutes")?.toString()?.toLong() ?: 15L
        val java = smokeJava()
        val argsFile = ensureForgeServer(java)
        prepareSmokeInstance(pack, acceptEula)
        val console = smokeDir.resolve("console.log")
        val command = listOf(java.absolutePath, "-Xmx${findProperty("smokeXmx") ?: "6G"}") + absoluteForgeArgs(argsFile) + "nogui"
        logger.lifecycle("发行包冒烟: 启动专用服（时限 $timeoutMinutes 分钟）→ ${smokeServerDir.relativeTo(projectDir)}，控制台输出 ${console.relativeTo(projectDir)}")
        val process = ProcessBuilder(command).directory(smokeServerDir).redirectErrorStream(true).start()
        val lines = LinkedBlockingQueue<String>()
        val reader = Thread {
            process.inputStream.bufferedReader(Charsets.UTF_8).useLines { seq -> seq.forEach { lines.put(it) } }
        }.apply { isDaemon = true; start() }
        val deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(timeoutMinutes)
        var done = false
        var eulaStop = false
        var sealRejected = false
        var timedOut = false
        console.bufferedWriter(Charsets.UTF_8).use { out ->
            while (true) {
                val line = lines.poll(1, TimeUnit.SECONDS)
                if (line != null) {
                    out.write(line)
                    out.newLine()
                    if (!done && "Done (" in line && "For help" in line) {
                        done = true
                        logger.lifecycle("发行包冒烟: 已进入世界，发送 stop")
                        process.outputStream.write("stop\n".toByteArray())
                        process.outputStream.flush()
                    }
                    if ("You need to agree to the EULA" in line) eulaStop = true
                    if ("has been modified" in line && "IllegalStateException" in line) sealRejected = true
                    continue
                }
                if (!process.isAlive && !reader.isAlive && lines.isEmpty()) break
                if (System.nanoTime() > deadline) {
                    timedOut = true
                    process.descendants().forEach { it.destroyForcibly() }
                    process.destroyForcibly()
                    process.waitFor(30, TimeUnit.SECONDS)
                    break
                }
            }
        }
        val exit = if (process.isAlive) -1 else process.exitValue()
        val hits = scanSmokeLog(smokeServerDir.resolve("logs/latest.log")).ifEmpty { scanSmokeLog(console) }
        val related = hits.filter { it.related }
        val crashes = smokeServerDir.resolve("crash-reports").listFiles()?.filter { it.isFile }.orEmpty()
        val report = smokeDir.resolve("report.txt")
        val verdict = when {
            sealRejected && !done -> "失败：Seal 运行时拒绝加载（GTO has been modified）。本地构建的 jar 会被拒，改用云端签名产物（-PgtoSmokeJar / -PgtoSmokeSealRuntime）"
            related.isNotEmpty() -> "失败：${related.size} 条与 gtocore/gtolib 相关的加载错误"
            crashes.isNotEmpty() -> "失败：服务端崩溃（${crashes.joinToString { it.name }}）"
            timedOut -> "失败：$timeoutMinutes 分钟内没有进入世界"
            done -> "通过：已进入世界并正常停服（exit=$exit）"
            eulaStop -> "未完成：模组发现与启动器阶段通过，因未接受 EULA 没有进入世界（加 -PacceptMinecraftEula=true）"
            else -> "失败：服务端提前退出（exit=$exit），没有进入世界"
        }
        report.writeText(
            (listOf("发行包冒烟 $verdict", "相关错误 ${related.size}，其他模组错误 ${hits.size - related.size}") +
                related.map { "[相关] ${it.head}" } + hits.filterNot { it.related }.map { "[其他] ${it.head}" }).joinToString("\n", postfix = "\n"),
            Charsets.UTF_8
        )
        related.take(20).forEach { logger.error("  [相关] ${it.head}") }
        logger.lifecycle("发行包冒烟 $verdict；相关错误 ${related.size}、其他模组错误 ${hits.size - related.size}，明细 ${report.relativeTo(projectDir)}")
        if (!verdict.startsWith("通过")) throw GradleException("发行包冒烟 $verdict")
    }
}
