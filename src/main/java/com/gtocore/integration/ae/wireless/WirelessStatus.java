package com.gtocore.integration.ae.wireless;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.network.chat.Component;

/** 无线网络操作的结果。失败一律带明确原因，显示在界面状态行或 actionbar，不静默。 */
@DataGeneratorScanned
public enum WirelessStatus {

    OK,
    NOT_FOUND,
    NO_PERMISSION_MACHINE,
    NO_PERMISSION_NETWORK,
    NAME_TAKEN,
    NAME_INVALID,
    NOT_ALLOWED,
    LIMIT_REACHED,
    UNAVAILABLE;

    @RegisterLanguage(cn = "操作完成", en = "Done")
    static final String KEY_OK = "gtocore.wireless.status.ok";
    @RegisterLanguage(cn = "网络不存在", en = "Network not found")
    static final String KEY_NOT_FOUND = "gtocore.wireless.status.not_found";
    @RegisterLanguage(cn = "你无权管理这台机器", en = "You cannot manage this machine")
    static final String KEY_NO_PERMISSION_MACHINE = "gtocore.wireless.status.no_permission_machine";
    @RegisterLanguage(cn = "你无权使用这个网络", en = "You cannot use this network")
    static final String KEY_NO_PERMISSION_NETWORK = "gtocore.wireless.status.no_permission_network";
    @RegisterLanguage(cn = "已有同名网络", en = "A network with this name already exists")
    static final String KEY_NAME_TAKEN = "gtocore.wireless.status.name_taken";
    @RegisterLanguage(cn = "名称须为 1～32 个字符", en = "Name must be 1-32 characters")
    static final String KEY_NAME_INVALID = "gtocore.wireless.status.name_invalid";
    @RegisterLanguage(cn = "这台机器不能连接无线网络", en = "This machine cannot join wireless networks")
    static final String KEY_NOT_ALLOWED = "gtocore.wireless.status.not_allowed";
    @RegisterLanguage(cn = "你创建的网络已达上限（" + WirelessNetworks.MAX_NETWORKS_PER_PLAYER + " 个）", en = "You have reached the limit of " + WirelessNetworks.MAX_NETWORKS_PER_PLAYER + " networks")
    static final String KEY_LIMIT_REACHED = "gtocore.wireless.status.limit_reached";
    @RegisterLanguage(cn = "无线网络数据不可用（存档文件损坏或来自更新的版本），请查看服务器日志", en = "Wireless network data unavailable (save file damaged or from a newer version); see the server log")
    static final String KEY_UNAVAILABLE = "gtocore.wireless.status.unavailable";

    public boolean ok() {
        return this == OK;
    }

    public Component message() {
        return switch (this) {
            case OK -> Component.translatable(KEY_OK);
            case NOT_FOUND -> Component.translatable(KEY_NOT_FOUND);
            case NO_PERMISSION_MACHINE -> Component.translatable(KEY_NO_PERMISSION_MACHINE);
            case NO_PERMISSION_NETWORK -> Component.translatable(KEY_NO_PERMISSION_NETWORK);
            case NAME_TAKEN -> Component.translatable(KEY_NAME_TAKEN);
            case NAME_INVALID -> Component.translatable(KEY_NAME_INVALID);
            case NOT_ALLOWED -> Component.translatable(KEY_NOT_ALLOWED);
            case LIMIT_REACHED -> Component.translatable(KEY_LIMIT_REACHED);
            case UNAVAILABLE -> Component.translatable(KEY_UNAVAILABLE);
        };
    }
}
