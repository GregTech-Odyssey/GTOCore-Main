package com.gtocore.common.item;

import com.gtocore.api.research.scanning.editor.DataScanningEditor;
import com.gtocore.api.research.techtree.editor.TechNodeEditor;
import com.gtocore.api.research.techtree.ui.TechTreePage;
import com.gtocore.config.GTOConfig;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

/**
 * 科技树调试器（物品）与 {@code /gtocore techtree ui} 的界面：研究窗口，每个研究类别一个标签，节点详情里有"强制解锁"；
 * 开发环境（或开启自定义配方）时，标签栏末尾再加节点编辑器、数据扫描编辑器。
 */
@DataGeneratorScanned
public class TechTreeViewer implements IItemUIFactory {

    @RegisterLanguage(cn = "科技树查看器", en = "Tech Tree Viewer")
    public static final String NAME = "gtocore.tech_tree_viewer";

    private final boolean editorTabsEnabled;

    public TechTreeViewer() {
        this(true);
    }

    public TechTreeViewer(boolean editorTabsEnabled) {
        this.editorTabsEnabled = editorTabsEnabled;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder heldItemHolder, Player player) {
        return new ModularUI(176, 166, heldItemHolder, player).widget(createWindow());
    }

    /** 研究窗口（两端都会调用）。 */
    public MachineWindow createWindow() {
        var options = new TechTreePage.Options().force();
        boolean editors = editorTabsEnabled && (GTCEu.isDev() || GTOConfig.INSTANCE.devMode.enableCustomRecipes);
        var extraTabs = editors ? new IFancyUIProvider[] { TechNodeEditor.INSTANCE, DataScanningEditor.INSTANCE } : new IFancyUIProvider[0];
        return TechTreePage.window(options, extraTabs);
    }
}
