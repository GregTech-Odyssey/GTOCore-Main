/**
 * Botania 内联流水线：将 Botania/ 子模块源码与资源直接并入 gtocore 主源码集，随 gtocore 单 jar 编译、reobf 与发布。
 * 发行代码保护（obfuscateGtocoreRelease）会按包前缀保留 vazkii.botania 包原名不混淆。
 */

val sourceSets = the<SourceSetContainer>()
val mainSourceSet = sourceSets["main"]

if (!file("Botania/src/main/java/vazkii").isDirectory) {
    throw GradleException("缺少 Botania 源码（Botania/ submodule）:\n  git submodule update --init Botania")
}

mainSourceSet.java.srcDir("Botania/src/main/java")

// 模组元数据与上游 mixin 配置不并入主 jar；共享命名空间的标签以主仓 datagen 产物为准
tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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

// 清理旧流水线可能残留在 run/*/mods 里的独立 botania-mod.jar，避免类重复加载
val cleanLegacyBotaniaModJar = tasks.register("cleanLegacyBotaniaModJar") {
    group = "botania"
    description = "清理 run/*/mods 下遗留的 botania-mod.jar"
    doLast {
        listOf("client", "data", "server", "gametest").forEach { dir ->
            val legacy = file("run/$dir/mods/botania-mod.jar")
            if (legacy.isFile) legacy.delete()
        }
    }
}

listOf("runClient", "runServer", "runData", "runGameTestServer").forEach { name ->
    tasks.matching { it.name == name }.configureEach { dependsOn(cleanLegacyBotaniaModJar) }
}
