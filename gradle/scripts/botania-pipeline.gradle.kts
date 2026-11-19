/**
 * Botania 流水线（仿 gtolib-pipeline）：子模块源码 → main 编译 → build/botania/botania-mod.jar（独立模组 jar）。
 * 源码指纹命中时不再编译，直接复用 jar；未命中时把 Botania/src 交给 main 编译并重建 jar。
 * 强制重建 -PbotaniaRebuild=true。
 */

import java.security.MessageDigest

val sourceSets = the<SourceSetContainer>()
val mainSourceSet = sourceSets["main"]
val botaniaVersion = property("botania_version").toString()
val botaniaOut: File = layout.buildDirectory.dir("botania").get().asFile
val modJar = botaniaOut.resolve("botania-mod.jar")
val stampFile = botaniaOut.resolve("inputs.stamp")
val classesJava: File = layout.buildDirectory.dir("classes/java/main").get().asFile
val classesKotlin: File = layout.buildDirectory.dir("classes/kotlin/main").get().asFile

if (!file("Botania/src/main/java/vazkii").isDirectory) {
    throw GradleException("缺少 Botania 源码（Botania/ submodule）:\n  git submodule update --init Botania")
}

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

fun computeBotaniaFingerprint(): String {
    val md = MessageDigest.getInstance("SHA-256")
    file("Botania/src").walkTopDown().filter { it.isFile }.sortedBy { it.absolutePath }.forEach { f ->
        md.update(f.relativeTo(rootDir).invariantSeparatorsPath.toByteArray())
        md.update(0)
        md.update(f.length().toString().toByteArray())
        md.update(0)
        md.update(f.readBytes())
        md.update(0)
    }
    md.update("botania_version=$botaniaVersion".toByteArray())
    md.update(0)
    return md.digest().joinToString("") { "%02x".format(it) }
}

fun readStamp(): Map<String, String> =
    if (!stampFile.isFile) emptyMap()
    else stampFile.readLines().filter { it.indexOf('=') > 0 }.associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }

val botaniaFingerprint = computeBotaniaFingerprint()
val isBotaniaJarUpToDate: Boolean = run {
    if (findProperty("botaniaRebuild") == "true") return@run false
    val stamp = readStamp()
    val reason = when {
        gradle.startParameter.taskNames.any { it == "clean" || it.endsWith(":clean") } -> "本次调用含 clean"
        stamp["fingerprint"] != botaniaFingerprint -> "stamp 源码指纹缺失或过期"
        sha256(modJar) != stamp["jarSha256"] -> "build/botania/botania-mod.jar 缺失或与 stamp 不一致"
        else -> null
    }
    if (reason != null) logger.lifecycle("Botania: $reason -> 重建模组产物")
    reason == null
}
val compileBotaniaSources = !isBotaniaJarUpToDate

logger.lifecycle(
    "Botania pipeline: jarUpToDate=$isBotaniaJarUpToDate, compileSources=$compileBotaniaSources, " +
        "fingerprint=${botaniaFingerprint.take(12)}…"
)

if (compileBotaniaSources) {
    mainSourceSet.java.srcDir("Botania/src/main/java")
    tasks.named("compileKotlin") { outputs.upToDateWhen { classesKotlin.resolve("vazkii/botania").exists() } }
    tasks.named("compileJava") {
        outputs.upToDateWhen { classesJava.resolve("vazkii/botania").list()?.isNotEmpty() == true }
    }
    logger.lifecycle("Botania: 源码有变更 → 将重新编译并重建模组 jar")
    // 与主仓合编译时，模组元数据与上游 mixin 配置不能并入，共享命名空间的标签以主仓 datagen 产物为准
    tasks.named<ProcessResources>("processResources") {
        from("Botania/src/main/resources") {
            exclude("META-INF/**", "pack.mcmeta", "botania_xplat.mixins.json", "botania_forge.mixins.json")
        }
        from("Botania/src/generated/resources") {
            exclude(
                "data/forge/tags/items/armors/boots.json",
                "data/forge/tags/items/armors/chestplates.json",
                "data/forge/tags/items/armors/helmets.json",
                "data/forge/tags/items/armors/leggings.json",
                "data/minecraft/tags/blocks/wither_immune.json",
                "data/minecraft/tags/blocks/mineable/axe.json",
                "data/minecraft/tags/blocks/mineable/pickaxe.json",
                "data/minecraft/tags/blocks/mineable/shovel.json",
                "data/minecraft/tags/damage_type/bypasses_armor.json",
                "data/minecraft/tags/items/freeze_immune_wearables.json",
            )
        }
    }
} else {
    logger.lifecycle("Botania: 指纹命中 → 直接依赖 build/botania/botania-mod.jar")
}

val buildBotaniaMod = tasks.register<Jar>("buildBotaniaMod") {
    group = "build"
    description = "打包 Botania 子模块为独立模组 jar（build/botania/botania-mod.jar）"
    archiveFileName.set(modJar.name)
    destinationDirectory.set(botaniaOut)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(mainSourceSet.output) { include("vazkii/botania/**") }
    from("Botania/src/main/resources") {
        exclude("botania_xplat.mixins.json", "botania_forge.mixins.json")
    }
    from("Botania/src/generated/resources")
    doLast {
        require(modJar.isFile) { "Botania 模组 jar 未生成：${modJar.path}" }
        java.util.zip.ZipFile(modJar).use { zf ->
            require(zf.entries().asSequence().any { it.name.startsWith("vazkii/botania/") && it.name.endsWith(".class") }) {
                "Botania 模组 jar 没有任何类"
            }
        }
        stampFile.writeText(
            "fingerprint=$botaniaFingerprint\njarSha256=${sha256(modJar)}\n"
        )
    }
}

if (compileBotaniaSources) {
    // 编译模式：类随 main 输出，同时产出模组 jar 供分发
    tasks.matching { it.name == "jar" }.configureEach { dependsOn(buildBotaniaMod) }
} else {
    // 产物模式：编译期引用 jar，运行时由 run/mods 下的模组 jar 提供，避免重复加载
    dependencies { add("compileOnly", files(modJar)) }
}

val installBotaniaMod = tasks.register("installBotaniaMod") {
    group = "botania"
    description = "编译模式保留 main 输出；产物模式把 Botania 模组 jar 放进 run/mods"
    dependsOn("classes")
    if (compileBotaniaSources) dependsOn(buildBotaniaMod)
    doLast {
        val runModDirs = listOf("client", "data", "server", "gametest").map { file("run/$it/mods") }
        runModDirs.forEach { dir ->
            dir.listFiles()?.filter { it.name == modJar.name }?.forEach { it.delete() }
        }
        if (compileBotaniaSources) {
            // 打包已完成；合编译模式下 main 不能再带 @Mod 入口类，否则主 mods.toml 里没有 botania → Forge 记成 BROKEN
            delete(classesJava.resolve("vazkii/botania/Botania.class"))
            return@doLast
        }
        require(modJar.isFile) { "缺少 Botania 模组产物：${modJar.path}（用 -PbotaniaRebuild=true 重建）" }
        // 安装态从 main 输出移除子模块类，改由 run/mods 的 jar 提供（与 GTOLib 相同）
        listOf(classesJava, classesKotlin).forEach { delete(it.resolve("vazkii/botania")) }
        runModDirs.forEach {
            it.mkdirs()
            modJar.copyTo(it.resolve(modJar.name), overwrite = true)
        }
        logger.lifecycle("Botania: 模组 jar → ${runModDirs.joinToString { it.parentFile.name }}")
    }
}

listOf("runClient", "runServer", "runData", "runGameTestServer").forEach { name ->
    tasks.matching { it.name == name }.configureEach { dependsOn(installBotaniaMod) }
}
