package com.gtocore.common.forge

import com.gtocore.client.Message
import com.gtocore.client.screen.MessageScreen

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent

import com.google.gson.GsonBuilder
import com.gregtechceu.gtceu.utils.TaskHandler

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.Predicate
import java.util.regex.Pattern

@OnlyIn(Dist.CLIENT)
object ClientForge {

    private val GSON = GsonBuilder().setPrettyPrinting().create()

    // 动态获取配置文件路径（每个存档/服务器独立）
    private fun getConfigPath(): Path {
        val mc = Minecraft.getInstance()
        return mc.gameDirectory.toPath().resolve("config/gtocore_client_messages.json")
    }

    // 版本格式验证 (x.x.x)
    private val VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+")

    // 日期格式验证 (yyyyMMdd)
    private val DATE_PATTERN = Pattern.compile("^\\d{8}$")

    // 东八区时区
    private val ZONE_UTC8: ZoneId = ZoneId.of("UTC+8")

    // 是否显示历史消息（默认只显示最近30天）
    private var showHistoricalMessages = false

    // 消息定义类
    // priority 优先级，默认0；非0则不受历史限制，越大越靠前
    data class MessageDefinition(val id: String, val gameVersion: String, val dateString: String, val languagePredicate: Predicate<String>, val messages: List<Component>, val priority: Int = 0) {
        val contentHash: String = buildString {
            append("version:$gameVersion\ndate:$dateString\npriority:$priority\n")
            messages.forEach { append(it.string).append("\n") }
        }.let { content ->
            MessageDigest.getInstance("SHA-256")
                .digest(content.toByteArray(StandardCharsets.UTF_8))
                .let { Base64.getEncoder().encodeToString(it) }
        }

        init {
            // 验证游戏版本格式
            require(VERSION_PATTERN.matcher(gameVersion).find()) {
                "Game version must start with x.x.x format, got: $gameVersion"
            }

            // 验证日期格式
            require(DATE_PATTERN.matcher(dateString).matches()) {
                "Date string must be in yyyyMMdd format (UTC+8), got: $dateString"
            }

            // 验证日期是否有效
            SimpleDateFormat("yyyyMMdd").apply { isLenient = false }.parse(dateString)
        }

        fun shouldShow(lang: String) = languagePredicate.test(lang)

        // 优先级非0的消息不受时间限制
        fun isRecent() = priority != 0 ||
            runCatching {
                val messageDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"))
                ChronoUnit.DAYS.between(messageDate, LocalDate.now(ZONE_UTC8)) <= 30
            }.getOrDefault(true)

        fun formatDate() = if (dateString.length == 8) {
            "${dateString.take(4)}-${dateString.substring(4, 6)}-${dateString.substring(6, 8)}"
        } else {
            dateString
        }
    }

    // 消息配置
    data class MessageConfig(val confirmedMessages: MutableMap<String, String> = mutableMapOf()) {
        fun confirmMessage(hash: String, id: String) = confirmedMessages.put(hash, id)
        fun isConfirmed(hash: String) = confirmedMessages.containsKey(hash)
    }

    // 消息列表
    val MESSAGE_DEFINITIONS: MutableList<MessageDefinition> = mutableListOf(
//        MessageDefinition(
//            id = "en_translation_notice",
//            gameVersion = "0.4.8",
//            dateString = "20251002",
//            languagePredicate = { it.lowercase(Locale.ROOT).startsWith("en") },
//            messages = listOf(
//                Component.literal("If you are using the English translation. This translation is community-maintained with help from AI. Have suggestions or corrections? No Chinese required.")
//                    .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
//                Component.literal("Thanks to all the contributors who have contributed to the English translation.")
//                    .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)),
//                Component.literal("Until pp>10 : xinxinsuried (666.28), 暮心 (406.55), KatNite (294.84), Rain-Flying (122.37), Xelo (108.85), Ormakent (84.7), totallynormal-tree (47.65), Hvm (42.85), LEgenD-Leo (32.02), nebniloc (19.35)")
//                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)),
//                Component.literal("Click Here to Join the English translation project on ParaTranz")
//                    .withStyle(
//                        Style.EMPTY.withColor(ChatFormatting.AQUA)
//                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://paratranz.cn/projects/16320")),
//                    ),
//                Component.literal("Click Here to Join the Discord for more information and updates")
//                    .withStyle(
//                        Style.EMPTY.withColor(ChatFormatting.GOLD)
//                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/ZSVb4dgVNB")),
//                    ),
//            ),
//        ),
//        MessageDefinition(
//            id = "zh_qq_group",
//            gameVersion = "0.4.8",
//            dateString = "20251004",
//            languagePredicate = { it.lowercase(Locale.ROOT).startsWith("zh") },
//            messages = listOf(
//                Component.literal("GTO寰宇重工集团·Alpha部门(927923997)，欢迎加入")
//                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)),
//                Component.literal("点击此处加入：https://qm.qq.com/q/evWgwcXde0")
//                    .withStyle(
//                        Style.EMPTY.withColor(ChatFormatting.GOLD)
//                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://qm.qq.com/q/evWgwcXde0")),
//                    ),
//            ),
//        ),
    )

    // 配置读写
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun loadConfig() = runCatching {
        val configPath = getConfigPath()
        if (Files.exists(configPath)) {
            Files.readString(configPath, StandardCharsets.UTF_8)
                .let { GSON.fromJson(it, Map::class.java) as Map<String, Any> }
                .getOrDefault("confirmedMessages", emptyMap<String, String>())
                .let { it as Map<String, String> }
                .toMutableMap()
                .let { MessageConfig(it) }
        } else {
            MessageConfig()
        }
    }.getOrElse {
        it.printStackTrace()
        MessageConfig()
    }

    @JvmStatic
    fun saveConfig(config: MessageConfig) = runCatching {
        val configPath = getConfigPath()
        Files.createDirectories(configPath.parent)
        Files.writeString(configPath, GSON.toJson(mapOf("confirmedMessages" to config.confirmedMessages)), StandardCharsets.UTF_8)
    }.onFailure { it.printStackTrace() }

    // 显示消息 GUI
    private fun showMessageScreen(msg: MessageDefinition, page: Int, total: Int) {
        val mc = Minecraft.getInstance()
        val langCode = mc.languageManager.selected
        val config = loadConfig()

        // 确认按钮的回调
        val onConfirm = {
            config.confirmMessage(msg.contentHash, msg.id)
            saveConfig(config)

            // 查找剩余消息
            MESSAGE_DEFINITIONS
                .filter { it.shouldShow(langCode) && !config.isConfirmed(it.contentHash) && (showHistoricalMessages || it.isRecent()) }
                .sortedByDescending { it.priority }
                .let { remaining ->
                    when {
                        // 有剩余消息，显示下一条
                        remaining.isNotEmpty() -> {
                            val allVisible = MESSAGE_DEFINITIONS
                                .filter { it.shouldShow(langCode) && (showHistoricalMessages || it.isRecent()) }
                                .sortedByDescending { it.priority }
                            val nextPage = allVisible.indexOf(remaining[0]) + 1
                            showMessageScreen(remaining[0], nextPage, allVisible.size)
                        }

                        // 检查是否有历史消息
                        MESSAGE_DEFINITIONS.any { it.shouldShow(langCode) && !config.isConfirmed(it.contentHash) && !it.isRecent() } && !showHistoricalMessages -> {
                            // 显示带有展开和标记全部已读按钮的完成界面
                            showCompletionScreen()
                        }

                        // 全部完成 - 关闭 UI
                        else -> {
                            mc.player?.sendSystemMessage(
                                Component.translatable(MessageScreen.ALL_CONFIRMED_KEY)
                                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
                            )
                            showHistoricalMessages = false
                            // 关闭当前屏幕
                            mc.setScreen(null)
                        }
                    }
                }
        }

        mc.setScreen(MessageScreen(msg, page, total, onConfirm))
    }

    // 显示完成界面（带有展开历史和标记全部已读的选项）
    private fun showCompletionScreen() {
        val mc = Minecraft.getInstance()
        val langCode = mc.languageManager.selected

        val completionMsg = MessageDefinition(
            id = "completion_screen",
            gameVersion = "0.0.0",
            dateString = "20250101",
            languagePredicate = { true },
            messages = listOf(
                Component.translatable(MessageScreen.ALL_RECENT_CONFIRMED_KEY).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
                Component.literal("").withStyle(Style.EMPTY),
                Component.translatable(MessageScreen.OLDER_MESSAGES_KEY).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
                Component.translatable(MessageScreen.VIEW_THEM_KEY).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)),
            ),
        )

        val onExpand: () -> Unit = {
            showHistoricalMessages = true
            MESSAGE_DEFINITIONS
                .filter { it.shouldShow(langCode) && !loadConfig().isConfirmed(it.contentHash) }
                .maxByOrNull { it.priority }
                ?.let { msg ->
                    val total = MESSAGE_DEFINITIONS
                        .filter { it.shouldShow(langCode) && !loadConfig().isConfirmed(it.contentHash) }
                        .size
                    showMessageScreen(msg, 1, total)
                }
        }

        val onMarkAll: () -> Unit = {
            val config = loadConfig()
            MESSAGE_DEFINITIONS
                .filter { it.shouldShow(langCode) && !config.isConfirmed(it.contentHash) }
                .also { messages ->
                    messages.forEach { config.confirmMessage(it.contentHash, it.id) }
                    saveConfig(config)
                    showHistoricalMessages = false
                    mc.player?.sendSystemMessage(
                        Component.translatable(MessageScreen.MARKED_READ_KEY, messages.size)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
                    )
                }
        }

        val onClose: () -> Unit = {
            mc.setScreen(null)
            showHistoricalMessages = false
        }

        mc.setScreen(MessageScreen(completionMsg, 0, 0, onClose, onExpand, onMarkAll))
    }

    // 【入口】登录时显示消息
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @JvmStatic
    fun onClientLoggedIn(event: ClientPlayerNetworkEvent.LoggingIn) {
        val mc = Minecraft.getInstance()
        val langCode = mc.languageManager.selected
        val config = loadConfig()

        MESSAGE_DEFINITIONS
            .filter { it.shouldShow(langCode) && !config.isConfirmed(it.contentHash) && (showHistoricalMessages || it.isRecent()) }
            .sortedByDescending { it.priority }
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?.let { msg ->
                val total = MESSAGE_DEFINITIONS
                    .filter { it.shouldShow(langCode) && !config.isConfirmed(it.contentHash) && (showHistoricalMessages || it.isRecent()) }
                    .size
                // 延迟显示 GUI，确保客户端完全加载
                TaskHandler.enqueueTask(event.player.clientLevel, { showMessageScreen(msg, 1, total) }, 40)
            }
        Message.serverLangSync.send({ buf: FriendlyByteBuf ->
            buf.writeUtf(langCode)
        })
    }
}
