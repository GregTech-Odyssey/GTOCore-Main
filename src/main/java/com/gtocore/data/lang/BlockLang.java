package com.gtocore.data.lang;

import com.gtolib.api.GTOValues;
import com.gtolib.api.data.Dimension;

import com.gregtechceu.gtceu.api.GTValues;

import static com.gtocore.data.lang.LangHandler.addCNEN;

final class BlockLang {

    static void init() {
        for (int tier = GTValues.LV; tier <= GTValues.IV; tier++) {
            addCNEN("block.gtceu." + GTValues.VN[tier].toLowerCase() + "_dual_input_hatch", "%s输入总成".formatted(GTOValues.VNFR[tier]), "%s Dual Input Hatch".formatted(GTOValues.VNFR[tier]));
            addCNEN("block.gtceu." + GTValues.VN[tier].toLowerCase() + "_dual_output_hatch", "%s输出总成".formatted(GTOValues.VNFR[tier]), "%s Dual Output Hatch".formatted(GTOValues.VNFR[tier]));
        }

        for (int tier = GTValues.IV; tier <= GTValues.MAX; tier++) {
            addCNEN("block.gtceu." + GTValues.VN[tier].toLowerCase() + "_parallel_hatch", GTOValues.VNFR[tier] + "并行控制仓", GTOValues.VNFR[tier] + " Parallel Control Hatch");
        }

        addCNEN("block.gtceu." + GTValues.VN[GTValues.EV].toLowerCase() + "_parallel_hatch", GTOValues.VNFR[GTValues.EV] + "并行控制仓", GTOValues.VNFR[GTValues.EV] + " Parallel Control Hatch");

        for (Dimension dim : new Dimension[] {
                Dimension.CERES, Dimension.IO, Dimension.GANYMEDE, Dimension.BARNARDA_C, Dimension.ENCELADUS, Dimension.TITAN, Dimension.PLUTO
        }) {
            var cnName = dim.getCn();
            var cnSuffix = !cnName.isEmpty() && cnName.charAt(cnName.length() - 1) == '星' ? "仪" : "星球仪";
            addCNEN("block.ad_astra." + dim.getLocation().getPath() + "_globe", dim.getCn() + cnSuffix, dim.getEn() + " Globe");
        }
        addCNEN("block.ad_astra.saturn_globe", "土星仪", "Saturn Globe");

        // 管道工具提示，格式对齐 GT 的物品/流体管道（一条速率行）
        addCNEN("gtocore.universal_pipe.item_transfer_rate", "§b物品传输速率：§f%s 个/s", "§bItem Transfer Rate: §f%s/s");
        addCNEN("gtocore.universal_pipe.fluid_transfer_rate", "§b流体传输速率：§f%s 桶/s", "§bFluid Transfer Rate: §f%s B/s");
        addCNEN("gtocore.universal_pipe.pull_mode", "管道不存货；shift+扳手把一面设为堵住后，改从其余相邻存储主动抽取",
                "No buffer. Block a face with shift + wrench to pull from the other neighbours instead");
        addCNEN("gtocore.mana_pipe.transfer_rate", "§b传输速率：§f%s 魔力/s（约 %s 池/s）", "§bTransfer Rate: §f%s mana/s (~%s pools/s)");
    }
}
