package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.techtree.TechTreeManager;
import com.gtocore.api.techtree.ui.TechTreeWidget;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.List;

public class DataCenter extends DataBankMachine {

    public DataCenter(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        var manager = AnalyzeData.INSTANCE.getTechTree();
        sideTabs.attachSubTab(new IFancyUIProvider() {

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var widget0 = new TechTreeWidget<>(0, 0, 176, 166, manager, TeamResearchSavedDtat::getForPlayer);
                widget0.setOnNodeClicked(null);
                return widget0;
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
        });
    }
}
