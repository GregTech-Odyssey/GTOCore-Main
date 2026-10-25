package com.gtocore.integration.jade.provider;

import com.gtocore.config.GTORules;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@DataGeneratorScanned
public class MaintenanceParamProvider extends CapabilityBlockProvider<IMaintenanceMachine> {

    private static final String KEY_DAMAGE_MULTIPLIER = "damageMultiplier";
    private static final String KEY_WEAR = "wear";

    @RegisterLanguage(cn = "损坏乘数: %s", en = "Damage Multiplier: %s")
    private static final String DAMAGE_MULTIPLIER = "gtocore.machine.maintenance_damage_multiplier.tooltip";
    @RegisterLanguage(cn = "损坏程度: %s", en = "Damage: %s")
    private static final String WEAR = "gtocore.machine.maintenance_wear.tooltip";
    @RegisterLanguage(cn = "维护仓损坏程度：%s", en = "Maintenance Hatch Damage: %s")
    private static final String HATCH_WEAR = "gtocore.machine.maintenance_hatch_wear.tooltip";

    public MaintenanceParamProvider() {
        super(GTOCore.id("maintenance_param_provider"));
    }

    @Override
    protected @Nullable IMaintenanceMachine getCapability(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (!(blockEntity instanceof MetaMachineBlockEntity)) {
            return null;
        }
        MetaMachine machine = MetaMachine.getMachine(blockEntity);
        if (machine instanceof IMaintenanceMachine maintenanceMachine) {
            return maintenanceMachine;
        }
        if (machine instanceof IMultiController controller) {
            for (var part : controller.getParts(IMaintenanceMachine.class)) {
                return part;
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag tag, IMaintenanceMachine machine) {
        if (machine.isFullAuto() || machine.getController() == null) {
            return;
        }
        tag.putFloat(KEY_DAMAGE_MULTIPLIER, getDamageMultiplier(machine));
        tag.putFloat(KEY_WEAR, machine.getWear());
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor accessor, BlockEntity blockEntity, IPluginConfig config) {
        if (data.isEmpty()) {
            return;
        }
        boolean isPart = blockEntity instanceof MetaMachineBlockEntity machineBlockEntity && MetaMachine.getMachine(machineBlockEntity) instanceof IMaintenanceMachine;
        float wear = data.getFloat(KEY_WEAR);
        var wearText = Component.literal((int) Math.floor(wear * 100) + "%").withStyle(wear >= 1 ? ChatFormatting.RED : ChatFormatting.GOLD);
        if (isPart) {
            tooltip.add(Component.translatable(DAMAGE_MULTIPLIER,
                    Component.literal(formatMultiplier(data.getFloat(KEY_DAMAGE_MULTIPLIER))).withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.translatable(WEAR, wearText));
        } else {
            tooltip.add(Component.translatable(HATCH_WEAR, wearText));
        }
    }

    static float getDamageMultiplier(IMaintenanceMachine machine) {
        var controller = machine.getController();
        // write() already skips when null; keep defensive for other callers.
        int pa = controller == null || controller.getParts() == null ? 1 : Math.max(1, controller.getParts().length);
        return machine.getTimeMultiplier() * GTORules.MAINTENANCE_WEAR.get() * pa;
    }

    static String formatMultiplier(float value) {
        String number = value == Math.round(value) ? FormattingUtil.formatNumbers(Math.round(value)) : FormattingUtil.formatNumber2Places(value);
        return number + "x";
    }
}
