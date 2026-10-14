package com.gtocore.api.gui;

import com.gtolib.GTOCore;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.styletemplate.WidgetIconAtlas;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public final class GTOGuiTextures {

    public static final ResourceTexture BOXED_BACKGROUND = new ResourceTexture(GTCEu.id("textures/gui/base/boxed_background.png"));

    public static final ResourceTexture REFRESH = getTexture("base/refresh");
    public static final ResourceTexture DELETE = getTexture("base/delete");
    public static final ResourceTexture ENERGY = getTexture("base/energy");
    public static final ResourceTexture[] VILLAGER_RECIPE_SLOTS = {
            getTexture("base/villager_recipe_slot_0"),
            getTexture("base/villager_recipe_slot_1"),
            getTexture("base/villager_recipe_slot_2") };

    public static final ResourceTexture PROGRESS_BAR_DATA_GENERATE_BASE = getTexture("progress_bar/progress_bar_data_generate_base");
    public static final ResourceTexture PROGRESS_BAR_EUREKA = getTexture("progress_bar/progress_bar_eureka_what");
    public static final ResourceTexture PROGRESS_BAR_RESEARCH_BASE = getTexture("progress_bar/progress_bar_research_base");
    public static final ResourceTexture CONDENSE_FROM_FLUID = getTexture("progress_bar/condense_from_fluid");
    public static final ResourceTexture CONDENSE_FROM_PLASMA = getTexture("progress_bar/condense_from_plasma");
    public static final ResourceTexture CONDENSE_FROM_MOLTEN = getTexture("progress_bar/condense_from_molten");
    public static final ResourceTexture PROGRESS_BAR_MINING_MODULE = getTexture("progress_bar/progress_bar_mining_module");
    public static final ResourceTexture PROGRESS_BAR_DRILLING_MODULE = getTexture("progress_bar/progress_bar_drilling_module");

    public static final ResourceTexture DATA_CRYSTAL_OVERLAY = getTexture("overlay/data_crystal_overlay");
    public static final ResourceTexture HIGH_SPEED_MODE = getTexture("overlay/high_speed_mode");
    public static final ResourceTexture COMPUTATION_RESEARCH_TAG_COMPONENT = getTexture("overlay/research_tag_component");
    public static final ResourceTexture INTELLIGENT_SCANNER = getTexture("overlay/intelligent_scanner");
    public static final ResourceTexture INTELLIGENT_SCANNER_1 = INTELLIGENT_SCANNER.getSubTexture(0, 0, 1, 0.25);
    public static final ResourceTexture INTELLIGENT_SCANNER_2 = INTELLIGENT_SCANNER.getSubTexture(0, 0.25, 1, 0.25);
    public static final ResourceTexture INTELLIGENT_SCANNER_3 = INTELLIGENT_SCANNER.getSubTexture(0, 0.5, 1, 0.25);
    public static final ResourceTexture INTELLIGENT_SCANNER_4 = INTELLIGENT_SCANNER.getSubTexture(0, 0.75, 1, 0.25);
    public static final ResourceTexture XP_ORBS = new ResourceTexture(RLUtils.mc("textures/entity/experience_orb.png"));
    public static final ResourceTexture SMALL_XP_ORB = XP_ORBS.getSubTexture(0.25, 0, 0.25, 0.25);
    public static final ResourceTexture LARGE_XP_ORB = XP_ORBS.getSubTexture(0, 0.5, 0.25, 0.25);

    /// 机器小组件（窗口左侧配置标签）图标，画法标准见 {@link WidgetIconAtlas}；通用的在 GTM WidgetIcons
    private static final WidgetIconAtlas WIDGET_ICONS = new WidgetIconAtlas(GTOCore.id("textures/gui/widget_icons.png"), 11);
    public static final IGuiTexture HUD_OFF = WIDGET_ICONS.icon(0, 0);
    public static final IGuiTexture HUD_ON = WIDGET_ICONS.icon(0, 1);
    /// 在传送网络中显示
    public static final IGuiTexture TRAVEL_OFF = WIDGET_ICONS.icon(1, 0);
    public static final IGuiTexture TRAVEL_ON = WIDGET_ICONS.icon(1, 1);
    /// 重复配方
    public static final IGuiTexture REPEAT_OFF = WIDGET_ICONS.icon(2, 0);
    public static final IGuiTexture REPEAT_ON = WIDGET_ICONS.icon(2, 1);
    /// 独立线程
    public static final IGuiTexture THREAD_OFF = WIDGET_ICONS.icon(3, 0);
    public static final IGuiTexture THREAD_ON = WIDGET_ICONS.icon(3, 1);
    public static final IGuiTexture PARALLEL = WIDGET_ICONS.icon(4);
    public static final IGuiTexture OVERCLOCK = WIDGET_ICONS.icon(5);
    public static final IGuiTexture HIGH_SPEED_OFF = WIDGET_ICONS.icon(6, 0);
    public static final IGuiTexture HIGH_SPEED_ON = WIDGET_ICONS.icon(6, 1);
    /// 前往星球（可用时彩色）
    public static final IGuiTexture PLANET_OFF = WIDGET_ICONS.icon(7, 0);
    public static final IGuiTexture PLANET_ON = WIDGET_ICONS.icon(7, 1);
    public static final IGuiTexture STRUCTURE_CHECK_OFF = WIDGET_ICONS.icon(8, 0);
    public static final IGuiTexture STRUCTURE_CHECK_ON = WIDGET_ICONS.icon(8, 1);
    /// 中子通量显示：平均 / 最低
    public static final IGuiTexture FLUX_AVG = WIDGET_ICONS.icon(9, 0);
    public static final IGuiTexture FLUX_MIN = WIDGET_ICONS.icon(9, 1);
    /// 收藏（8×8 画法放大 2 倍存放，用在 14 像素的小图标按钮里按 8 像素绘制）
    public static final IGuiTexture FAVORITE_OFF = WIDGET_ICONS.icon(10, 0);
    public static final IGuiTexture FAVORITE_ON = WIDGET_ICONS.icon(10, 1);

    private static ResourceTexture getTexture(String string) {
        return new ResourceTexture(GTOCore.id("textures/gui/" + string + ".png"));
    }
}
