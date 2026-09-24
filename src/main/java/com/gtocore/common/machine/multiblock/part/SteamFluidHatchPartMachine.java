package com.gtocore.common.machine.multiblock.part;

import com.gtocore.common.data.GTOMachines;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.gregtechceu.gtceu.uipro.elements.IconToggle;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine.IS_STEEL;

public class SteamFluidHatchPartMachine extends FluidHatchPartMachine {

    public SteamFluidHatchPartMachine(MetaMachineBlockEntity holder, IO io) {
        super(holder, 1, io, 8000, 1);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        // 保留蒸汽皮肤（铜/钢底板、蒸汽槽背包）；内容与其他仓一样分两区：
        // 上面流体槽（输出仓另有锁定）和自动输入/输出开关，下面状态面板
        var auto = io == IO.IN ?
                IconToggle.of(WidgetIcons.IMPORT, this::isWorkingEnabled, this::setWorkingEnabled)
                        .tooltips("gtceu.gui.fluid_auto_input.tooltip.enabled", "gtceu.gui.fluid_auto_input.tooltip.disabled") :
                IconToggle.of(WidgetIcons.EXPORT, this::isWorkingEnabled, this::setWorkingEnabled)
                        .tooltips("gtceu.gui.fluid_auto_output.tooltip.enabled", "gtceu.gui.fluid_auto_output.tooltip.disabled");
        var content = HatchViews.fixedPage(UISizes.CONTENT_WIDTH, SteamHatchPartMachine.STEAM_CONTENT_HEIGHT,
                HatchViews.operations(HatchViews.tankOperation(tank, io), auto), HatchViews.tankStatus(tank, io),
                SteamHatchPartMachine.STEAM_DISPLAY_WIDTH);
        content.setSelfPosition(new Position(UISizes.WINDOW_PADDING_X, SteamHatchPartMachine.STEAM_CONTENT_Y));
        return new ModularUI(UISizes.WINDOW_WIDTH, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(IS_STEEL))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(content)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(IS_STEEL), UISizes.WINDOW_PADDING_X, SteamHatchPartMachine.STEAM_INVENTORY_Y, true));
    }

    @Override
    protected @NotNull NotifiableItemStackHandler createCircuitItemHandler(Object @NotNull... args) {
        return NotifiableItemStackHandler.empty(this);
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.superAttachConfigurators(configuratorPanel);
    }

    @Override
    public boolean swapIO() {
        BlockPos blockPos = getHolder().pos();
        MachineDefinition newDefinition = null;
        if (io == IO.IN) {
            newDefinition = GTOMachines.STEAM_FLUID_OUTPUT_HATCH;
        } else if (io == IO.OUT) {
            newDefinition = GTOMachines.STEAM_FLUID_INPUT_HATCH;
        }
        if (newDefinition == null) return false;
        BlockState newBlockState = newDefinition.get().defaultBlockState();
        getLevel().setBlockAndUpdate(blockPos, newBlockState);
        if (getLevel().getBlockEntity(blockPos) instanceof MetaMachineBlockEntity newHolder) {
            if (newHolder.getMetaMachine() instanceof FluidHatchPartMachine newMachine) {
                newMachine.setFrontFacing(this.getFrontFacing());
                newMachine.setUpwardsFacing(this.getUpwardsFacing());
                newMachine.setPaintingColor(this.getPaintingColor());
                for (int i = 0; i < this.tank.getTanks(); i++) {
                    newMachine.tank.setFluidInTank(i, this.tank.getFluidInTank(i));
                }
            }
        }
        return true;
    }
}
