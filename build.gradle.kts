import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.Library
import org.gradle.plugins.ide.eclipse.model.SourceFolder
import org.gradle.plugins.ide.eclipse.model.internal.FileReferenceFactory
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.zip.ZipFile

plugins {
    idea
    eclipse
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.modDevGradleLegacy)
    alias(libs.plugins.lombok)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin)
    alias(libs.plugins.kotlin.lombok)
    alias(libs.plugins.gtopublishgradleplugin)
    id("org.jetbrains.kotlin.kapt") version "2.3.20"
}

fun prop(name: String): String = providers.gradleProperty(name).get()

val mod_version = prop("mod_version")
val mod_id = prop("mod_id")
val mod_group_id = prop("mod_group_id")
val mod_name = prop("mod_name")
val mod_license = prop("mod_license")
val mod_description = prop("mod_description")
val mod_url = prop("mod_url")
val mod_issue_tracker = prop("mod_issue_tracker")
val pack_version = prop("pack_version")
val minecraft_version = prop("minecraft_version")
val forge_version = prop("forge_version")
val parchment_minecraft_version = prop("parchment_minecraft_version")
val parchment_mappings_version = prop("parchment_mappings_version")
val mixinextras_version = prop("mixinextras_version")
val taffy_version = prop("taffy_version")
val ae2_version = prop("ae2_version")
val extendedae_version = prop("extendedae_version")
val emi_version = prop("emi_version")
val botania_version = prop("botania_version")
val ars_nouveau_version = prop("ars_nouveau_version")
val gtceu_version = prop("gtceu_version")
val datasynclib_version = prop("datasynclib_version")
val gtoutils_version = prop("gtoutils_version")
val gtorenders_version = prop("gtorenders_version")
val recipeSearch_version = prop("recipeSearch_version")
val architectury_version = prop("architectury_version")
val resourcefullib_version = prop("resourcefullib_version")
val resourcefulconfig_version = prop("resourcefulconfig_version")
val botarium_version = prop("botarium_version")
val ad_astra_version = prop("ad_astra_version")
val jei_version = prop("jei_version")
val curiosVersion = prop("curiosVersion")

version = mod_version

// 本地密钥可放在被忽略的 .env 里；进程环境变量优先。
val dotenv: Map<String, String> = file(".env").takeIf { it.isFile }?.readLines(Charsets.UTF_8).orEmpty()
    .map { it.trim().removePrefix("export ").trim() }
    .filter { it.isNotEmpty() && !it.startsWith("#") && it.indexOf('=') > 0 }
    .associate { line ->
        val key = line.substringBefore('=').trim()
        var value = line.substringAfter('=').trim()
        if (value.length >= 2 && (value.startsWith('"') && value.endsWith('"') || value.startsWith('\'') && value.endsWith('\''))) {
            value = value.substring(1, value.length - 1)
        }
        key to value
    }
    .filterKeys { it.matches(Regex("[A-Za-z_][A-Za-z0-9_]*")) }

fun localEnv(name: String): String? = System.getenv(name) ?: dotenv[name]

// 模组字节码目标 21（GTCEu/gtmthings/datasynclib 等依赖要求 JVM 21+）；Java 25 字节码无法被 reobf/ART 处理。
java.toolchain.languageVersion = JavaLanguageVersion.of(21)

sourceSets {
    main {
        resources.srcDir("src/generated/resources")
    }
    test {
        java.srcDir("GTOLib/src/test/java")
        compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
    }
}

// ---------- ModDevGradle ----------

mixin {
    add(sourceSets.main.get(), "gtocore.refmap.json")
    add(sourceSets.main.get(), "gtolib.refmap.json")
    config("gtocore.mixins.json")
    config("gtolib.mixins.json")
}

// IDEA 的 Application run 只能挂一个 source set 的 classpath：每个 run 用自己的空 source set，
// 让只在运行时需要的模组留在启动 classpath 上被 Forge 发现。
val runNames = listOf("client", "data", "server")
runNames.forEach { runName ->
    sourceSets.create(runName) {
        java.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    configurations.named("${runName}Implementation") { extendsFrom(configurations.implementation.get()) }
    configurations.named("${runName}CompileOnly") { extendsFrom(configurations.compileOnly.get()) }
    configurations.named("${runName}RuntimeOnly") { extendsFrom(configurations.runtimeOnly.get()) }
}

sourceSets.create("gametest") {
    java.setSrcDirs(listOf("src/gametest/java"))
    resources.setSrcDirs(listOf("src/gametest/resources"))
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().output
}
configurations.named("gametestImplementation") { extendsFrom(configurations.implementation.get()) }
configurations.named("gametestCompileOnly") { extendsFrom(configurations.compileOnly.get()) }
configurations.named("gametestRuntimeOnly") { extendsFrom(configurations.runtimeOnly.get()) }
configurations.named("gametestRuntimeClasspath") { exclude(group = "curse.maven", module = "open-parties-and-claims-636608") }

legacyForge {
    version = "$minecraft_version-$forge_version"

    // 自定义 run source set 不会自动拿到 Minecraft/Forge 运行时产物，缺了会跳过依赖的 AT/SRG 重映射。
    (runNames + "gametest").forEach { addModdingDependenciesTo(sourceSets[it]) }

    parchment {
        minecraftVersion = parchment_minecraft_version
        mappingsVersion = parchment_mappings_version
    }

    runs {
        create("client") {
            client()
            sourceSet = sourceSets["client"]
            gameDirectory.set(file("run/client"))
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }
        create("server") {
            server()
            sourceSet = sourceSets["server"]
            programArgument("--nogui")
            gameDirectory.set(file("run/server"))
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }
        create("data") {
            data()
            sourceSet = sourceSets["data"]
            ideName = "Data"
            gameDirectory.set(file("run/data"))
            programArguments.addAll("--mod", mod_id, "--all")
            programArguments.addAll("--output", file("src/generated/resources/").absolutePath)
            programArguments.addAll("--existing", file("src/main/resources/").absolutePath)
            programArguments.addAll("--existing-mod", "gtceu", "--existing-mod", "ars_nouveau")
        }
        create("gameTestServer") {
            type = "gameTestServer"
            sourceSet = sourceSets["gametest"]
            gameDirectory.set(file("run/gametest"))
            systemProperty("forge.enabledGameTestNamespaces", mod_id)
        }
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.INFO
        }
    }

    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["gametest"])
        }
    }

    setAccessTransformers("src/main/resources/META-INF/accesstransformer.cfg")
}

// 每个 run source set 一个重映射依赖桶（modClientRuntimeOnly / modDataRuntimeOnly / modServerRuntimeOnly / modGametestRuntimeOnly）
(runNames + "gametest").forEach { obfuscation.createRemappingConfiguration(configurations.getByName("${it}RuntimeOnly")) }

// ---------- 仓库 ----------

repositories {
    mavenLocal()
    flatDir { dir("libs") }
    mavenCentral()
    maven("https://gtodyssey.com/maven/releases")
    exclusiveContent { // CurseForge
        forRepository { maven("https://cursemaven.com") }
        filter { includeGroup("curse.maven") }
    }
    exclusiveContent { // Xaero's
        forRepository { maven("https://chocolateminecraft.com/maven/") }
        filter { includeGroupAndSubgroups("xaero") }
    }
    maven("https://maven.shadowsoffire.dev/releases") { content { includeGroup("dev.shadowsoffire") } }
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.blamejared.com/") { name = "Jared's Maven" } // JEI
    maven("https://modmaven.dev") { name = "ModMaven" } // JEI 镜像、AE2
    maven("https://maven.firstdarkdev.xyz/snapshots/")
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.architectury.dev/")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    maven("https://maven.jt-dev.tech/releases/")
    maven("https://mvnrepository.com/artifact/com.hollingsworth.ars_nouveau")
    maven("https://maven.bawnorton.com/releases")
    maven("https://maven.enjarai.dev/mirrors")
    maven("https://maven.twelveiterations.com/repository/maven-public/") { content { includeGroup("net.blay09.mods") } }
    maven("https://neoforged.forgecdn.net/releases")
    maven("https://neoforged.forgecdn.net/mojang-meta")
}

// ---------- 依赖 ----------

/** 编译期可见，运行时只进客户端 */
fun DependencyHandler.clientMod(notation: String) {
    add("modCompileOnly", notation)
    add("modClientRuntimeOnly", notation)
}

/** 编译期可见，运行时进客户端与服务端（不进 data/gametest） */
fun DependencyHandler.sidedMod(notation: String) {
    clientMod(notation)
    add("modServerRuntimeOnly", notation)
}

/** 编译期可见，运行时进全部 run */
fun DependencyHandler.runtimeMod(notation: String) {
    add("modCompileOnly", notation)
    add("modRuntimeOnly", notation)
}

dependencies {
    // 基础库、注解处理器与测试
    compileOnly(libs.jetbrains.annotations)
    "org.projectlombok:lombok:1.18.42".let { compileOnly(it); annotationProcessor(it) }
    "io.github.llamalad7:mixinextras-common:$mixinextras_version".let { api(it); annotationProcessor(it) }
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    "com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.2-beta.4".let { compileOnly(it); annotationProcessor(it) }
    implementation("com.github.bawnorton.mixinsquared:mixinsquared-neoforge:0.3.2-beta.4")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("curse.maven:spark-361579:4738952")

    implementation("org.lwjgl:lwjgl:3.3.2")
    implementation("org.lwjgl:lwjgl-opengl:3.3.2")
    implementation("curse.maven:kotlin-for-forge-351264:5402061")
    implementation("com.github.houbb:opencc4j:1.14.0") // datagen 繁中

    // Taffy：GTM uipro 的布局引擎，运行时由 GTM 的 jarJar 提供；这里再内嵌或进运行 classpath 会出现两个同名模块
    compileOnly("dev.vfyjxf:taffy:$taffy_version")

    // GTO Seal Runtime 只进开发 classpath，禁止打进 gtocore（ITransformationService SPI 会让 Forge 整包排除 gtocore）；
    // 运行与发行时作为独立 gto-seal-runtime-1.0.jar 与 gtocore 并列放在 mods/。
    implementation("dev:gto-seal-runtime:1.0")

    // jarJar 内嵌依赖
    "org.apache.commons:commons-math3:3.6.1".let { implementation(it); jarJar(it) }

    // 全局运行范围
    modImplementation("appeng:appliedenergistics2-forge-1.20.1:$ae2_version")
    modImplementation("com.gregtechceu.gtceu:gtceu-1.20.1-forge-1.20.1:$gtceu_version")
    modImplementation("com.gto:datasynclib-forge-1.20.1:$datasynclib_version")
    modImplementation("com.gto:gtoutils-forge-1.20.1:$gtoutils_version")
    modImplementation("com.gto:gtorenders-forge-1.20.1:$gtorenders_version")
    modImplementation("curse.maven:fast-recipe-search-1406509:8737025")
    modCompileOnly("com.gto.recipesearch:RecipeSearch:$recipeSearch_version")
    implementation("com.gto.fastcollection:FastCollection:26.8.7")
    modImplementation("com.lowdragmc.ldlib:ldlib-forge-1.20.1:1.0.50")
    modImplementation("curse.maven:glodium-957920:5006780")
    modImplementation("curse.maven:lean-object-1703183:8929333")
    modImplementation("curse.maven:duplicationless-1380105:8646420")

    // Applied Energistics 2
    modImplementation("org.appliedenergistics:guideme:20.1.13")
    modImplementation("curse.maven:applied-energistics-2-wireless-terminals-459929:7251206")
    modImplementation("com.glodblock.github.extendedae:extendedae-forge-1.20.1:$extendedae_version")
    modImplementation("curse.maven:merequester-688367:5689932")
    modImplementation("curse.maven:ae2-toggleable-view-cell-1141472:8054118")

    // Ad Astra
    modImplementation("dev.architectury:architectury-forge:$architectury_version")
    modImplementation("com.teamresourceful:yabn:1.0.3")
    modImplementation("com.teamresourceful:bytecodecs:1.0.2")
    modImplementation("com.teamresourceful.resourcefullib:resourcefullib-forge-$minecraft_version:$resourcefullib_version") { isTransitive = false }
    modImplementation("com.teamresourceful.resourcefulconfig:resourcefulconfig-forge-$minecraft_version:$resourcefulconfig_version") { isTransitive = false }
    modImplementation("earth.terrarium.botarium:botarium-forge-$minecraft_version:$botarium_version") { isTransitive = false }
    modImplementation("earth.terrarium.adastra:ad_astra-forge-$minecraft_version:$ad_astra_version") { isTransitive = false }
    modImplementation("curse.maven:ad-astra-rocketed-996980:5245376")

    // JEI / EMI
    modImplementation("dev.emi:emi-forge-1.20.1:$emi_version")
    modCompileOnly("mezz.jei:jei-$minecraft_version-forge-api:$jei_version") { isTransitive = false }
    modCompileOnly("mezz.jei:jei-$minecraft_version-common-api:$jei_version") { isTransitive = false }
    modImplementation("mezz.jei:jei-$minecraft_version-forge:$jei_version") { isTransitive = false }

    // Botania
    modImplementation("top.theillusivec4.curios:curios-forge:$curiosVersion+$minecraft_version")
    modImplementation("vazkii.botania:Botania-forge-1.20.1:$botania_version")
    modImplementation("vazkii.patchouli:Patchouli:$minecraft_version-84.1-FORGE")
    modImplementation("curse.maven:extrabotany-reburn-1271869:7182385")
    modImplementation("curse.maven:event-wrapper-1256971:7182179")
    modImplementation("curse.maven:mythicbotany-400058:6702112")

    // Apotheosis
    modImplementation("dev.shadowsoffire:ApothicAttributes:$minecraft_version-1.3.7")
    modImplementation("dev.shadowsoffire:Placebo:$minecraft_version-8.6.2")
    modImplementation("dev.shadowsoffire:Apotheosis:$minecraft_version-7.4.8")

    // FTB
    modImplementation("curse.maven:ftb-library-forge-404465:8226927")
    modImplementation("curse.maven:ftb-ultimine-forge-386134:7880472")

    // Farmers Delight
    modImplementation("curse.maven:farmers-delight-398521:8083474")
    modImplementation("curse.maven:farmers-respite-551453:5543547")

    // 其他
    modImplementation("dev:Jade-1.20.1-Forge-11.13.1")
    modImplementation("curse.maven:building-gadgets-298187:6850495")
    modImplementation("curse.maven:configuration-444699:5840405")
    modImplementation("curse.maven:endremastered-404183:5598990")
    modImplementation("curse.maven:libx-412525:5207625")
    modImplementation("curse.maven:functional-storage-556861:5650296")
    modImplementation("curse.maven:titanium-287342:5468426")
    modImplementation("curse.maven:deeperdarker-659011:5906086")
    modImplementation("curse.maven:modernfix-790626:7928686")
    modCompileOnly("curse.maven:spark-361579:4738952")
    modImplementation("curse.maven:just-enough-resources-jer-240630:5057220")
    modImplementation("curse.maven:construction-wand-399558:4684054")
    modImplementation("curse.maven:pipez-443900:6945379")
    modImplementation("curse.maven:jumbo-furnace-390880:4685578")
    modImplementation("curse.maven:easy-villagers-400514:6831415")
    modImplementation("curse.maven:ae2-import-export-card-982512:5551629")
    modImplementation("curse.maven:ae2-toogleable-view-cell-1141472:8054118")
    modImplementation("curse.maven:ae2-network-analyser-961856:5107131")
    modImplementation("curse.maven:explorers-compass-491794:7892927")
    modImplementation("curse.maven:natures-compass-252848:7892948")
    modImplementation("curse.maven:foodvariations-1109288:8832188")
    modImplementation("curse.maven:spice-of-life-carrot-edition-277616:4888575")

    // Ars Nouveau
    modImplementation("com.hollingsworth.ars_nouveau:ars_nouveau-1.20.1-forge-1.20.1:$ars_nouveau_version")
    modRuntimeOnly("curse.maven:geckolib-388172:8285794")
    modImplementation("curse.maven:ars-meteorites-1278083:7125271")

    runtimeMod("curse.maven:open-parties-and-claims-636608:8690609")
    runtimeMod("curse.maven:modular-routers-250294:5056687")

    // 只在运行时需要（无源码引用）
    modRuntimeOnly("curse.maven:cloth-config-348521:5729105")
    modRuntimeOnly("curse.maven:more-red-387638:4691333")
    runtimeMod("curse.maven:torchmaster-254268:5849456")
    modRuntimeOnly("curse.maven:xtones-reworked-694231:5907303")

    // 客户端与服务端
    sidedMod("curse.maven:ftb-quests-forge-289412:8078538")
    sidedMod("curse.maven:ftb-xmod-compat-889915:6402486")
    sidedMod("curse.maven:ftb-teams-forge-404468:7499810")
    sidedMod("curse.maven:sophisticated-backpacks-422301:9083935")
    sidedMod("curse.maven:sophisticated-core-618298:9112479")
    sidedMod("curse.maven:sophisticated-storage-619320:9083978")
    sidedMod("curse.maven:tool-belt-260262:5929061")
    sidedMod("curse.maven:immersive-aircraft-666014:8865821")
    sidedMod("curse.maven:factory-blocks-640001:6267781")
    listOf("curse.maven:spark-361579:4738952", "curse.maven:modern-ui-352491:5977000").forEach {
        "modClientRuntimeOnly"(it)
        "modServerRuntimeOnly"(it)
    }

    // 只进客户端
    clientMod("xaero.lib:xaerolib-forge-1.20.1:1.1.15")
    clientMod("xaero.map:xaeroworldmap-forge-1.20.1:1.41.2")
    clientMod("xaero.minimap:xaerominimap-forge-1.20.1:26.1.0")
    clientMod("curse.maven:emi-loot-681783:6449092")
    clientMod("curse.maven:mouse-tweaks-60089:5338457")
    clientMod("dev:jecharacters:1.20.1-forge-4.5.11-dev-shadow")
    clientMod("curse.maven:render-blender-1346485:7035162")
    clientMod("curse.maven:embeddium-908741:5681725")
    clientMod("curse.maven:moremorelang-1444917:8673054")
    clientMod("maven.modrinth:oculus:1.20.1-1.8.0")
    "modClientRuntimeOnly"("curse.maven:fzzy-config-1005914:7085017")
    "modClientRuntimeOnly"("curse.maven:imblocker-483760:6922546")

    // 只编译（含 gtolib mixin 目标）
    modCompileOnly("curse.maven:ferritecore-429235:4810975")
    modCompileOnly("curse.maven:radium-reforged-570017:5706069")
    modCompileOnly("curse.maven:oculus-581495:6020952")
    modCompileOnly("curse.maven:alltheleaks-1091339:7040641")
    modCompileOnly("curse.maven:ftb-essentials-410811:4896152")
    modCompileOnly("software.bernie.geckolib:geckolib-forge-$minecraft_version:4.7.1.2")
    modCompileOnly("corgitaco.corgilib:Corgilib-Forge:$minecraft_version-4.0.3.3")
    modCompileOnly("curse.maven:Shimmer-forge-627824:5685940")
    modCompileOnly("curse.maven:chisel-reborn-551763:6266130")
    modCompileOnly("curse.maven:biomes-o-plenty-220318:6028852")
    modCompileOnly("curse.maven:oh-the-biomes-weve-gone-1070751:8255932")
    modCompileOnly("curse.maven:athena-841890:5176879")
    modCompileOnly("curse.maven:inventory-essentials-368825:6977231")
    modCompileOnly("curse.maven:balm-531761:6978679")
    modCompileOnly("dev:gtoepp:1.0.1")
    modCompileOnly("com.ref:calculatoroverlay:1.0.2")
    modCompileOnly("dev:calculatoroverlay:1.0.2")

//    // 开发客户端内存分析：named 产物无需重映射，只加入客户端运行时。
//    "clientRuntimeOnly"("com.gto:memory-tool-dev-forge-1.20.1:0.1.2")
//    "clientRuntimeOnly"("io.github.llamalad7:mixinextras-forge:0.5.5")


    // 额外模组
    // modImplementation(jarJar("dev.mineland:item-interactions-mod-forge+1.20.1:2.1.4"))
    // modRuntimeOnly("curse.maven:mouse-tweaks-60089:4581240")
    // modRuntimeOnly("curse.maven:inventory-essentials-368825:6977231")
    // modRuntimeOnly("curse.maven:balm-531761:6978679")
    // modCompileOnly("curse.maven:mouse-tweaks-60089:4581240")
    // runtimeMod("curse.maven:rts-building-build-from-above-1553142:8155138")
    // modRuntimeOnly("curse.maven:super-factory-manager-306935:7021189")
    // modRuntimeOnly("curse.maven:worldedit-225608:4586218")
//     modRuntimeOnly("curse.maven:create-328085:7178761") // 几把动力
//     modRuntimeOnly("curse.maven:ftb-chunks-forge-314906:7880858") // 有个人存档有
    // modRuntimeOnly("curse.maven:effortless-building-302113:6330928")

    // 牢模组
    // modRuntimeOnly("curse.maven:re-avaritia-623969:7229795")
    // modCompileOnly("curse.maven:ender-io-64578:7183516")
    // modRuntimeOnly("curse.maven:laserio-uel-1168178:6773950")
    // modRuntimeOnly("curse.maven:redstone-pen-461710:5242883")
    // modRuntimeOnly("curse.maven:huge-structure-blocks-474114:4803547")
}

// GTOLib：打包 → 加密 → 嵌入 gtocore；run*/jar 都依赖这条流水线
apply(from = "gradle/scripts/gtolib-pipeline.gradle.kts")
apply(from = "gradle/scripts/release-smoke.gradle.kts")
if (extra["isGtolibDebugMode"] == true) {
    legacyForge.runs.configureEach { systemProperty("gtolib.debug", "true") }
}

val questHotReloadClasspath = layout.buildDirectory.file("quest-hotreload/classpath.txt")
val writeQuestHotReloadClasspath = tasks.register("writeQuestHotReloadClasspath") {
    val main = sourceSets.main.get()
    val entries = main.output.classesDirs + main.compileClasspath
    val sourceRoot = file("src/main/java")
    outputs.upToDateWhen { false }
    doLast {
        val target = questHotReloadClasspath.get().asFile
        target.parentFile.mkdirs()
        target.writeText((listOf(sourceRoot.absolutePath) + entries.files.map { it.absolutePath }).joinToString("\n"))
    }
}
legacyForge.runs.configureEach { systemProperty("gtocore.questHotReload", questHotReloadClasspath.get().asFile.absolutePath) }
tasks.matching { it.name in setOf("runClient", "runServer", "runData", "runGameTestServer") }.configureEach { dependsOn(writeQuestHotReloadClasspath) }

// ---------- 资源与 jar ----------

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "version" to version,
        "mod_id" to mod_id,
        "mod_license" to mod_license,
        "mod_name" to mod_name,
        "mod_description" to mod_description,
        "mod_url" to mod_url,
        "mod_issue_tracker" to mod_issue_tracker,
        "pack_version" to pack_version,
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
    doFirst {
        listOf("run/client", "run/server", "run/data").forEach { file(it).mkdirs() }
        delete(fileTree("run/client/gtocore/cache"))
    }
}
sourceSets.main.get().resources.srcDir(generateModMetadata)
legacyForge.ideSyncTask(generateModMetadata)

base {
    archivesName = mod_id
}

java {
    withSourcesJar()
}

tasks.withType<Jar>().configureEach {
    manifest.attributes(
        "MixinConfigs" to "gtocore.mixins.json,gtolib.mixins.json",
        "Specification-Title" to project.name,
        "Specification-Version" to mod_version,
        "Implementation-Title" to base.archivesName.get(),
        "Implementation-Version" to version,
        "Name" to "com/gtocore/mixin",
        "Sealed" to "true",
    )
}

tasks.jar {
    exclude("**/.cache", "**/SimplifiedToTraditional.properties")
}

// ---------- 编译与测试 ----------

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // 模组发行字节码固定 21（勿用 25，否则 reobf 报 major 69）
    options.release = 21
    options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
    // Mixin AP + 大体量工程：worker 默认堆过小会 OOM（与 daemon 的 org.gradle.jvmargs 无关）
    options.isFork = true
    options.forkOptions.jvmArgs = listOf("-Xmx4g", "-Xms512m")
    options.forkOptions.memoryMaximumSize = "4g"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.compileJava {
    // Mixin AP 不支持增量、每次都整编；开着增量分析只会多一次无用的类依赖扫描。
    options.isIncremental = false
    dependsOn(tasks.compileKotlin)
    classpath += files(tasks.compileKotlin.map { it.destinationDirectory })
}

kapt {
    keepJavacAnnotationProcessors = true
}

val gtolibPlaintext = findProperty("gtolibUnprotected") == "true" || findProperty("gtolibDebug") == "true"

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform { excludeTags("limitation") }
    onlyIf { gtolibPlaintext }
}

fun Test.craftingTest(limits: Boolean) {
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter { includeTestsMatching("com.gtolib.ae2.crafting2.*") }
    useJUnitPlatform { if (limits) includeTags("limitation") else excludeTags("limitation") }
    workingDir = layout.buildDirectory.dir("crafting-test").get().asFile
    doFirst {
        workingDir.mkdirs()
        if (!gtolibPlaintext) throw GradleException("$name 需要明文 GTOLib：加 -PgtolibUnprotected=true")
    }
}
tasks.register<Test>("testCrafting") {
    description = "AE 自动合成：能力与性能预警测试（应全部通过）"
    craftingTest(false)
}
tasks.register<Test>("testCraftingLimits") {
    description = "AE 自动合成：能力边界测试（按理想语义断言，当前预期失败）"
    craftingTest(true)
}

// ---------- 运行 ----------

tasks.named("prepareClientRun") {
    outputs.upToDateWhen { false }
}

mapOf("Client" to "clientRuntimeClasspath", "Server" to "serverRuntimeClasspath", "Data" to "dataRuntimeClasspath", "GameTestServer" to "gametestRuntimeClasspath").forEach { (side, configName) ->
    tasks.named("write${side}LegacyClasspath") {
        doLast {
            val out = layout.buildDirectory.file("moddev/${side.replaceFirstChar { it.lowercase() }}LegacyClasspath.txt").get().asFile
            out.parentFile.mkdirs()
            out.appendText(configurations.getByName(configName).files.map { it.absolutePath }
                .filter { p -> listOf("commons-math3", "opencc4j", "heaven", "nlp-common").any { p.contains(it) } }
                .joinToString(System.lineSeparator()))
        }
    }
}

// ---------- 签名与发布 ----------

val signJar = tasks.register("signJar") {
    dependsOn("stageSealRuntimeArtifact")
    outputs.upToDateWhen { false }
    onlyIf { file("keystore.jks").exists() }
    doLast {
        val libsDir = layout.buildDirectory.dir("libs").get().asFile
        fun sign(jar: File) = ant.invokeMethod(
            "signjar",
            mapOf(
                "jar" to jar.absolutePath,
                "destDir" to libsDir.absolutePath,
                "alias" to "gtokey",
                "storetype" to "jks",
                "keystore" to "keystore.jks",
                "storepass" to localEnv("GTO_STOREPASS"),
                "keypass" to localEnv("GTO_KEYPASS"),
                "preservelastmodified" to "true",
                "tsaurl" to "http://timestamp.sectigo.com?td=sha256",
            ),
        )
        sign(libsDir.resolve("$mod_id-forge-1.20.1-$version.jar"))
        // Seal Runtime 独立产物一并签名（与 gtocore 并列分发）
        val sealJar = libsDir.resolve("gto-seal-runtime-1.0.jar")
        if (!sealJar.isFile) {
            file("libs/gto-seal-runtime-1.0.jar").takeIf { it.isFile }?.copyTo(sealJar)
        }
        if (sealJar.isFile && sealJar.length() > 0) {
            sign(sealJar)
            logger.lifecycle("GTO Seal: signed ${sealJar.name}")
        } else {
            logger.warn("GTO Seal: build/libs/gto-seal-runtime-1.0.jar 不存在，跳过签名")
        }
    }
}
tasks.matching { it.name == "reobfJar" }.configureEach { finalizedBy(signJar) }

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = mod_group_id
            artifactId = mod_id
        }
    }
    repositories {
        maven("https://gtodyssey.com/maven/releases") {
            name = "gtodysseyRepository"
            credentials {
                username = findProperty("gtodysseyRepositoryUsername")?.toString() ?: ""
                password = findProperty("gtodysseyRepositoryPassword")?.toString() ?: ""
            }
        }
    }
}

gtoPublish {
    publishMaven = true
    publishGithub = false
    mavenRepoName = "gtodysseyRepository"
    mavenRepoUrl = "releases" // 可选 releases / private
    minecraftVersion = "1.20.1"
    modLoader = "Forge"
    githubRepo = "owner/repo-name" // 启用 GitHub 发布时必填
    curseforgeProjectId = "1756992"
    curseforgeJavaVersion = "Java 21" // 运行时最低 Java（GTCEu 等依赖要求 21+）
    curseforgeEnvironment = "both"
    curseforgeRelations.putAll(
        mapOf(
            "gregtech-modern-gto-edition" to "required",
            "applied-energistics-2-gto-edition" to "required",
            "ars-nouveau-gto-edition" to "required",
            "botania-gto-edition" to "required",
            "gtoutils" to "required",
            "gtorender" to "required",
            "gto-seal-runtime" to "required",
            "data-sync-lib" to "required",
            "ad-astra" to "required",
        ),
    )
}

// ---------- IDE ----------

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
        // 仅供 IDEA 索引/跳转
        sourceDirs.add(file("GTOLib/src/main/java"))
    }
}

// VS Code Java Language Server 读 Eclipse classpath
eclipse {
    classpath {
        plusConfigurations.addAll(listOf(configurations.compileClasspath.get(), configurations.runtimeClasspath.get()))
        isDownloadSources = true
        isDownloadJavadoc = true
        file {
            whenMerged {
                val cp = this as Classpath
                // Kotlin 输出目录，让 JDT LS 能从 Java 解析 Kotlin 类
                cp.entries.add(SourceFolder("build/classes/kotlin/main", null))
                // 补上 Eclipse 漏掉的 compileClasspath 依赖（例如 ModDevGradle 转换后的 modImplementation 产物）
                val existing = cp.entries.filterIsInstance<Library>().map { it.path }.toMutableSet()
                configurations.compileClasspath.get().resolvedConfiguration.resolvedArtifacts.map { it.file }
                    .filter { it.exists() && it.name.endsWith(".jar") && existing.add(it.absolutePath) }
                    .forEach { cp.entries.add(Library(FileReferenceFactory().fromFile(it))) }
            }
        }
    }
}

// ---------- 格式化 ----------

spotless {
    encoding("UTF-8")
    java {
        target("src/main/java/**/*.java", "GTOLib/src/main/java/**/*.java")
        toggleOffOn()
        importOrderFile(file("gradle/spotless/spotless.importorder"))
        removeUnusedImports("cleanthat-javaparser-unnecessaryimport")
        endWithNewline()
        eclipse("4.31").configFile(file("gradle/spotless/spotless.eclipseformat.xml"))
    }
    kotlin {
        target("src/main/java/**/*.kt", "GTOLib/src/main/java/**/*.kt")
        ktlint().setEditorConfigPath("$rootDir/gradle/spotless/spotless.ktlint")
        trimTrailingWhitespace()
        endWithNewline()
    }
    json {
        target(
            "src/*/resources/**/*.json", "src/*/resources/**/*.mcmeta",
            "GTOLib/src/main/resources/**/*.json", "GTOLib/src/main/resources/**/*.mcmeta",
        )
        targetExclude("src/generated/resources/**")
        biome()
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
    }
}
