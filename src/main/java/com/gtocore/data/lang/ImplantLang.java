package com.gtocore.data.lang;

import static com.gtocore.data.lang.LangHandler.addCNEN;

final class ImplantLang {

    private ImplantLang() {}

    static void init() {
        addCNEN("key.gtocore.implant_action", "GTO 义体动作模式", "GTO Implant Action Mode");
        addCNEN("key.gtocore.implant_wheel", "GTO 义体轮盘", "GTO Implant Wheel");

        addCNEN("gtocore.implant.action.wrench", "扳手", "Wrench");
        addCNEN("gtocore.implant.action.screwdriver", "螺丝刀", "Screwdriver");
        addCNEN("gtocore.implant.action.crowbar", "撬棍", "Crowbar");
        addCNEN("gtocore.implant.action.soft_mallet", "软锤", "Soft Mallet");
        addCNEN("gtocore.implant.action.hard_hammer", "锤子", "Hammer");
        addCNEN("gtocore.implant.action.wire_cutter", "剪线钳", "Wire Cutter");
        addCNEN("gtocore.implant.action.mark", "队伍标记", "Team Marker");
        addCNEN("gtocore.implant.action.beam", "光束", "Beam");
        addCNEN("gtocore.implant.action.cannon", "蓄能炮", "Charge Cannon");
        addCNEN("gtocore.implant.action.tether", "束缚", "Tether");
        addCNEN("gtocore.implant.hud.no_target", "%s：无目标", "%s: no target");
        addCNEN("gtocore.implant.hud.marker.distance", "%s 格", "%s blocks");
        addCNEN("gtocore.implant.wheel.gained", "你获得了%s动作，可按住 %s 键在轮盘中选择", "You gained the %s action; hold %s to pick it on the wheel");
        addCNEN("gtocore.implant.wheel.lost", "你失去了%s动作", "You lost the %s action");
        addCNEN("gtocore.implant.wheel.title", "义体动作", "Implant Actions");

        addCNEN("gtocore.implant.value.no_power", "义体电量耗尽", "Implant out of charge");
        addCNEN("gtocore.implant.value.no_fault", "附近没有停机的机器", "No stopped machines nearby");
        addCNEN("gtocore.implant.value.no_aim", "未指向物品", "No item targeted");
        addCNEN("gtocore.implant.value.no_grid", "未连接 AE 网络", "Not connected to an AE network");
        addCNEN("gtocore.implant.value.no_target", "未指向机器", "No machine targeted");
        addCNEN("gtocore.implant.value.stock_craftable", "%s（可合成）", "%s (craftable)");

        addCNEN("gtocore.monitor.source.implant", "义体感知", "Implant Senses");
        addCNEN("gtocore.monitor.source.implant_remote", "远程触控", "Remote Touch");
        addCNEN("gtocore.monitor.channel.implant.faults", "故障清单", "Fault List");
        addCNEN("gtocore.monitor.channel.implant.faults.desc", "附近停止工作的机器与停机原因", "Nearby stopped machines and why they stopped");
        addCNEN("gtocore.monitor.channel.implant.stock", "库存直觉", "Stock Sense");
        addCNEN("gtocore.monitor.channel.implant.stock.desc", "准星所指物品在 AE 网络中的存量", "AE network stock of the item under the crosshair");
        addCNEN("gtocore.monitor.value.source_lost.no_implant", "对应义体未装备", "the implant is not equipped");

        addCNEN("gtocore.implant.stat.idle", "待机耗电", "Standby Draw");
        addCNEN("gtocore.implant.stat.active", "工作耗电", "Working Draw");
        addCNEN("gtocore.implant.stat.use", "单次耗电", "Energy per Use");
        addCNEN("gtocore.implant.power.supply", "自动从队伍无线电网补电，也可放入充电设备充电；电量耗尽后停止工作",
                "Recharges from the team wireless grid automatically and can be charged in charging devices; stops working when empty");
        addCNEN("gtocore.implant.requires_mind_hud", "HUD 组件须同时装备心智义体", "HUD components require a Mind Implant as well");
        addCNEN("gtocore.implant.requires_terminal", "须随身携带已连接的无线终端，并处于无线接入点范围内",
                "Requires a linked wireless terminal in the inventory, within range of a wireless access point");
        addCNEN("gtocore.implant.control.wheel", "按住打开义体轮盘，鼠标指向动作后松开选定", "Hold to open the implant wheel; point at an action and release to select it");

        addCNEN("gtocore.implant.fault_vision.desc", "标出附近停止工作的机器", "Highlights nearby machines that have stopped working");
        addCNEN("gtocore.implant.fault_vision.outline", "%s 格内未成型、缺少输入或输出受阻的机器以红框标出，隔墙可见",
                "Machines within %s blocks that are unformed, short of inputs or blocked on output are outlined in red, visible through walls");
        addCNEN("gtocore.implant.fault_vision.hud", "HUD 组件「故障清单」列出各机器的停机原因", "The HUD component \"Fault List\" shows why each machine stopped");

        addCNEN("gtocore.implant.stock_sense.desc", "显示准星所指物品在 AE 网络中的存量", "Shows the AE network stock of the item under the crosshair");
        addCNEN("gtocore.implant.stock_sense.hud", "指向 %s 格内的方块或掉落物时，HUD 组件「库存直觉」显示存量与能否合成",
                "When pointing at a block or dropped item within %s blocks, the HUD component \"Stock Sense\" shows the stock and whether it can be crafted");

        addCNEN("gtocore.implant.current_vision.desc", "显示附近导线中的电流", "Shows the current in nearby cables");
        addCNEN("gtocore.implant.current_vision.label", "%s 格内的导线上方显示近一秒的峰值电流", "Cables within %s blocks show their peak current over the last second");
        addCNEN("gtocore.implant.current_vision.color", "颜色表示负载：绿色低于一半，黄色低于九成，红色接近上限", "Color shows the load: green below half, yellow below nine tenths, red near the limit");

        addCNEN("gtocore.implant.remote_touch.desc", "远距离查看并操作机器", "Views and operates machines from a distance");
        addCNEN("gtocore.implant.remote_touch.lock", "准星指向的 %s 格内的机器即为目标，视线移开立即失效",
                "The machine under the crosshair within %s blocks is the target; it is dropped as soon as the crosshair leaves it");
        addCNEN("gtocore.implant.remote_touch.hud", "HUD 组件「远程触控」显示所指机器的状态与开关，内容与监控器的机器组件相同",
                "The HUD component \"Remote Touch\" shows the status and switch of the targeted machine, same as the monitor's machine components");
        addCNEN("gtocore.implant.remote_touch.interact", "保持准星指向机器，按住后点击开关组件启停", "Keep the crosshair on the machine, then hold and click the switch component to start or stop it");

        addCNEN("gtocore.implant.team_marker.desc", "在准星位置放置队伍标记", "Places a team marker at the crosshair");
        addCNEN("gtocore.implant.team_marker.rule", "最远 %s 格，持续 %s 秒，每人最多 %s 个", "Up to %s blocks away, lasts %s seconds, at most %s per player");
        addCNEN("gtocore.implant.team_marker.team", "同队玩家无需装备义体即可看到标记，隔墙可见", "Teammates see the markers without an implant, through walls");
        addCNEN("gtocore.implant.team_marker.act", "按住并点击右键，放置标记", "Hold and right-click to place a marker");

        addCNEN("gtocore.implant.restock.desc", "从 AE 网络补满背包中未满一组的物品", "Tops up partial stacks in the inventory from the AE network");
        addCNEN("gtocore.implant.restock.rule", "每 %s 秒检查背包与快捷栏，网络中有同种物品时补至一组",
                "Checks the inventory and hotbar every %s seconds and fills the stacks the network can supply");
        addCNEN("gtocore.implant.restock.cost", "单次耗电按补入的物品数量计", "Energy per use is charged for each item supplied");

        addCNEN("gtocore.implant.tool.desc", "空手或手持任意物品时使用 GT 工具", "Uses GT tools with an empty hand or while holding anything");
        addCNEN("gtocore.implant.tool.kinds", "提供扳手、螺丝刀、撬棍、软锤、锤子、剪线钳六种动作，对 %s 格内的机器与管道生效",
                "Provides wrench, screwdriver, crowbar, soft mallet, hammer and wire cutter actions for machines and pipes within %s blocks");
        addCNEN("gtocore.implant.tool.act", "按住并右键点击机器或管道，执行所选工具动作", "Hold and right-click a machine or pipe to use the selected tool");

        addCNEN("gtocore.implant.beam.desc", "发射持续造成伤害的光束", "Fires a beam that deals continuous damage");
        addCNEN("gtocore.implant.beam.rule", "射程 %s 格，每秒命中 %s 次，每次造成 %s 点伤害", "Range %s blocks, %s hits per second, %s damage per hit");
        addCNEN("gtocore.implant.beam.act", "按住，再按住右键持续发射", "Hold, then hold right-click to keep firing");

        addCNEN("gtocore.implant.cannon.desc", "蓄能后发射一发爆炸弹", "Charges up and fires an explosive shot");
        addCNEN("gtocore.implant.cannon.rule", "射程 %s 格，蓄能 %s 秒至满；不破坏方块", "Range %s blocks, fully charged in %s seconds; does not break blocks");
        addCNEN("gtocore.implant.cannon.damage", "伤害随蓄能从 %s 升至 %s，范围从 %s 格升至 %s 格", "Damage grows with charge from %s to %s, radius from %s to %s blocks");
        addCNEN("gtocore.implant.cannon.cost", "耗电随蓄能从单次耗电的 1/%s 升至全额", "Energy grows with charge from 1/%s of the energy per use to the full amount");
        addCNEN("gtocore.implant.cannon.act", "按住，再按住右键蓄能，松开右键发射", "Hold, then hold right-click to charge and release it to fire");

        addCNEN("gtocore.implant.tether.desc", "束缚并牵引生物或玩家", "Binds and drags a mob or player");
        addCNEN("gtocore.implant.tether.rule", "对 %s 格内的目标生效，最长持续 %s 秒，目标随准星移动",
                "Works on targets within %s blocks for up to %s seconds; the target follows the crosshair");
        addCNEN("gtocore.implant.tether.limit", "对首领生物无效", "Has no effect on bosses");
        addCNEN("gtocore.implant.tether.act", "按住，再对目标按住右键束缚", "Hold, then hold right-click on a target to bind it");
        addCNEN("gtocore.implant.tether.fling", "束缚中点击，将目标沿视线甩出", "While binding, click to fling the target along the line of sight");
    }
}
