package com.gtocore.common.item;

import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.scanning.editor.DataScanningEditor;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.editor.TechNodeEditor;
import com.gtocore.api.research.techtree.ui.TechTreeWidget;
import com.gtocore.config.GTOConfig;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;

@DataGeneratorScanned
public class TechTreeViewer implements IItemUIFactory, IFancyUIProvider {

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder heldItemHolder, Player player) {
        return new ModularUI(176, 166, heldItemHolder, player)
                .widget(new FancyMachineUIWidget(this, 176, 166));
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return new WidgetGroup();
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(Blocks.DIRT.asItem());
    }

    @Override
    public Component getTitle() {
        return Component.translatable(NAME);
    }

    @RegisterLanguage(cn = "科技树查看器", en = "Tech Tree Viewer")
    public static final String NAME = "gtocore.tech_tree_viewer";

    @Override
    public void attachSideTabs(TabsWidget tabs) {
        IFancyUIProvider page;
        for (var manager : TechTreeManager.getManagers()) {
            page = new IFancyUIProvider() {

                @Override
                public Widget createMainPage(FancyMachineUIWidget widget) {
                    return new TechTreeWidget(0, 0, 176, 166, manager, TeamResearchSavedDtat::getOrCreateContext);
                }

                @Override
                public IGuiTexture getTabIcon() {
                    return manager.getIcon();
                }

                @Override
                public List<Component> getTabTooltips() {
                    return List.of(TechTreeManager.getTreeName(manager));
                }

                @Override
                public Component getTitle() {
                    return TechTreeManager.getTreeName(manager);
                }
            };
            if (tabs.getMainTab() == null) {
                tabs.setMainTab(page);
            } else {
                tabs.attachSubTab(page);
            }
        }
        if (GTCEu.isDev() || GTOConfig.INSTANCE.devMode.enableCustomRecipes) {
            if (tabs.getMainTab() == null) {
                tabs.setMainTab(TechNodeEditor.INSTANCE);
            } else {
                tabs.attachSubTab(TechNodeEditor.INSTANCE);
            }
            tabs.attachSubTab(DataScanningEditor.INSTANCE);
        }
    }
}
