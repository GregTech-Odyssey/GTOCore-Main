package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.flow.FlowChart;
import com.gregtechceu.gtceu.uipro.flow.FlowNode;
import com.gregtechceu.gtceu.uipro.flow.FlowParts;
import com.gregtechceu.gtceu.uipro.flow.FlowState;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.flow.IssueLine;
import com.gregtechceu.gtceu.uiwidgets.flow.IssueView;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeDiagnoser;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeIssue;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DataGeneratorScanned
public final class NeutronActivatorFlowPage {

    private static final int SIDE_WIDTH = 104;
    private static final int CORE_WIDTH = 124;
    private static final int REFRESH_TICKS = 20;
    private static final int PROGRESS_TICKS = 4;
    private static final int OVERLOAD_EV = NeutronActivatorMachine.EXPLOSION_EV - 50000000;
    private static final int MEV = 1000000;
    private static final int KEV = 1000;
    private static final int AVERAGE_EV_PER_EU = 15;
    private static final int RANGE_COLOR = 0xFF2E7D1E;
    private static final int EXPLOSION_COLOR = 0xFFC24D3E;

    @RegisterLanguage(cn = "中子加速器", en = "Neutron Accelerators")
    private static final String LANG_ACCELERATOR = "gtocore.machine.neutron_activator.flow.accelerator";
    @RegisterLanguage(cn = "中子动能", en = "Kinetic Energy")
    private static final String LANG_KINETIC = "gtocore.machine.neutron_activator.flow.kinetic";
    @RegisterLanguage(cn = "衰减与慢化", en = "Decay & Moderation")
    private static final String LANG_LOSS = "gtocore.machine.neutron_activator.flow.loss";
    @RegisterLanguage(cn = "物料输入", en = "Material Input")
    private static final String LANG_INPUT = "gtocore.machine.neutron_activator.flow.input";
    @RegisterLanguage(cn = "中子活化", en = "Activation")
    private static final String LANG_RECIPE = "gtocore.machine.neutron_activator.flow.recipe";
    @RegisterLanguage(cn = "产物输出", en = "Product Output")
    private static final String LANG_OUTPUT = "gtocore.machine.neutron_activator.flow.output";
    @RegisterLanguage(cn = "能源接收器", en = "Energy Acceptor")
    private static final String LANG_ENERGY = "gtocore.machine.neutron_activator.flow.energy";

    @RegisterLanguage(cn = "状态", en = "State")
    private static final String LANG_STATE = "gtocore.machine.neutron_activator.flow.state";
    @RegisterLanguage(cn = "动能", en = "Energy")
    private static final String LANG_ENERGY_BAR = "gtocore.machine.neutron_activator.flow.energy_bar";
    @RegisterLanguage(cn = "进度", en = "Progress")
    private static final String LANG_PROGRESS = "gtocore.machine.neutron_activator.flow.progress";
    @RegisterLanguage(cn = "已安装", en = "Installed")
    private static final String LANG_INSTALLED = "gtocore.machine.neutron_activator.flow.installed";
    @RegisterLanguage(cn = "有电", en = "Powered")
    private static final String LANG_POWERED = "gtocore.machine.neutron_activator.flow.powered";
    @RegisterLanguage(cn = "增速", en = "Gain")
    private static final String LANG_GAIN = "gtocore.machine.neutron_activator.flow.gain";
    @RegisterLanguage(cn = "目标", en = "Target")
    private static final String LANG_TARGET = "gtocore.machine.neutron_activator.flow.target";
    @RegisterLanguage(cn = "净变化", en = "Net")
    private static final String LANG_NET = "gtocore.machine.neutron_activator.flow.net";
    @RegisterLanguage(cn = "衰减", en = "Decay")
    private static final String LANG_DECAY = "gtocore.machine.neutron_activator.flow.decay";
    @RegisterLanguage(cn = "慢化剂", en = "Moderator")
    private static final String LANG_MODERATOR = "gtocore.machine.neutron_activator.flow.moderator";
    @RegisterLanguage(cn = "可吸收", en = "Absorbs")
    private static final String LANG_ABSORB = "gtocore.machine.neutron_activator.flow.absorb";
    @RegisterLanguage(cn = "输入单元", en = "Units")
    private static final String LANG_INPUT_UNITS = "gtocore.machine.neutron_activator.flow.input_units";
    @RegisterLanguage(cn = "输出单元", en = "Units")
    private static final String LANG_OUTPUT_UNITS = "gtocore.machine.neutron_activator.flow.output_units";
    @RegisterLanguage(cn = "动能消耗", en = "Drain")
    private static final String LANG_DRAIN = "gtocore.machine.neutron_activator.flow.drain";
    @RegisterLanguage(cn = "耗时", en = "Duration")
    private static final String LANG_DURATION = "gtocore.machine.neutron_activator.flow.duration";
    @RegisterLanguage(cn = "耗能", en = "Usage")
    private static final String LANG_USAGE = "gtocore.machine.neutron_activator.flow.usage";
    @RegisterLanguage(cn = "电压", en = "Tier")
    private static final String LANG_VOLTAGE = "gtocore.machine.neutron_activator.flow.voltage";

    @RegisterLanguage(cn = "%s ~ %s MeV", en = "%s ~ %s MeV")
    private static final String LANG_RANGE = "gtocore.machine.neutron_activator.flow.range";
    @RegisterLanguage(cn = "%s / %s", en = "%s / %s")
    private static final String LANG_RATIO = "gtocore.machine.neutron_activator.flow.ratio";
    @RegisterLanguage(cn = "≈ %s", en = "≈ %s")
    private static final String LANG_ABOUT = "gtocore.machine.neutron_activator.flow.about";
    @RegisterLanguage(cn = "%s 个", en = "%s")
    private static final String LANG_COUNT = "gtocore.machine.neutron_activator.flow.count";
    @RegisterLanguage(cn = "暂停", en = "Paused")
    private static final String LANG_PAUSED = "gtocore.machine.neutron_activator.flow.paused";
    @RegisterLanguage(cn = "不消耗", en = "None")
    private static final String LANG_NO_DRAIN = "gtocore.machine.neutron_activator.flow.no_drain";

    @RegisterLanguage(cn = "未安装", en = "None Installed")
    private static final String LANG_ACC_NONE = "gtocore.machine.neutron_activator.flow.acc_none";
    @RegisterLanguage(cn = "结构中没有中子加速器，中子动能无法提升。", en = "The structure has no neutron accelerator; kinetic energy cannot rise.")
    private static final String LANG_ACC_NONE_DESC = "gtocore.machine.neutron_activator.flow.acc_none.desc";
    @RegisterLanguage(cn = "已停用", en = "Disabled")
    private static final String LANG_ACC_OFF = "gtocore.machine.neutron_activator.flow.acc_off";
    @RegisterLanguage(cn = "所有中子加速器均已停用。", en = "All neutron accelerators are disabled.")
    private static final String LANG_ACC_OFF_DESC = "gtocore.machine.neutron_activator.flow.acc_off.desc";
    @RegisterLanguage(cn = "缺电", en = "No Power")
    private static final String LANG_ACC_EMPTY = "gtocore.machine.neutron_activator.flow.acc_empty";
    @RegisterLanguage(cn = "中子加速器没有储能，无法加速中子。", en = "The neutron accelerators hold no energy and cannot accelerate neutrons.")
    private static final String LANG_ACC_EMPTY_DESC = "gtocore.machine.neutron_activator.flow.acc_empty.desc";
    @RegisterLanguage(cn = "加速中", en = "Accelerating")
    private static final String LANG_ACC_ON = "gtocore.machine.neutron_activator.flow.acc_on";
    @RegisterLanguage(cn = "中子加速器正在消耗电能提升中子动能。", en = "The neutron accelerators are converting energy into kinetic energy.")
    private static final String LANG_ACC_ON_DESC = "gtocore.machine.neutron_activator.flow.acc_on.desc";
    @RegisterLanguage(cn = "不使用", en = "Bypassed")
    private static final String LANG_ACC_BYPASS = "gtocore.machine.neutron_activator.flow.acc_bypass";
    @RegisterLanguage(cn = "能源接收器已激活，中子动能由配方直接设定，中子加速器不参与。", en = "The Energy Acceptor is active; kinetic energy is set by the recipe and the accelerators are not used.")
    private static final String LANG_ACC_BYPASS_DESC = "gtocore.machine.neutron_activator.flow.acc_bypass.desc";
    @RegisterLanguage(cn = "%s：%s / %s EU，%s", en = "%s: %s / %s EU, %s")
    private static final String LANG_ACC_LINE = "gtocore.machine.neutron_activator.flow.acc_line";
    @RegisterLanguage(cn = "启用", en = "enabled")
    private static final String LANG_ENABLED = "gtocore.machine.neutron_activator.flow.enabled";
    @RegisterLanguage(cn = "停用", en = "disabled")
    private static final String LANG_DISABLED = "gtocore.machine.neutron_activator.flow.disabled";
    @RegisterLanguage(cn = "每台每刻至多消耗 0.8 倍电压的 EU，每点 EU 转化为 10~20 eV 中子动能，再乘以效率 %s。", en = "Each accelerator uses up to 0.8× its voltage in EU per tick; each EU becomes 10~20 eV of kinetic energy, multiplied by the efficiency %s.")
    private static final String LANG_ACC_RULE = "gtocore.machine.neutron_activator.flow.acc_rule";
    @RegisterLanguage(cn = "增速按有电加速器满功率、平均 15 eV/EU 估算。", en = "The gain assumes powered accelerators at full draw and an average of 15 eV per EU.")
    private static final String LANG_ACC_ESTIMATE = "gtocore.machine.neutron_activator.flow.acc_estimate";

    @RegisterLanguage(cn = "未连接", en = "Unlinked")
    private static final String LANG_OFFLINE = "gtocore.machine.neutron_activator.flow.offline";
    @RegisterLanguage(cn = "供给中", en = "Supplying")
    private static final String LANG_KE_SUPPLY = "gtocore.machine.neutron_activator.flow.ke_supply";
    @RegisterLanguage(cn = "配方运行中，每刻扣除动能消耗。", en = "The recipe is running; the drain is deducted every tick.")
    private static final String LANG_KE_SUPPLY_DESC = "gtocore.machine.neutron_activator.flow.ke_supply.desc";
    @RegisterLanguage(cn = "运行中", en = "Running")
    private static final String LANG_KE_HELD = "gtocore.machine.neutron_activator.flow.ke_held";
    @RegisterLanguage(cn = "配方运行中，不消耗中子动能。", en = "The recipe is running and does not consume kinetic energy.")
    private static final String LANG_KE_HELD_DESC = "gtocore.machine.neutron_activator.flow.ke_held.desc";
    @RegisterLanguage(cn = "不足以维持", en = "Too Low")
    private static final String LANG_KE_STARVED = "gtocore.machine.neutron_activator.flow.ke_starved";
    @RegisterLanguage(cn = "中子动能低于每刻消耗，配方已暂停，待动能回升后继续。", en = "Kinetic energy is below the per-tick drain; the recipe is paused until it recovers.")
    private static final String LANG_KE_STARVED_DESC = "gtocore.machine.neutron_activator.flow.ke_starved.desc";
    @RegisterLanguage(cn = "自动设定", en = "Automatic")
    private static final String LANG_KE_AUTO = "gtocore.machine.neutron_activator.flow.ke_auto";
    @RegisterLanguage(cn = "能源接收器已激活：配方开始时中子动能直接设为区间中点，结束后归零。", en = "Energy Acceptor active: kinetic energy is set to the middle of the range when a recipe starts and reset to zero when it ends.")
    private static final String LANG_KE_AUTO_DESC = "gtocore.machine.neutron_activator.flow.ke_auto.desc";
    @RegisterLanguage(cn = "接近爆炸", en = "Near Explosion")
    private static final String LANG_KE_DANGER = "gtocore.machine.neutron_activator.flow.ke_danger";
    @RegisterLanguage(cn = "中子动能接近 1,200 MeV，超过后机器会爆炸。", en = "Kinetic energy is close to 1,200 MeV; the machine explodes above it.")
    private static final String LANG_KE_DANGER_DESC = "gtocore.machine.neutron_activator.flow.ke_danger.desc";
    @RegisterLanguage(cn = "无动能", en = "Empty")
    private static final String LANG_KE_EMPTY = "gtocore.machine.neutron_activator.flow.ke_empty";
    @RegisterLanguage(cn = "当前没有中子动能。", en = "There is no kinetic energy.")
    private static final String LANG_KE_EMPTY_DESC = "gtocore.machine.neutron_activator.flow.ke_empty.desc";
    @RegisterLanguage(cn = "无目标", en = "No Target")
    private static final String LANG_KE_NO_TARGET = "gtocore.machine.neutron_activator.flow.ke_no_target";
    @RegisterLanguage(cn = "尚未匹配到配方，没有目标区间。", en = "No recipe is matched yet, so there is no target range.")
    private static final String LANG_KE_NO_TARGET_DESC = "gtocore.machine.neutron_activator.flow.ke_no_target.desc";
    @RegisterLanguage(cn = "偏低·上升中", en = "Low · Rising")
    private static final String LANG_KE_LOW_RISING = "gtocore.machine.neutron_activator.flow.ke_low_rising";
    @RegisterLanguage(cn = "低于区间", en = "Below Range")
    private static final String LANG_KE_LOW = "gtocore.machine.neutron_activator.flow.ke_low";
    @RegisterLanguage(cn = "中子动能低于配方区间，需要中子加速器提升。", en = "Kinetic energy is below the recipe range; neutron accelerators must raise it.")
    private static final String LANG_KE_LOW_DESC = "gtocore.machine.neutron_activator.flow.ke_low.desc";
    @RegisterLanguage(cn = "偏高·下降中", en = "High · Falling")
    private static final String LANG_KE_HIGH_FALLING = "gtocore.machine.neutron_activator.flow.ke_high_falling";
    @RegisterLanguage(cn = "高于区间", en = "Above Range")
    private static final String LANG_KE_HIGH = "gtocore.machine.neutron_activator.flow.ke_high";
    @RegisterLanguage(cn = "中子动能高于配方区间，可停用加速器等待衰减，或投入石墨粉、铍粉吸收。", en = "Kinetic energy is above the recipe range; disable the accelerators and wait for decay, or insert graphite or beryllium dust to absorb it.")
    private static final String LANG_KE_HIGH_DESC = "gtocore.machine.neutron_activator.flow.ke_high.desc";
    @RegisterLanguage(cn = "区间内", en = "In Range")
    private static final String LANG_KE_IN_RANGE = "gtocore.machine.neutron_activator.flow.ke_in_range";
    @RegisterLanguage(cn = "中子动能处于配方区间内，配方可以开始。", en = "Kinetic energy is inside the recipe range; the recipe can start.")
    private static final String LANG_KE_IN_RANGE_DESC = "gtocore.machine.neutron_activator.flow.ke_in_range.desc";
    @RegisterLanguage(cn = "当前：%s eV", en = "Current: %s eV")
    private static final String LANG_KE_CURRENT = "gtocore.machine.neutron_activator.flow.ke_current";
    @RegisterLanguage(cn = "目标区间：%s（开始配方时须严格处于区间内）", en = "Target range: %s (must be strictly inside when a recipe starts)")
    private static final String LANG_KE_TARGET_LINE = "gtocore.machine.neutron_activator.flow.ke_target_line";
    @RegisterLanguage(cn = "目标来自运行中的配方", en = "Target from the running recipe")
    private static final String LANG_KE_FROM_RUNNING = "gtocore.machine.neutron_activator.flow.ke_from_running";
    @RegisterLanguage(cn = "目标来自等待动能的配方", en = "Target from the recipe waiting for kinetic energy")
    private static final String LANG_KE_FROM_WAITING = "gtocore.machine.neutron_activator.flow.ke_from_waiting";
    @RegisterLanguage(cn = "净变化：近 1 秒的实测变化量", en = "Net: change measured over the last second")
    private static final String LANG_KE_NET_LINE = "gtocore.machine.neutron_activator.flow.ke_net_line";
    @RegisterLanguage(cn = "超过 1,200 MeV（红线）时机器爆炸", en = "The machine explodes above 1,200 MeV (red line)")
    private static final String LANG_KE_EXPLOSION_LINE = "gtocore.machine.neutron_activator.flow.ke_explosion_line";
    @RegisterLanguage(cn = "爆炸阈值 1,200 MeV", en = "Explosion threshold 1,200 MeV")
    private static final String LANG_CALLOUT_TITLE = "gtocore.machine.neutron_activator.flow.callout.title";
    @RegisterLanguage(cn = "当前 %s，距阈值 %s", en = "Now %s, %s below the threshold")
    private static final String LANG_CALLOUT_NOW = "gtocore.machine.neutron_activator.flow.callout.now";
    @RegisterLanguage(cn = "净变化 %s，约 %s 秒后到达阈值", en = "Net change %s, reaches the threshold in about %s s")
    private static final String LANG_CALLOUT_ETA = "gtocore.machine.neutron_activator.flow.callout.eta";
    @RegisterLanguage(cn = "超过阈值时机器爆炸。关闭中子加速器，或向输入总线放入石墨粉、铍粉（每个吸收 10 MeV）可降低中子动能。", en = "The machine explodes above the threshold. Turn off the neutron accelerators, or put graphite or beryllium dust into the input bus (each absorbs 10 MeV), to lower the kinetic energy.")
    private static final String LANG_CALLOUT_HINT = "gtocore.machine.neutron_activator.flow.callout.hint";
    @RegisterLanguage(cn = "中子传感器：输出红石信号 %s", en = "Neutron Sensor: redstone output %s")
    private static final String LANG_KE_SENSOR_LINE = "gtocore.machine.neutron_activator.flow.ke_sensor_line";

    @RegisterLanguage(cn = "有慢化剂", en = "Moderator Present")
    private static final String LANG_LOSS_ABSORB = "gtocore.machine.neutron_activator.flow.loss_absorb";
    @RegisterLanguage(cn = "输入总线中有石墨粉或铍粉，总线物品变动时按当前动能消耗并吸收中子动能。", en = "Graphite or beryllium dust is in the input buses; it is consumed to absorb kinetic energy whenever the bus contents change.")
    private static final String LANG_LOSS_ABSORB_DESC = "gtocore.machine.neutron_activator.flow.loss_absorb.desc";
    @RegisterLanguage(cn = "自然衰减", en = "Decaying")
    private static final String LANG_LOSS_DECAY = "gtocore.machine.neutron_activator.flow.loss_decay";
    @RegisterLanguage(cn = "没有中子加速器工作，中子动能每秒衰减 72 keV。", en = "No accelerator is working; kinetic energy decays by 72 keV per second.")
    private static final String LANG_LOSS_DECAY_DESC = "gtocore.machine.neutron_activator.flow.loss_decay.desc";
    @RegisterLanguage(cn = "无损耗", en = "Stable")
    private static final String LANG_LOSS_NONE = "gtocore.machine.neutron_activator.flow.loss_none";
    @RegisterLanguage(cn = "加速器工作时不衰减；输入总线中没有慢化剂。", en = "No decay while accelerating; no moderator in the input buses.")
    private static final String LANG_LOSS_NONE_DESC = "gtocore.machine.neutron_activator.flow.loss_none.desc";
    @RegisterLanguage(cn = "无加速器工作时，中子动能每秒衰减 72 keV。", en = "While no accelerator is working, kinetic energy decays by 72 keV per second.")
    private static final String LANG_LOSS_RULE_DECAY = "gtocore.machine.neutron_activator.flow.loss_rule_decay";
    @RegisterLanguage(cn = "输入总线中的石墨粉、铍粉每个吸收 10 MeV，按当前动能折算消耗数量（至少 1 个）。", en = "Each graphite or beryllium dust in the input buses absorbs 10 MeV; the number consumed follows the current energy (at least one).")
    private static final String LANG_LOSS_RULE_ABSORB = "gtocore.machine.neutron_activator.flow.loss_rule_absorb";
    @RegisterLanguage(cn = "能源接收器激活时不衰减。", en = "No decay while the Energy Acceptor is active.")
    private static final String LANG_LOSS_RULE_AUTO = "gtocore.machine.neutron_activator.flow.loss_rule_auto";

    @RegisterLanguage(cn = "已投料", en = "Loaded")
    private static final String LANG_INPUT_LOADED = "gtocore.machine.neutron_activator.flow.input_loaded";
    @RegisterLanguage(cn = "物料已匹配配方", en = "Matched")
    private static final String LANG_INPUT_MATCHED = "gtocore.machine.neutron_activator.flow.input_matched";
    @RegisterLanguage(cn = "待匹配", en = "Waiting")
    private static final String LANG_INPUT_WAITING = "gtocore.machine.neutron_activator.flow.input_waiting";
    @RegisterLanguage(cn = "输入物料已满足配方，仅等待中子动能进入区间。", en = "The inputs satisfy a recipe; only kinetic energy is not yet in range.")
    private static final String LANG_INPUT_MATCHED_DESC = "gtocore.machine.neutron_activator.flow.input_matched.desc";
    @RegisterLanguage(cn = "待产出", en = "Standby")
    private static final String LANG_OUTPUT_IDLE = "gtocore.machine.neutron_activator.flow.output_idle";
    @RegisterLanguage(cn = "结构中没有输出仓，产物将全部销毁。", en = "The structure has no output hatch; all products will be voided.")
    private static final String LANG_OUTPUT_VOID_ALL_DESC = "gtocore.machine.neutron_activator.flow.output_void_all_desc";

    @RegisterLanguage(cn = "动能不在区间", en = "Out of Range")
    private static final String LANG_RECIPE_RANGE = "gtocore.machine.neutron_activator.flow.recipe_range";
    @RegisterLanguage(cn = "高度 %s，效率 %s（0.95^(高度−4)）", en = "Height %s, efficiency %s (0.95^(height−4))")
    private static final String LANG_RECIPE_HEIGHT = "gtocore.machine.neutron_activator.flow.recipe_height";
    @RegisterLanguage(cn = "动能消耗 = 配方每刻 %s keV × 倍率 %s（max(1, 并行 %s ^1.2 × 效率)）", en = "Drain = recipe %s keV per tick × multiplier %s (max(1, parallel %s ^1.2 × efficiency))")
    private static final String LANG_RECIPE_DRAIN_RULE = "gtocore.machine.neutron_activator.flow.recipe_drain_rule";
    @RegisterLanguage(cn = "运行时不消耗中子动能", en = "Kinetic energy is not consumed while running")
    private static final String LANG_RECIPE_NO_DRAIN_RULE = "gtocore.machine.neutron_activator.flow.recipe_no_drain_rule";
    @RegisterLanguage(cn = "能源接收器模式：耗时 ×20%%，耗电为（区间下限 + 上限）× 5 EU/t", en = "Energy Acceptor mode: duration ×20%%, power (range min + max) × 5 EU/t")
    private static final String LANG_RECIPE_AUTO_RULE = "gtocore.machine.neutron_activator.flow.recipe_auto_rule";

    @RegisterLanguage(cn = "未激活", en = "Inactive")
    private static final String LANG_ENERGY_OFF = "gtocore.machine.neutron_activator.flow.energy_off";
    @RegisterLanguage(cn = "能源接收器未激活，中子动能由加速器提供。可用本节点中的开关切换。", en = "The Energy Acceptor is inactive; accelerators supply kinetic energy. Use the switch in this node to toggle it.")
    private static final String LANG_ENERGY_OFF_DESC = "gtocore.machine.neutron_activator.flow.energy_off.desc";
    @RegisterLanguage(cn = "能源接收器激活后消耗电力，并按配方自动设定中子动能。", en = "When active, the Energy Acceptor uses power and sets kinetic energy from the recipe.")
    private static final String LANG_ENERGY_RULE = "gtocore.machine.neutron_activator.flow.energy_rule";
    @RegisterLanguage(cn = "启用能源接收器", en = "Enable Energy Acceptor")
    private static final String LANG_ENERGY_SWITCH = "gtocore.machine.neutron_activator.flow.energy_switch";
    @RegisterLanguage(cn = "能源接收段未搭建", en = "Energy Acceptor Section not built")
    private static final String LANG_NO_RECEIVER = "gtocore.machine.neutron_activator.flow.no_receiver";
    @RegisterLanguage(cn = "结构中没有能源接收段，能源接收器不可用，中子动能由加速器提供。", en = "The structure has no Energy Acceptor Section; the Energy Acceptor is unavailable and accelerators supply kinetic energy.")
    private static final String LANG_NO_RECEIVER_DESC = "gtocore.machine.neutron_activator.flow.no_receiver.desc";

    private NeutronActivatorFlowPage() {}

    static Widget create(NeutronActivatorMachine machine, FancyMachineUIWidget window) {
        var status = new Status(machine);
        var chart = new FlowChart(0, SIDE_WIDTH, CORE_WIDTH, SIDE_WIDTH);
        var accelerator = acceleratorNode(chart, status);
        var kinetic = kineticNode(chart, status);
        var loss = lossNode(chart, status);
        var input = inputNode(chart, status);
        var recipe = recipeNode(chart, machine, status);
        var output = outputNode(chart, status);
        chart.link(accelerator, kinetic);
        chart.link(kinetic, loss).follow(loss);
        chart.link(kinetic, recipe);
        chart.link(input, recipe);
        chart.link(recipe, output).follow(output);
        if (status.vortex != null) chart.link(energyNode(chart, status), recipe);
        return UIElement.column(LayoutStyle.AUTO).addChild(chart.toView(window, false, machine.getDefinition().getId().toString()));
    }

    static Widget details(NeutronActivatorMachine machine) {
        var status = new Status(machine);
        return MachineDisplay.column().addChild(MachineDisplay.display(machine, MachineDisplay.DETAILS_HEIGHT - ProgressBar.HEIGHT - UISizes.SECTION_GAP)).addChild(kineticBar(status));
    }

    private static ProgressBar kineticBar(Status status) {
        return ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_ENERGY_BAR), UITheme.FLOW_CYAN_LIGHT, status::kinetic)
                .currentOnly().setUnit("eV").bindRange(status::window, RANGE_COLOR).addMarker(NeutronActivatorMachine.EXPLOSION_EV, EXPLOSION_COLOR, status::explosionCallout);
    }

    private static FlowNode acceleratorNode(FlowChart chart, Status status) {
        var node = chart.node(0, 0).bindState(status::acceleratorState).bindDetail(status::acceleratorDetail);
        node.addChildren(FlowParts.header(ItemView.of(GTOMachines.NEUTRON_ACCELERATOR[GTValues.IV].asStack()), LANG_ACCELERATOR),
                new IssueLine(LayoutStyle.AUTO, null, status::acceleratorView),
                StatusLine.of(LayoutStyle.AUTO, LANG_INSTALLED, status::installedText),
                StatusLine.of(LayoutStyle.AUTO, LANG_POWERED, status::poweredText),
                StatusLine.of(LayoutStyle.AUTO, LANG_GAIN, status::gainText));
        return node;
    }

    private static FlowNode kineticNode(FlowChart chart, Status status) {
        var node = chart.node(0, 1).bindState(status::kineticState).bindDetail(status::kineticDetail);
        node.addChildren(FlowParts.header(ItemView.of(new ItemStack(GTOBlocks.SPEEDING_PIPE.get())), LANG_KINETIC),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status::kineticView),
                kineticBar(status),
                StatusLine.of(LayoutStyle.AUTO, LANG_TARGET, status::targetText),
                StatusLine.of(LayoutStyle.AUTO, LANG_NET, status::netText).bindLevel(status::netLevel));
        return node;
    }

    private static FlowNode lossNode(FlowChart chart, Status status) {
        var node = chart.node(0, 2).bindState(status::lossState).bindDetail(status::lossDetail);
        node.addChildren(FlowParts.header(ItemView.of(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Graphite)), LANG_LOSS),
                new IssueLine(LayoutStyle.AUTO, null, status::lossView),
                StatusLine.of(LayoutStyle.AUTO, LANG_DECAY, status::decayText).bindLevel(status::decayLevel),
                StatusLine.of(LayoutStyle.AUTO, LANG_MODERATOR, status::moderatorText),
                StatusLine.of(LayoutStyle.AUTO, LANG_ABSORB, status::absorbText));
        return node;
    }

    private static FlowNode inputNode(FlowChart chart, Status status) {
        var node = chart.node(1, 0).bindState(status::inputState).bindDetail(status::inputDetail);
        node.addChildren(FlowParts.header(ItemView.of(GTMachines.ITEM_IMPORT_BUS[GTValues.LV].asStack()), LANG_INPUT),
                new IssueLine(LayoutStyle.AUTO, null, status::inputView),
                StatusLine.of(LayoutStyle.AUTO, LANG_INPUT_UNITS, status::inputUnitsText));
        return node;
    }

    private static FlowNode recipeNode(FlowChart chart, NeutronActivatorMachine machine, Status status) {
        var node = chart.node(1, 1).bindState(status::recipeState).bindDetail(status::recipeDetail);
        node.addChildren(FlowParts.header(ItemView.of(machine.getDefinition().asStack()), LANG_RECIPE),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status::recipeView),
                ProgressBar.of(LayoutStyle.AUTO, Component.translatable(LANG_PROGRESS), UITheme.FLOW_CYAN_LIGHT, status::progress),
                StatusLine.of(LayoutStyle.AUTO, LANG_DRAIN, status::drainText),
                StatusLine.of(LayoutStyle.AUTO, LANG_DURATION, status::durationText));
        return node;
    }

    private static FlowNode outputNode(FlowChart chart, Status status) {
        var node = chart.node(1, 2).bindState(status::outputState).bindDetail(status::outputDetail);
        node.addChildren(FlowParts.header(ItemView.of(GTMachines.ITEM_EXPORT_BUS[GTValues.LV].asStack()), LANG_OUTPUT),
                new IssueLine(LayoutStyle.AUTO, null, status::outputView),
                StatusLine.of(LayoutStyle.AUTO, LANG_OUTPUT_UNITS, status::outputUnitsText));
        return node;
    }

    private static FlowNode energyNode(FlowChart chart, Status status) {
        var node = chart.node(2, 1).bindState(status::energyState).bindDetail(status::energyDetail);
        var vortex = status.vortex;
        var toggle = Switch.of(vortex::isEnergySwitchOn, vortex::setEnergySwitch)
                .disabled(() -> !vortex.hasEnergyReceiver(), LANG_NO_RECEIVER);
        node.addChildren(FlowParts.header(ItemView.of(FlowParts.energyIcon(node)), LANG_ENERGY),
                UIElement.centeredRow(UISizes.CONTROL_HEIGHT)
                        .addChildren(TextLine.translatable(0, LANG_ENERGY_SWITCH).layout(l -> l.flex(1)), toggle),
                new IssueLine(LayoutStyle.AUTO, Component.translatable(LANG_STATE), status::energyView),
                StatusLine.of(LayoutStyle.AUTO, LANG_USAGE, status::usageText),
                StatusLine.of(LayoutStyle.AUTO, LANG_VOLTAGE, status::voltageText),
                ProgressBar.of(LayoutStyle.AUTO, Component.empty(), UITheme.FLOW_CYAN_LIGHT, status::buffer).percent());
        return node;
    }

    private static Component rate(long perSecond) {
        String text = FormattingUtil.formatNumberReadable(perSecond, false, FormattingUtil.DECIMAL_FORMAT_1F, "eV/s");
        return Component.literal(perSecond > 0 ? "+" + text : text);
    }

    private static Component energy(long ev, String unit) {
        return Component.literal(FormattingUtil.formatNumberReadable(ev, false, FormattingUtil.DECIMAL_FORMAT_1F, unit));
    }

    private static Component percent(double ratio) {
        return Component.literal(FormattingUtil.formatNumbers(ratio * 100) + "%");
    }

    private static Component gray(Component component) {
        return component.copy().withStyle(ChatFormatting.GRAY);
    }

    private static boolean voidsAllOutputs(IVoidable machine, @Nullable GTRecipe recipe) {
        boolean items = recipe == null || !recipe.itemOutputs.isEmpty();
        boolean fluids = recipe == null || !recipe.fluidOutputs.isEmpty();
        return (!items || machine.canVoidRecipeOutputs(ItemRecipeInfo.INSTANCE)) && (!fluids || machine.canVoidRecipeOutputs(FluidRecipeInfo.INSTANCE));
    }

    private static Component sentence(IssueView view, @Nullable String description) {
        var key = description != null ? description : view.issue().descriptionKey();
        return Component.translatable(key).withStyle(RecipeDiagnoser.style(view.issue()));
    }

    private static final class Status {

        private static final Component NONE = Component.literal("—");

        private final NeutronActivatorMachine machine;
        @Nullable
        private final NeutronVortexMachine vortex;

        private boolean refreshed;
        private int refreshedAt;
        private boolean progressRefreshed;
        private int progressAt;
        private boolean sampled;
        private int sampleEv;
        private int sampleAt;
        private long netRate;
        private boolean hasRate;

        private boolean hasTarget;
        private boolean targetRunning;
        private int targetMin = -1, targetMax = -1, targetEvt;
        private ProgressBar.Range window = ProgressBar.Range.NONE;

        private ProgressBar.Progress kinetic = ProgressBar.Progress.EMPTY;
        private ProgressBar.Callout explosionCallout = ProgressBar.Callout.HIDDEN;
        private ProgressBar.Progress progress = ProgressBar.Progress.EMPTY;
        private ProgressBar.Progress buffer = ProgressBar.Progress.EMPTY;
        private int shownEv = -1, shownProgress = -1, shownMaxProgress = -1;
        private long shownStored = -1, shownCapacity = -1;

        private IssueView acceleratorView = RecipeIssue.OFFLINE.view();
        private FlowState acceleratorState = FlowState.IDLE;
        private List<Component> acceleratorDetail = Collections.emptyList();
        private Component installedText = NONE, poweredText = NONE, gainText = NONE;

        private IssueView kineticView = RecipeIssue.OFFLINE.view();
        private FlowState kineticState = FlowState.IDLE;
        private List<Component> kineticDetail = Collections.emptyList();
        private Component targetText = NONE, netText = NONE;
        private Level netLevel = Level.NORMAL;

        private IssueView lossView = RecipeIssue.OFFLINE.view();
        private FlowState lossState = FlowState.IDLE;
        private List<Component> lossDetail = Collections.emptyList();
        private Component decayText = NONE, moderatorText = NONE, absorbText = NONE;
        private Level decayLevel = Level.NORMAL;

        private IssueView inputView = RecipeIssue.OFFLINE.view();
        private List<Component> inputDetail = Collections.emptyList();
        private Component inputUnitsText = NONE;
        private IssueView outputView = RecipeIssue.OFFLINE.view();
        private List<Component> outputDetail = Collections.emptyList();
        private Component outputUnitsText = NONE;

        private IssueView recipeView = RecipeIssue.OFFLINE.view();
        private FlowState recipeState = FlowState.IDLE;
        private List<Component> recipeDetail = Collections.emptyList();
        private Component drainText = NONE, durationText = NONE;

        private IssueView energyView = RecipeIssue.OFFLINE.view();
        private List<Component> energyDetail = Collections.emptyList();
        private Component usageText = NONE, voltageText = NONE;

        private Status(NeutronActivatorMachine machine) {
            this.machine = machine;
            this.vortex = machine instanceof NeutronVortexMachine v ? v : null;
        }

        IssueView acceleratorView() {
            refresh();
            return acceleratorView;
        }

        FlowState acceleratorState() {
            refresh();
            return acceleratorState;
        }

        List<Component> acceleratorDetail() {
            refresh();
            return acceleratorDetail;
        }

        Component installedText() {
            refresh();
            return installedText;
        }

        Component poweredText() {
            refresh();
            return poweredText;
        }

        Component gainText() {
            refresh();
            return gainText;
        }

        IssueView kineticView() {
            refresh();
            return kineticView;
        }

        FlowState kineticState() {
            refresh();
            return kineticState;
        }

        List<Component> kineticDetail() {
            refresh();
            return kineticDetail;
        }

        ProgressBar.Progress kinetic() {
            refresh();
            return kinetic;
        }

        ProgressBar.Callout explosionCallout() {
            refresh();
            return explosionCallout;
        }

        ProgressBar.Range window() {
            refresh();
            return window;
        }

        Component targetText() {
            refresh();
            return targetText;
        }

        Component netText() {
            refresh();
            return netText;
        }

        Level netLevel() {
            refresh();
            return netLevel;
        }

        IssueView lossView() {
            refresh();
            return lossView;
        }

        FlowState lossState() {
            refresh();
            return lossState;
        }

        List<Component> lossDetail() {
            refresh();
            return lossDetail;
        }

        Component decayText() {
            refresh();
            return decayText;
        }

        Level decayLevel() {
            refresh();
            return decayLevel;
        }

        Component moderatorText() {
            refresh();
            return moderatorText;
        }

        Component absorbText() {
            refresh();
            return absorbText;
        }

        IssueView inputView() {
            refresh();
            return inputView;
        }

        FlowState inputState() {
            refresh();
            return inputView.state();
        }

        List<Component> inputDetail() {
            refresh();
            return inputDetail;
        }

        Component inputUnitsText() {
            refresh();
            return inputUnitsText;
        }

        IssueView outputView() {
            refresh();
            return outputView;
        }

        FlowState outputState() {
            refresh();
            return outputView.state();
        }

        List<Component> outputDetail() {
            refresh();
            return outputDetail;
        }

        Component outputUnitsText() {
            refresh();
            return outputUnitsText;
        }

        IssueView recipeView() {
            refresh();
            return recipeView;
        }

        FlowState recipeState() {
            refresh();
            return recipeState;
        }

        List<Component> recipeDetail() {
            refresh();
            return recipeDetail;
        }

        ProgressBar.Progress progress() {
            refresh();
            return progress;
        }

        Component drainText() {
            refresh();
            return drainText;
        }

        Component durationText() {
            refresh();
            return durationText;
        }

        IssueView energyView() {
            refresh();
            return energyView;
        }

        FlowState energyState() {
            refresh();
            return energyView.state();
        }

        List<Component> energyDetail() {
            refresh();
            return energyDetail;
        }

        Component usageText() {
            refresh();
            return usageText;
        }

        Component voltageText() {
            refresh();
            return voltageText;
        }

        ProgressBar.Progress buffer() {
            refresh();
            return buffer;
        }

        private void refresh() {
            int now = machine.getOffsetTimer();
            if (!progressRefreshed || now < progressAt || now - progressAt >= PROGRESS_TICKS) {
                progressRefreshed = true;
                progressAt = now;
                refreshProgress();
            }
            if (refreshed && now >= refreshedAt && now - refreshedAt < REFRESH_TICKS) return;
            refreshed = true;
            refreshedAt = now;
            refreshStates(now);
        }

        private void refreshProgress() {
            int ev = machine.getEV();
            if (ev != shownEv) {
                shownEv = ev;
                kinetic = new ProgressBar.Progress(Math.max(0, ev), NeutronActivatorMachine.DISPLAY_MAX_EV, 0);
            }
            var logic = machine.getRecipeLogic();
            int value = 0, max = 0;
            if (logic.isActive() && logic.getMaxProgress() > 0) {
                value = logic.getProgress();
                max = logic.getMaxProgress();
            }
            if (value != shownProgress || max != shownMaxProgress) {
                shownProgress = value;
                shownMaxProgress = max;
                progress = max > 0 ? new ProgressBar.Progress(value, max, 0) : new ProgressBar.Progress(0, 1, 0);
            }
            if (vortex != null) {
                var container = vortex.getEnergyContainer();
                long stored = container.getEnergyStored(), capacity = container.getEnergyCapacity();
                if (stored != shownStored || capacity != shownCapacity) {
                    shownStored = stored;
                    shownCapacity = capacity;
                    buffer = capacity > 0 ? new ProgressBar.Progress(stored, capacity, 0) : new ProgressBar.Progress(0, 1, 0);
                }
            }
        }

        private void refreshStates(int now) {
            boolean formed = machine.isFormed();
            var logic = machine.getRecipeLogic();
            boolean working = logic.isWorking();
            boolean waiting = logic.isWaiting();
            Component reason = working ? null : logic.getIdleReason();
            boolean neutronReason = reason == IdleReason.NEUTRON_KINETIC_ENERGY_NOT_SATISFIES.reason();
            boolean auto = vortex != null && vortex.isEnergyMode();
            int ev = machine.getEV();
            refreshRate(ev, now);
            refreshTarget(logic.isActive() ? logic.getLastRecipe() : null, !working && !waiting && neutronReason);
            double efficiency = machine.getEfficiencyFactor();
            boolean accelerating = refreshAccelerators(formed, auto, efficiency);
            refreshKinetic(formed, auto, working, waiting && neutronReason, ev);
            refreshLoss(formed, auto, accelerating, ev);
            refreshInput(formed, working || waiting, neutronReason);
            refreshOutput(formed, working, reason);
            refreshRecipe(formed, auto, working, waiting, neutronReason, reason, efficiency);
            if (vortex != null) refreshEnergy(formed, auto, working);
        }

        private ProgressBar.Callout explosionCallout(int ev) {
            long remaining = Math.max(0, (long) NeutronActivatorMachine.EXPLOSION_EV - ev);
            boolean rising = hasRate && netRate > 0;
            var detail = new ArrayList<Component>(4);
            detail.add(Component.translatable(LANG_CALLOUT_TITLE).withStyle(ChatFormatting.RED));
            detail.add(Component.translatable(LANG_CALLOUT_NOW, energy(ev, "eV"), energy(remaining, "eV")));
            if (rising) detail.add(Component.translatable(LANG_CALLOUT_ETA, rate(netRate), Math.max(1, (remaining + netRate - 1) / netRate)));
            detail.add(gray(Component.translatable(LANG_CALLOUT_HINT)));
            return ProgressBar.Callout.of(Level.ERROR, Component.translatable(LANG_CALLOUT_TITLE), detail);
        }

        private void refreshRate(int ev, int now) {
            if (sampled && now > sampleAt) {
                netRate = (long) (ev - sampleEv) * 20 / (now - sampleAt);
                hasRate = true;
            }
            sampled = true;
            sampleEv = ev;
            sampleAt = now;
        }

        private void refreshTarget(@Nullable GTRecipe running, boolean waitingForRange) {
            int min, max, evt;
            boolean fromRunning = false;
            if (running != null) {
                min = running.data.getInt(GTORecipeDataKeys.EV_MIN);
                max = running.data.getInt(GTORecipeDataKeys.EV_MAX);
                evt = running.data.getInt(GTORecipeDataKeys.EVT);
                fromRunning = true;
            } else if (waitingForRange) {
                min = machine.targetMin;
                max = machine.targetMax;
                evt = machine.targetEvt;
            } else {
                min = max = -1;
                evt = 0;
            }
            hasTarget = max > 0;
            targetRunning = fromRunning;
            targetEvt = evt;
            if (min != targetMin || max != targetMax) {
                targetMin = min;
                targetMax = max;
                window = hasTarget ? new ProgressBar.Range((long) min * MEV, (long) max * MEV) : ProgressBar.Range.NONE;
                targetText = hasTarget ? Component.translatable(LANG_RANGE, FormattingUtil.formatNumbers(min), FormattingUtil.formatNumbers(max)) : NONE;
            }
        }

        private boolean refreshAccelerators(boolean formed, boolean auto, double efficiency) {
            var accelerators = machine.getAccelerators();
            int installed = formed ? accelerators.size() : 0, enabled = 0, powered = 0;
            long drain = 0;
            var lines = new ArrayList<Component>(installed + 4);
            lines.add(Component.translatable(LANG_ACCELERATOR));
            for (int i = 0; i < installed; i++) {
                var accelerator = accelerators.get(i);
                var container = accelerator.energyContainer;
                boolean on = accelerator.isWorkingEnabled();
                long stored = container.getEnergyStored();
                if (on) enabled++;
                if (on && stored > 0) {
                    powered++;
                    drain += accelerator.getMaxEUConsume();
                }
                lines.add(gray(Component.translatable(LANG_ACC_LINE, accelerator.getBlockState().getBlock().getName(), FormattingUtil.formatNumbers(stored),
                        FormattingUtil.formatNumbers(container.getEnergyCapacity()), Component.translatable(on ? LANG_ENABLED : LANG_DISABLED))));
            }
            long gain = auto ? 0 : Math.round(drain * 20D * AVERAGE_EV_PER_EU * efficiency);
            String description;
            if (!formed) {
                acceleratorView = RecipeIssue.OFFLINE.view();
                description = null;
            } else if (auto) {
                acceleratorView = IssueView.of(RecipeIssue.DISABLED, Component.translatable(LANG_ACC_BYPASS));
                description = LANG_ACC_BYPASS_DESC;
            } else if (installed == 0) {
                acceleratorView = IssueView.of(RecipeIssue.NO_ENERGY_HATCH, Component.translatable(LANG_ACC_NONE));
                description = LANG_ACC_NONE_DESC;
            } else if (enabled == 0) {
                acceleratorView = IssueView.of(RecipeIssue.DISABLED, Component.translatable(LANG_ACC_OFF));
                description = LANG_ACC_OFF_DESC;
            } else if (powered == 0) {
                acceleratorView = IssueView.of(RecipeIssue.LOW_POWER, Component.translatable(LANG_ACC_EMPTY));
                description = LANG_ACC_EMPTY_DESC;
            } else {
                acceleratorView = IssueView.of(RecipeIssue.RUNNING, Component.translatable(LANG_ACC_ON));
                description = LANG_ACC_ON_DESC;
            }
            acceleratorState = acceleratorView.state();
            installedText = formed ? Component.translatable(LANG_RATIO, installed, machine.getMaxAccelerators()) : NONE;
            poweredText = formed ? Component.literal(Integer.toString(powered)) : NONE;
            gainText = formed && !auto ? Component.translatable(LANG_ABOUT, rate(gain)) : NONE;
            lines.add(gray(Component.translatable(LANG_ACC_RULE, percent(efficiency))));
            lines.add(gray(Component.translatable(LANG_ACC_ESTIMATE)));
            lines.add(sentence(acceleratorView, description));
            acceleratorDetail = lines;
            return formed && !auto && powered > 0;
        }

        private void refreshKinetic(boolean formed, boolean auto, boolean working, boolean starved, int ev) {
            IssueView view;
            String description;
            if (!formed) {
                view = RecipeIssue.OFFLINE.view();
                description = null;
            } else if (auto) {
                view = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_KE_AUTO), working ? FlowState.ACTIVE : FlowState.READY);
                description = LANG_KE_AUTO_DESC;
            } else if (ev >= OVERLOAD_EV) {
                view = IssueView.of(RecipeIssue.CONDITION, Component.translatable(LANG_KE_DANGER));
                description = LANG_KE_DANGER_DESC;
            } else if (working) {
                boolean drains = machine.consumesKineticEnergy();
                view = IssueView.of(RecipeIssue.RUNNING, Component.translatable(drains ? LANG_KE_SUPPLY : LANG_KE_HELD));
                description = drains ? LANG_KE_SUPPLY_DESC : LANG_KE_HELD_DESC;
            } else if (starved) {
                view = IssueView.of(RecipeIssue.CONDITION, Component.translatable(LANG_KE_STARVED));
                description = LANG_KE_STARVED_DESC;
            } else if (!hasTarget) {
                boolean empty = ev <= 0;
                view = IssueView.of(RecipeIssue.IDLE, Component.translatable(empty ? LANG_KE_EMPTY : LANG_KE_NO_TARGET));
                description = empty ? LANG_KE_EMPTY_DESC : LANG_KE_NO_TARGET_DESC;
            } else if ((long) ev <= (long) targetMin * MEV) {
                boolean rising = hasRate && netRate > 0;
                view = rising ? IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_KE_LOW_RISING)) : IssueView.of(RecipeIssue.CONDITION, Component.translatable(LANG_KE_LOW));
                description = LANG_KE_LOW_DESC;
            } else if ((long) ev >= (long) targetMax * MEV) {
                boolean falling = hasRate && netRate < 0;
                view = falling ? IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_KE_HIGH_FALLING)) : IssueView.of(RecipeIssue.CONDITION, Component.translatable(LANG_KE_HIGH));
                description = LANG_KE_HIGH_DESC;
            } else {
                view = IssueView.of(RecipeIssue.OK, Component.translatable(LANG_KE_IN_RANGE));
                description = LANG_KE_IN_RANGE_DESC;
            }
            kineticView = view;
            kineticState = view.state();
            explosionCallout = formed && !auto ? explosionCallout(ev) : ProgressBar.Callout.HIDDEN;
            netText = formed && hasRate ? rate(netRate) : NONE;
            netLevel = netLevel(ev);
            var lines = new ArrayList<Component>(8);
            lines.add(Component.translatable(LANG_KINETIC));
            lines.add(gray(Component.translatable(LANG_KE_CURRENT, FormattingUtil.formatNumbers(ev))));
            if (hasTarget) {
                lines.add(gray(Component.translatable(LANG_KE_TARGET_LINE, targetText)));
                lines.add(gray(Component.translatable(targetRunning ? LANG_KE_FROM_RUNNING : LANG_KE_FROM_WAITING)));
            }
            lines.add(gray(Component.translatable(LANG_KE_NET_LINE)));
            lines.add(gray(Component.translatable(LANG_KE_EXPLOSION_LINE)));
            var sensor = machine.getSensor();
            if (formed && sensor != null) lines.add(gray(Component.translatable(LANG_KE_SENSOR_LINE, sensor.getRedstoneSignalOutput())));
            lines.add(sentence(view, description));
            kineticDetail = lines;
        }

        private Level netLevel(int ev) {
            if (!hasRate || !hasTarget || netRate == 0) return Level.NORMAL;
            if ((long) ev <= (long) targetMin * MEV) return netRate > 0 ? Level.GOOD : Level.WARNING;
            if ((long) ev >= (long) targetMax * MEV) return netRate < 0 ? Level.GOOD : Level.WARNING;
            return Level.NORMAL;
        }

        private void refreshLoss(boolean formed, boolean auto, boolean accelerating, int ev) {
            int moderators = 0;
            if (formed) {
                for (var bus : machine.getModeratorBuses()) {
                    var inventory = bus.getInventory();
                    for (int i = 0; i < inventory.getSlots(); i++) {
                        var stack = inventory.getStackInSlot(i);
                        if (NeutronActivatorMachine.isModerator(stack)) moderators += stack.getCount();
                    }
                }
            }
            boolean decaying = formed && !auto && !accelerating && ev > 0;
            String description;
            if (!formed) {
                lossView = RecipeIssue.OFFLINE.view();
                description = null;
            } else if (moderators > 0) {
                lossView = IssueView.of(RecipeIssue.RUNNING, Component.translatable(LANG_LOSS_ABSORB));
                description = LANG_LOSS_ABSORB_DESC;
            } else if (decaying) {
                lossView = IssueView.of(RecipeIssue.WAITING, Component.translatable(LANG_LOSS_DECAY));
                description = LANG_LOSS_DECAY_DESC;
            } else {
                lossView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_LOSS_NONE));
                description = LANG_LOSS_NONE_DESC;
            }
            lossState = lossView.state();
            decayText = !formed ? NONE : decaying ? rate(-NeutronActivatorMachine.DECAY_EV_PER_SECOND) : Component.translatable(LANG_PAUSED);
            decayLevel = decaying ? Level.WARNING : Level.NORMAL;
            moderatorText = formed ? Component.translatable(LANG_COUNT, moderators) : NONE;
            absorbText = formed ? energy((long) moderators * NeutronActivatorMachine.MODERATOR_EV, "eV") : NONE;
            var lines = new ArrayList<Component>(5);
            lines.add(Component.translatable(LANG_LOSS));
            lines.add(gray(Component.translatable(auto ? LANG_LOSS_RULE_AUTO : LANG_LOSS_RULE_DECAY)));
            lines.add(gray(Component.translatable(LANG_LOSS_RULE_ABSORB)));
            lines.add(sentence(lossView, description));
            lossDetail = lines;
        }

        private void refreshInput(boolean formed, boolean active, boolean neutronReason) {
            int units = formed ? machine.getInputUnits().size() : 0;
            String description;
            if (!formed) {
                inputView = RecipeIssue.OFFLINE.view();
                description = null;
            } else if (units == 0) {
                inputView = RecipeIssue.NO_INPUT_HATCH.view();
                description = null;
            } else if (active) {
                inputView = IssueView.of(RecipeIssue.RUNNING, Component.translatable(LANG_INPUT_LOADED));
                description = null;
            } else if (neutronReason) {
                inputView = IssueView.of(RecipeIssue.INPUT_STOCKED, Component.translatable(LANG_INPUT_MATCHED));
                description = LANG_INPUT_MATCHED_DESC;
            } else {
                inputView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_INPUT_WAITING));
                description = null;
            }
            inputUnitsText = formed ? Component.literal(Integer.toString(units)) : NONE;
            inputDetail = List.of(Component.translatable(LANG_INPUT), sentence(inputView, description));
        }

        private void refreshOutput(boolean formed, boolean working, @Nullable Component reason) {
            int units = formed ? machine.getOutputUnits().size() : 0;
            boolean full = reason == IdleReason.OUTPUT_FULL.reason() || reason == IdleReason.INSUFFICIENT_OUT.reason();
            boolean voiding = voidsAllOutputs(machine, machine.getRecipeLogic().getLastRecipe());
            String description = null;
            if (!formed) outputView = RecipeIssue.OFFLINE.view();
            else if (voiding && units == 0) {
                outputView = RecipeIssue.OUTPUT_VOIDED.view();
                description = LANG_OUTPUT_VOID_ALL_DESC;
            } else if (units == 0) outputView = RecipeIssue.NO_OUTPUT_HATCH.view();
            else if (voiding) outputView = working ? RecipeIssue.OUTPUT_VOID_OVERFLOW_ACTIVE.view() : RecipeIssue.OUTPUT_VOID_OVERFLOW.view();
            else if (working) outputView = RecipeIssue.OUTPUT_ACTIVE.view();
            else if (full) outputView = RecipeIssue.OUTPUT_FULL.view();
            else outputView = IssueView.of(RecipeIssue.IDLE, Component.translatable(LANG_OUTPUT_IDLE));
            outputUnitsText = formed ? Component.literal(Integer.toString(units)) : NONE;
            outputDetail = List.of(Component.translatable(LANG_OUTPUT), sentence(outputView, description));
        }

        private void refreshRecipe(boolean formed, boolean auto, boolean working, boolean waiting, boolean neutronReason, @Nullable Component reason, double efficiency) {
            var logic = machine.getRecipeLogic();
            String description = null;
            if (!formed) {
                recipeView = RecipeIssue.UNFORMED.view();
            } else if (!logic.isWorkingEnabled()) {
                recipeView = RecipeIssue.DISABLED.view();
            } else if (working) {
                recipeView = RecipeIssue.RUNNING.view();
            } else if (neutronReason) {
                recipeView = IssueView.of(RecipeIssue.CONDITION, Component.translatable(waiting ? LANG_KE_STARVED : LANG_RECIPE_RANGE));
                description = waiting ? LANG_KE_STARVED_DESC : hasTarget && targetMin >= 0 && (long) machine.getEV() >= (long) targetMax * MEV ? LANG_KE_HIGH_DESC : LANG_KE_LOW_DESC;
            } else if (waiting) {
                recipeView = IssueView.of(RecipeIssue.WAITING, reason);
            } else {
                recipeView = IssueView.of(RecipeIssue.IDLE, reason);
            }
            recipeState = working ? FlowState.ACTIVE : recipeView.state();
            boolean drains = machine.consumesKineticEnergy();
            double multiplier = machine.getEVtMultiplier();
            long parallel = MachineUtils.getHatchParallel(machine);
            if (!formed || !hasTarget) drainText = NONE;
            else if (!drains || auto) drainText = Component.translatable(LANG_NO_DRAIN);
            else drainText = energy(Math.round(targetEvt * (double) KEV * multiplier), "eV/t");
            durationText = formed ? percent(auto ? 0.2 : efficiency) : NONE;
            var lines = new ArrayList<Component>(6);
            lines.add(Component.translatable(LANG_RECIPE));
            lines.add(gray(Component.translatable(LANG_RECIPE_HEIGHT, machine.height, percent(efficiency))));
            if (auto) lines.add(gray(Component.translatable(LANG_RECIPE_AUTO_RULE)));
            if (drains && !auto) {
                lines.add(gray(Component.translatable(LANG_RECIPE_DRAIN_RULE, FormattingUtil.formatNumbers(targetEvt), FormattingUtil.formatNumbers(multiplier), parallel)));
            } else {
                lines.add(gray(Component.translatable(LANG_RECIPE_NO_DRAIN_RULE)));
            }
            lines.add(description != null ? Component.translatable(description).withStyle(RecipeDiagnoser.style(recipeView.issue())) : recipeView.text().copy().withStyle(RecipeDiagnoser.style(recipeView.issue())));
            recipeDetail = lines;
        }

        private void refreshEnergy(boolean formed, boolean auto, boolean working) {
            var container = vortex.getEnergyContainer();
            long capacity = container.getEnergyCapacity(), stored = container.getEnergyStored();
            long usage = hasTarget ? (long) (targetMin + targetMax) * 5 : 0;
            long voltage = container.getInputVoltage();
            String description = null;
            if (!formed) energyView = RecipeIssue.OFFLINE.view();
            else if (!vortex.hasEnergyReceiver()) {
                energyView = IssueView.of(RecipeIssue.DISABLED, Component.translatable(LANG_NO_RECEIVER));
                description = LANG_NO_RECEIVER_DESC;
            } else if (!auto) {
                energyView = IssueView.of(RecipeIssue.DISABLED, Component.translatable(LANG_ENERGY_OFF));
                description = LANG_ENERGY_OFF_DESC;
            } else if (capacity <= 0) energyView = RecipeIssue.NO_ENERGY_HATCH.view();
            else if (usage > 0 && voltage < usage) energyView = RecipeIssue.LOW_POWER.view();
            else if (usage > 0 && stored < usage) energyView = RecipeIssue.LOW_BUFFER.view();
            else energyView = working ? RecipeIssue.ENERGY_ACTIVE.view() : RecipeIssue.ENERGY_READY.view();
            usageText = formed && auto && usage > 0 ? Component.literal(FormattingUtil.formatNumbers(usage) + " EU/t") : NONE;
            voltageText = formed && capacity > 0 ? Component.literal(GTValues.VN[GTUtil.getTierByVoltage(voltage)]) : NONE;
            energyDetail = List.of(Component.translatable(LANG_ENERGY), gray(Component.translatable(LANG_ENERGY_RULE)),
                    gray(Component.translatable(LANG_RECIPE_AUTO_RULE)), sentence(energyView, description));
        }
    }
}
