package com.gtocore.common.wireless.energy.map;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.network.chat.Component;

@DataGeneratorScanned
public final class GridMapLang {

    @RegisterLanguage(cn = "电网星图", en = "Grid Map")
    public static final Component TITLE = Component.translatable("gtocore.wireless_grid_map.title");

    @RegisterLanguage(cn = "信息卡", en = "Labels")
    public static final Component TOGGLE_TAGS = Component.translatable("gtocore.wireless_grid_map.toggle.tags");
    @RegisterLanguage(cn = "显示星球旁的储能信息卡", en = "Show storage labels next to bodies")
    public static final Component TOGGLE_TAGS_TIP = Component.translatable("gtocore.wireless_grid_map.toggle.tags.tooltip");
    @RegisterLanguage(cn = "空闲线路", en = "Idle Lines")
    public static final Component TOGGLE_IDLE = Component.translatable("gtocore.wireless_grid_map.toggle.idle");
    @RegisterLanguage(cn = "显示当前没有流量的线路", en = "Show lines without current flow")
    public static final Component TOGGLE_IDLE_TIP = Component.translatable("gtocore.wireless_grid_map.toggle.idle.tooltip");
    @RegisterLanguage(cn = "动效", en = "Motion")
    public static final Component TOGGLE_MOTION = Component.translatable("gtocore.wireless_grid_map.toggle.motion");
    @RegisterLanguage(cn = "显示沿线路移动的能量粒子", en = "Show energy particles moving along lines")
    public static final Component TOGGLE_MOTION_TIP = Component.translatable("gtocore.wireless_grid_map.toggle.motion.tooltip");
    @RegisterLanguage(cn = "图例", en = "Legend")
    public static final Component TOGGLE_LEGEND = Component.translatable("gtocore.wireless_grid_map.toggle.legend");
    @RegisterLanguage(cn = "显示线路与标记的含义", en = "Show what lines and marks mean")
    public static final Component TOGGLE_LEGEND_TIP = Component.translatable("gtocore.wireless_grid_map.toggle.legend.tooltip");
    @RegisterLanguage(cn = "定位到当前星球", en = "Locate the current body")
    public static final Component LOCATE = Component.translatable("gtocore.wireless_grid_map.locate");
    @RegisterLanguage(cn = "返回机器", en = "Back")
    public static final Component BACK = Component.translatable("gtocore.wireless_grid_map.back");
    @RegisterLanguage(cn = "返回打开星图的机器界面", en = "Return to the machine that opened this map")
    public static final Component BACK_TIP = Component.translatable("gtocore.wireless_grid_map.back.tooltip");
    @RegisterLanguage(cn = "从终端打开", en = "Opened from the terminal")
    public static final Component FROM_TERMINAL = Component.translatable("gtocore.wireless_grid_map.from_terminal");
    @RegisterLanguage(cn = "查看星系", en = "View this star system")
    public static final Component GALAXY_TIP = Component.translatable("gtocore.wireless_grid_map.galaxy.tooltip");
    @RegisterLanguage(cn = "电网星图操作", en = "Grid map controls")
    public static final Component HELP = Component.translatable("gtocore.wireless_grid_map.help");
    @RegisterLanguage(cn = "单击星球、轨道或异界维度：打开设备供电地图", en = "Click a body, orbit or realm: open its equipment power map")
    public static final Component HELP_SELECT = Component.translatable("gtocore.wireless_grid_map.help.select");
    @RegisterLanguage(cn = "右键星球：查看节点详情；单击空白处或按 Esc：取消选中", en = "Right-click a body: node details; click empty space or press Esc: deselect")
    public static final Component HELP_DESELECT = Component.translatable("gtocore.wireless_grid_map.help.deselect");
    @RegisterLanguage(cn = "选中后悬停另一颗星球：两地之间的最大输送", en = "Hover another body while selected: maximum transfer between them")
    public static final Component HELP_COMPARE = Component.translatable("gtocore.wireless_grid_map.help.compare");
    @RegisterLanguage(cn = "低缩放下卫星并入母星，线路两端画在母星附近", en = "At low zoom, moons merge into their planet and line ends sit near it")
    public static final Component HELP_MOONS = Component.translatable("gtocore.wireless_grid_map.help.moons");

    @RegisterLanguage(cn = "选择线路目标：点击一颗星球或异界维度", en = "Choose the line target: click a planet or realm")
    public static final Component PICK_HINT = Component.translatable("gtocore.wireless_grid_map.pick.hint");
    @RegisterLanguage(cn = "取消", en = "Cancel")
    public static final Component PICK_CANCEL = Component.translatable("gtocore.wireless_grid_map.pick.cancel");
    @RegisterLanguage(cn = "无法将该维度设为线路目标", en = "This dimension cannot be set as the line target")
    public static final Component PICK_REJECTED = Component.translatable("gtocore.wireless_grid_map.pick.rejected");

    @RegisterLanguage(cn = "储能", en = "Storage")
    public static final Component STORED = Component.translatable("gtocore.wireless_grid_map.stored");
    @RegisterLanguage(cn = "%s", en = "%s")
    public static final String STORED_DETAIL = "gtocore.wireless_grid_map.stored.detail";
    @RegisterLanguage(cn = "折叠或展开", en = "Collapse or expand")
    public static final Component COLLAPSE = Component.translatable("gtocore.wireless_grid_map.collapse");
    @RegisterLanguage(cn = "现在", en = "Now")
    public static final Component WINDOW_NOW = Component.translatable("gtocore.wireless_grid_map.window.now");
    @RegisterLanguage(cn = "分钟", en = "Min")
    public static final Component WINDOW_MINUTE = Component.translatable("gtocore.wireless_grid_map.window.minute");
    @RegisterLanguage(cn = "小时", en = "Hour")
    public static final Component WINDOW_HOUR = Component.translatable("gtocore.wireless_grid_map.window.hour");
    @RegisterLanguage(cn = "天", en = "Day")
    public static final Component WINDOW_DAY = Component.translatable("gtocore.wireless_grid_map.window.day");
    @RegisterLanguage(cn = "当前速率", en = "Current rate")
    public static final Component WINDOW_NOW_TIP = Component.translatable("gtocore.wireless_grid_map.window.now.tooltip");
    @RegisterLanguage(cn = "最近 1 分钟平均", en = "Average over the last minute")
    public static final Component WINDOW_MINUTE_TIP = Component.translatable("gtocore.wireless_grid_map.window.minute.tooltip");
    @RegisterLanguage(cn = "最近 1 小时平均", en = "Average over the last hour")
    public static final Component WINDOW_HOUR_TIP = Component.translatable("gtocore.wireless_grid_map.window.hour.tooltip");
    @RegisterLanguage(cn = "最近 1 天平均", en = "Average over the last day")
    public static final Component WINDOW_DAY_TIP = Component.translatable("gtocore.wireless_grid_map.window.day.tooltip");
    @RegisterLanguage(cn = "净流量", en = "Net")
    public static final Component NET = Component.translatable("gtocore.wireless_grid_map.net");
    @RegisterLanguage(cn = "损耗", en = "Loss")
    public static final Component LOSS = Component.translatable("gtocore.wireless_grid_map.loss");
    @RegisterLanguage(cn = "充满/耗尽", en = "Fill / Drain")
    public static final Component ETA = Component.translatable("gtocore.wireless_grid_map.eta");
    @RegisterLanguage(cn = "约 %s后充满", en = "Full in about %s")
    public static final String ETA_FILL = "gtocore.wireless_grid_map.eta.fill";
    @RegisterLanguage(cn = "约 %s后耗尽", en = "Empty in about %s")
    public static final String ETA_DRAIN = "gtocore.wireless_grid_map.eta.drain";
    @RegisterLanguage(cn = "持平", en = "Steady")
    public static final Component ETA_STEADY = Component.translatable("gtocore.wireless_grid_map.eta.steady");
    @RegisterLanguage(cn = "超过 999 年后充满", en = "Full in over 999 years")
    public static final Component ETA_FILL_BEYOND = Component.translatable("gtocore.wireless_grid_map.eta.fill_beyond");
    @RegisterLanguage(cn = "超过 999 年后耗尽", en = "Empty in over 999 years")
    public static final Component ETA_DRAIN_BEYOND = Component.translatable("gtocore.wireless_grid_map.eta.drain_beyond");
    @RegisterLanguage(cn = "已充满", en = "Full")
    public static final Component ETA_FULL = Component.translatable("gtocore.wireless_grid_map.eta.full");
    @RegisterLanguage(cn = "已耗尽", en = "Empty")
    public static final Component ETA_EMPTY = Component.translatable("gtocore.wireless_grid_map.eta.empty");
    @RegisterLanguage(cn = "%s 秒", en = "%s s")
    public static final String TIME_SECONDS = "gtocore.wireless_grid_map.time.seconds";
    @RegisterLanguage(cn = "%s 分钟", en = "%s min")
    public static final String TIME_MINUTES = "gtocore.wireless_grid_map.time.minutes";
    @RegisterLanguage(cn = "%s 小时", en = "%s h")
    public static final String TIME_HOURS = "gtocore.wireless_grid_map.time.hours";
    @RegisterLanguage(cn = "%s 天", en = "%s days")
    public static final String TIME_DAYS = "gtocore.wireless_grid_map.time.days";
    @RegisterLanguage(cn = "%s 年", en = "%s years")
    public static final String TIME_YEARS = "gtocore.wireless_grid_map.time.years";
    @RegisterLanguage(cn = "规模", en = "Scale")
    public static final Component SCALE = Component.translatable("gtocore.wireless_grid_map.scale");
    @RegisterLanguage(cn = "%s 个节点 · %s 条线路（%s 条在用）", en = "%s nodes · %s lines (%s active)")
    public static final String SCALE_VALUE = "gtocore.wireless_grid_map.scale.value";
    @RegisterLanguage(cn = "显示范围", en = "Coverage")
    public static final Component COVERAGE = Component.translatable("gtocore.wireless_grid_map.coverage");
    @RegisterLanguage(cn = "仅显示前 %s 个节点、%s 条线路", en = "First %s nodes and %s lines only")
    public static final String TRUNCATED = "gtocore.wireless_grid_map.truncated";
    @RegisterLanguage(cn = "尚未建立无线电网", en = "No wireless grid yet")
    public static final Component EMPTY_TITLE = Component.translatable("gtocore.wireless_grid_map.empty.title");
    @RegisterLanguage(cn = "建造无线能源塔。", en = "Build a wireless energy tower.")
    public static final Component EMPTY_STEP_1 = Component.translatable("gtocore.wireless_grid_map.empty.step_1");
    @RegisterLanguage(cn = "在能源塔内放入存储单元与传输单元，所在星球即成为电网节点。", en = "Place storage and transfer units inside the tower; its body becomes a grid node.")
    public static final Component EMPTY_STEP_2 = Component.translatable("gtocore.wireless_grid_map.empty.step_2");
    @RegisterLanguage(cn = "建造维度中继器并选择目标，将电网延伸到其他星球或异界维度。", en = "Build a dimension repeater and choose a target to extend the grid to other bodies or realms.")
    public static final Component EMPTY_STEP_3 = Component.translatable("gtocore.wireless_grid_map.empty.step_3");
    @RegisterLanguage(cn = "储能最低", en = "Lowest storage")
    public static final Component RANK_LOW = Component.translatable("gtocore.wireless_grid_map.rank.low");
    @RegisterLanguage(cn = "最忙线路", en = "Busiest lines")
    public static final Component RANK_BUSY = Component.translatable("gtocore.wireless_grid_map.rank.busy");
    @RegisterLanguage(cn = "储能最多", en = "Most storage")
    public static final Component RANK_MOST = Component.translatable("gtocore.wireless_grid_map.rank.most");
    @RegisterLanguage(cn = "%s · %s%%", en = "%s · %s%%")
    public static final String RANK_STORED_VALUE = "gtocore.wireless_grid_map.rank.stored_value";
    @RegisterLanguage(cn = "最多", en = "Most")
    public static final Component RANK_TAB_MOST = Component.translatable("gtocore.wireless_grid_map.rank.tab.most");
    @RegisterLanguage(cn = "最低", en = "Lowest")
    public static final Component RANK_TAB_LOW = Component.translatable("gtocore.wireless_grid_map.rank.tab.low");
    @RegisterLanguage(cn = "最忙", en = "Busiest")
    public static final Component RANK_TAB_BUSY = Component.translatable("gtocore.wireless_grid_map.rank.tab.busy");
    @RegisterLanguage(cn = "满载 · %s", en = "Full · %s")
    public static final String RANK_SATURATED = "gtocore.wireless_grid_map.rank.saturated";
    @RegisterLanguage(cn = "点击：定位到该天体或线路并查看详情", en = "Click: locate this body or line and show details")
    public static final Component RANK_CLICK = Component.translatable("gtocore.wireless_grid_map.rank.click");

    @RegisterLanguage(cn = "电网节点 · %s", en = "Grid node · %s")
    public static final String STATE_NODE = "gtocore.wireless_grid_map.state.node";
    @RegisterLanguage(cn = "无储能节点 · 线路最高 %s", en = "No-storage node · lines up to %s")
    public static final String STATE_LINE_ONLY = "gtocore.wireless_grid_map.state.line_only";
    @RegisterLanguage(cn = "未接入电网", en = "Not connected")
    public static final Component STATE_OUTSIDE = Component.translatable("gtocore.wireless_grid_map.state.outside");
    @RegisterLanguage(cn = "位置", en = "Location")
    public static final Component LOCATION = Component.translatable("gtocore.wireless_grid_map.location");
    @RegisterLanguage(cn = "异界维度", en = "Realm")
    public static final Component REALM = Component.translatable("gtocore.wireless_grid_map.realm");
    @RegisterLanguage(cn = "%s · %s", en = "%s · %s")
    public static final String LOCATION_VALUE = "gtocore.wireless_grid_map.location.value";
    @RegisterLanguage(cn = "无储能节点：本地无能源塔，设备经线路取用", en = "No-storage node: no local towers; devices draw through lines")
    public static final Component RELAY_ONLY = Component.translatable("gtocore.wireless_grid_map.relay_only");
    @RegisterLanguage(cn = "未接入电网，不储能", en = "Not connected, no storage")
    public static final Component NO_STORAGE = Component.translatable("gtocore.wireless_grid_map.no_storage");
    @RegisterLanguage(cn = "电压等级", en = "Voltage")
    public static final Component TIER = Component.translatable("gtocore.wireless_grid_map.tier");
    @RegisterLanguage(cn = "能源塔", en = "Towers")
    public static final Component TOWERS = Component.translatable("gtocore.wireless_grid_map.towers");
    @RegisterLanguage(cn = "%s 座 · 最高 %s", en = "%s · up to %s")
    public static final String TOWERS_VALUE = "gtocore.wireless_grid_map.towers.value";
    @RegisterLanguage(cn = "无", en = "None")
    public static final Component NONE = Component.translatable("gtocore.wireless_grid_map.none");
    @RegisterLanguage(cn = "入网损耗", en = "Input loss")
    public static final Component NODE_LOSS = Component.translatable("gtocore.wireless_grid_map.node_loss");
    @RegisterLanguage(cn = "%s%%", en = "%s%%")
    public static final String PERCENT = "gtocore.wireless_grid_map.percent";
    @RegisterLanguage(cn = "设备", en = "Devices")
    public static final Component PORTS = Component.translatable("gtocore.wireless_grid_map.ports");
    @RegisterLanguage(cn = "%s 台", en = "%s")
    public static final String PORTS_VALUE = "gtocore.wireless_grid_map.ports.value";
    @RegisterLanguage(cn = "净流量", en = "Net")
    public static final Component DELTA = Component.translatable("gtocore.wireless_grid_map.delta");
    @RegisterLanguage(cn = "流量", en = "Flow")
    public static final Component FLOW = Component.translatable("gtocore.wireless_grid_map.flow");
    @RegisterLanguage(cn = "存入：存进本节点的能量，含经线路送来的部分；取用：本节点设备与整笔结算取走的能量，含经线路从其他节点调来的部分；中继流入、流出：经本节点线路转发的能量。三者口径不同，相加不等于净流量。无储能节点的取用全部来自线路，中继流入减流出的差额即本地取用与损耗", en = "Stored in: energy stored at this node, including what arrives by line; Drawn: energy taken by devices and settlements here, including what lines bring from other nodes; Relay in/out: energy passing through this node's lines. These are measured differently and do not add up to the net flow. At a no-storage node everything drawn comes through lines, so relay in minus relay out equals local drawing plus loss")
    public static final Component FLOW_INFO = Component.translatable("gtocore.wireless_grid_map.flow.info");
    @RegisterLanguage(cn = "存入", en = "Stored in")
    public static final Component NODE_INPUT = Component.translatable("gtocore.wireless_grid_map.node_input");
    @RegisterLanguage(cn = "取用", en = "Drawn")
    public static final Component NODE_OUTPUT = Component.translatable("gtocore.wireless_grid_map.node_output");
    @RegisterLanguage(cn = "中继流入", en = "Relay in")
    public static final Component RELAY_IN = Component.translatable("gtocore.wireless_grid_map.relay_in");
    @RegisterLanguage(cn = "中继流出", en = "Relay out")
    public static final Component RELAY_OUT = Component.translatable("gtocore.wireless_grid_map.relay_out");
    @RegisterLanguage(cn = "线路 %s 条", en = "Lines: %s")
    public static final String LINES = "gtocore.wireless_grid_map.lines";
    @RegisterLanguage(cn = "没有线路", en = "No lines")
    public static final Component LINES_EMPTY = Component.translatable("gtocore.wireless_grid_map.lines.empty");
    @RegisterLanguage(cn = "%s", en = "%s")
    public static final String LINE_TIER = "gtocore.wireless_grid_map.line.tier";
    @RegisterLanguage(cn = "送出 %s · 接收 %s", en = "Out %s · In %s")
    public static final String LINE_FLOW = "gtocore.wireless_grid_map.line.flow";
    @RegisterLanguage(cn = "利用率", en = "Usage")
    public static final Component LINE_USAGE = Component.translatable("gtocore.wireless_grid_map.line.usage");
    @RegisterLanguage(cn = "点击：将视图移到该线路", en = "Click: move the view to this line")
    public static final Component LINE_CLICK = Component.translatable("gtocore.wireless_grid_map.line.click");
    @RegisterLanguage(cn = "设备供电地图", en = "Equipment power map")
    public static final Component DEVICE_MAP = Component.translatable("gtocore.wireless_grid_map.device_map");
    @RegisterLanguage(cn = "在 Xaero 地图上查看各设备的供电情况，可切换维度", en = "View equipment power on the Xaero map; dimensions can be switched")
    public static final Component DEVICE_MAP_HINT = Component.translatable("gtocore.wireless_grid_map.device_map.hint");
    @RegisterLanguage(cn = "Xaero 世界地图尚未就绪", en = "Xaero's World Map is not ready")
    public static final Component DEVICE_MAP_UNAVAILABLE = Component.translatable("gtocore.wireless_grid_map.device_map.unavailable");
    @RegisterLanguage(cn = "发电", en = "Generation")
    public static final Component GENERATION = Component.translatable("gtocore.wireless_grid_map.device.generation");
    @RegisterLanguage(cn = "耗电", en = "Consumption")
    public static final Component CONSUMPTION = Component.translatable("gtocore.wireless_grid_map.device.consumption");
    @RegisterLanguage(cn = "绿色：发电 · 橙色：耗电 · 电量越大颜色越深", en = "Green: generation · Orange: consumption · Darker: more power")
    public static final Component DEVICE_LEGEND = Component.translatable("gtocore.wireless_grid_map.device.legend");
    @RegisterLanguage(cn = "坐标：%s, %s, %s", en = "Position: %s, %s, %s")
    public static final String DEVICE_POSITION = "gtocore.wireless_grid_map.device.position";
    @RegisterLanguage(cn = "高亮", en = "Highlight")
    public static final Component HIGHLIGHT = Component.translatable("gtocore.wireless_grid_map.device.highlight");
    @RegisterLanguage(cn = "传送", en = "Teleport")
    public static final Component TELEPORT = Component.translatable("gtocore.wireless_grid_map.device.teleport");
    @RegisterLanguage(cn = "需要 OP 权限", en = "Requires OP permission")
    public static final Component OP_REQUIRED = Component.translatable("gtocore.wireless_grid_map.device.op_required");
    @RegisterLanguage(cn = "返回电网星图", en = "Back to Grid Map")
    public static final Component BACK_TO_GRID = Component.translatable("gtocore.wireless_grid_map.device.back");
    @RegisterLanguage(cn = "显示全部设备", en = "Fit all equipment")
    public static final Component FIT_DEVICES = Component.translatable("gtocore.wireless_grid_map.device.fit");
    @RegisterLanguage(cn = "无设备", en = "No devices")
    public static final Component NO_PORTS = Component.translatable("gtocore.wireless_grid_map.no_ports");
    @RegisterLanguage(cn = "关闭地图并在世界中高亮 10 秒", en = "Close the map and highlight it in the world for 10 s")
    public static final Component PORT_HIGHLIGHT = Component.translatable("gtocore.wireless_grid_map.port.highlight");
    @RegisterLanguage(cn = "不在当前维度", en = "Not in the current dimension")
    public static final Component NOT_HERE = Component.translatable("gtocore.wireless_grid_map.not_here");

    @RegisterLanguage(cn = "有线路、无流量", en = "Linked, idle")
    public static final Component LEGEND_IDLE = Component.translatable("gtocore.wireless_grid_map.legend.idle");
    @RegisterLanguage(cn = "低流量或低负载", en = "Low flow or load")
    public static final Component LEGEND_LOW = Component.translatable("gtocore.wireless_grid_map.legend.low");
    @RegisterLanguage(cn = "中等流量或负载", en = "Medium flow or load")
    public static final Component LEGEND_MID = Component.translatable("gtocore.wireless_grid_map.legend.mid");
    @RegisterLanguage(cn = "大流量或接近满载（带动效）", en = "Heavy flow or near capacity (animated)")
    public static final Component LEGEND_HIGH = Component.translatable("gtocore.wireless_grid_map.legend.high");
    @RegisterLanguage(cn = "选中时与其他天体的引导线", en = "Guide lines to other bodies while selected")
    public static final Component LEGEND_GUIDE = Component.translatable("gtocore.wireless_grid_map.legend.guide");
    @RegisterLanguage(cn = "最大输送路径", en = "Maximum transfer path")
    public static final Component LEGEND_PATH = Component.translatable("gtocore.wireless_grid_map.legend.path");
    @RegisterLanguage(cn = "净流量为正 / 为负", en = "Net flow positive / negative")
    public static final Component LEGEND_TREND = Component.translatable("gtocore.wireless_grid_map.legend.trend");
    @RegisterLanguage(cn = "图例", en = "Legend")
    public static final Component LEGEND_TITLE = Component.translatable("gtocore.wireless_grid_map.legend.title");
    @RegisterLanguage(cn = "线路", en = "Lines")
    public static final Component LEGEND_LINES = Component.translatable("gtocore.wireless_grid_map.legend.lines");
    @RegisterLanguage(cn = "标记", en = "Marks")
    public static final Component LEGEND_MARKS = Component.translatable("gtocore.wireless_grid_map.legend.marks");
    @RegisterLanguage(cn = "线路端点", en = "Line end")
    public static final Component LEGEND_END = Component.translatable("gtocore.wireless_grid_map.legend.end");
    @RegisterLanguage(cn = "折叠卫星的线路端点", en = "Line end of a folded moon")
    public static final Component LEGEND_FOLDED = Component.translatable("gtocore.wireless_grid_map.legend.folded");
    @RegisterLanguage(cn = "未接入电网的天体", en = "Body outside the grid")
    public static final Component LEGEND_OUTSIDE = Component.translatable("gtocore.wireless_grid_map.legend.outside");
    @RegisterLanguage(cn = "当前所在位置", en = "Your current location")
    public static final Component LEGEND_CURRENT = Component.translatable("gtocore.wireless_grid_map.legend.current");
    @RegisterLanguage(cn = "名称 +N：折叠的卫星数", en = "Name +N: folded moons")
    public static final Component LEGEND_FOLDED_COUNT = Component.translatable("gtocore.wireless_grid_map.legend.folded_count");

    @RegisterLanguage(cn = "%s", en = "%s")
    public static final String TAG_STORED = "gtocore.wireless_grid_map.tag.stored";
    @RegisterLanguage(cn = "无储能节点 · 线路最高 %s", en = "No-storage node · lines up to %s")
    public static final String TAG_RELAY = "gtocore.wireless_grid_map.tag.relay";
    @RegisterLanguage(cn = "无储能 · %s", en = "No storage · %s")
    public static final String TAG_RELAY_SHORT = "gtocore.wireless_grid_map.tag.relay_short";

    @RegisterLanguage(cn = "单击查看设备地图，右键查看节点详情", en = "Click for equipment map; right-click for node details")
    public static final Component TIP_DETAILS = Component.translatable("gtocore.wireless_grid_map.tip.details");
    @RegisterLanguage(cn = "净流量 %s", en = "Net %s")
    public static final String TIP_DELTA = "gtocore.wireless_grid_map.tip.delta";
    @RegisterLanguage(cn = "%s 颗卫星在电网中", en = "%s moons in the grid")
    public static final String TIP_ANCHOR_MOONS = "gtocore.wireless_grid_map.tip.anchor_moons";
    @RegisterLanguage(cn = "%s → %s：最大 %s", en = "%s → %s: up to %s")
    public static final String TIP_PAIR_MAX = "gtocore.wireless_grid_map.tip.pair_max";
    @RegisterLanguage(cn = "途经 %s 段线路 · 最高 %s", en = "Via %s lines · up to %s")
    public static final String TIP_PAIR_ROUTE = "gtocore.wireless_grid_map.tip.pair_route";
    @RegisterLanguage(cn = "无法经线路到达", en = "Not reachable through lines")
    public static final Component TIP_UNREACHABLE = Component.translatable("gtocore.wireless_grid_map.tip.unreachable");
    @RegisterLanguage(cn = "%s ↔ %s", en = "%s ↔ %s")
    public static final String TIP_LINE_ENDS = "gtocore.wireless_grid_map.tip.line_ends";
    @RegisterLanguage(cn = "%s（每方向）", en = "%s (each way)")
    public static final String TIP_LINE_CAPACITY = "gtocore.wireless_grid_map.tip.line_capacity";
    @RegisterLanguage(cn = "%s → %s：%s（负载 %s%%）", en = "%s → %s: %s (load %s%%)")
    public static final String TIP_LINE_DIRECTION = "gtocore.wireless_grid_map.tip.line_direction";
    @RegisterLanguage(cn = "点击设为线路目标", en = "Click to set as the line target")
    public static final Component TIP_PICK = Component.translatable("gtocore.wireless_grid_map.tip.pick");
    @RegisterLanguage(cn = "中继器所在星球，不能作为线路目标", en = "The repeater's own body cannot be the target")
    public static final Component TIP_PICK_SELF = Component.translatable("gtocore.wireless_grid_map.tip.pick_self");
    @RegisterLanguage(cn = "不在线路目标范围内", en = "Not a valid line target")
    public static final Component TIP_PICK_INVALID = Component.translatable("gtocore.wireless_grid_map.tip.pick_invalid");

    private GridMapLang() {}
}
