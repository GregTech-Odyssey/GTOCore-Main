package com.gtocore.api.research.techtree;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterEnumLang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.gui.me.common.ContentToast;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TechNodeToast extends ContentToast {

    private final String text;
    private final String title;
    private final TechNode techNode;

    public TechNodeToast(TechNode techNode, TechNodeToast.Type type) {
        super(techNode.icon);
        this.techNode = techNode;
        this.text = "gtocore.research.eureka.toast.type.desc." + type.name();
        this.title = "gtocore.research.eureka.toast.type.title." + type.name();
    }

    protected void addInfoLines(List<FormattedCharSequence> lines) {
        super.addInfoLines(lines);
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        lines.addAll(font.split(Component.translatable(text, techNode.getDisplayName()), this.width() - 30 - 5));
    }

    protected Component getTitle() {
        return Component.translatable(title);
    }

    @DataGeneratorScanned
    @RegisterEnumLang(keyPrefix = "gtocore.research.eureka.toast.type")
    public enum Type {

        FINISHED_RESEARCH("已完成节点%s研究！", "Research finished for node %s!", "研究完成", "Research Finished"),
        EUREKA_GAINED("已获得节点%s尤里卡！", "Eureka gained for node %s!", "尤里卡！", "Eureka!"),;

        @RegisterEnumLang.CnValue("desc")
        private final String cnDesc;
        @RegisterEnumLang.EnValue("desc")
        private final String enDesc;

        @RegisterEnumLang.CnValue("title")
        private final String cnTitle;
        @RegisterEnumLang.EnValue("title")
        private final String enTitle;

        Type(String cnDesc, String enDesc, String cnTitle, String enTitle) {
            this.cnDesc = cnDesc;
            this.enDesc = enDesc;
            this.cnTitle = cnTitle;
            this.enTitle = enTitle;
        }
    }
}
