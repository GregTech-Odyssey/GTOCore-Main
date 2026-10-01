package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.machine.trait.ProxySlotRecipeHandler;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine.readBufferTag;
import static com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine.writeBufferTag;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MEPatternBufferProxyPartMachine extends WorkableTieredIOPartMachine implements IMachineLife, IDataStickInteractable, IWailaDisplayProvider {

    private static final String JADE_BOUND = "bound";
    private static final String JADE_POS = "pos";

    private ProxySlotRecipeHandler proxySlotRecipeHandler = ProxySlotRecipeHandler.DEFAULT;
    @SaveToDisk
    @SyncToClient
    @Nullable
    private BlockPos bufferPos;
    @Nullable
    private MEPatternBufferPartMachine buffer = null;
    private boolean bufferResolved = false;

    public MEPatternBufferProxyPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.IN);
    }

    @Override
    public int tintColor(int index) {
        if (index == 9) return getRealColor();
        return -1;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel level) {
            TaskHandler.enqueueTask(level, () -> this.setBuffer(bufferPos));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        var buf = getBuffer();
        if (buf != null) {
            buf.unloadProxy(this);
            proxySlotRecipeHandler = ProxySlotRecipeHandler.DEFAULT;
            bufferResolved = false;
        }
    }

    @Override
    public List<RecipeHandlerUnit> getRecipeHandlers() {
        return proxySlotRecipeHandler.getProxySlotHandlers();
    }

    public void setBuffer(@Nullable BlockPos pos) {
        bufferResolved = true;
        bind(findBuffer(pos));
    }

    @Nullable
    private MEPatternBufferPartMachine findBuffer(@Nullable BlockPos pos) {
        var level = getLevel();
        if (level == null || pos == null) return null;
        return MetaMachine.getMachine(level, pos) instanceof MEPatternBufferPartMachine machine ? machine : null;
    }

    private void bind(@Nullable MEPatternBufferPartMachine target) {
        var previous = buffer;
        if (previous != null && previous != target) previous.removeProxy(this);
        buffer = target;
        if (target == null) {
            proxySlotRecipeHandler.updateProxy(null);
            return;
        }
        proxySlotRecipeHandler = new ProxySlotRecipeHandler(this, target);
        bufferPos = target.getPos();
        target.addProxy(this);
        if (!isRemote()) {
            proxySlotRecipeHandler.updateProxy(target);
            for (var controller : getControllers()) {
                controller.requestCheck();
            }
        }
    }

    @Nullable
    public BlockPos getBufferPos() {
        return bufferPos;
    }

    @Nullable
    public MEPatternBufferPartMachine getBuffer() {
        if (!bufferResolved) setBuffer(bufferPos);
        return buffer;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return getBuffer() != null;
    }

    @Override
    public @Nullable ModularUI createUI(Player entityPlayer) {
        assert getBuffer() != null;
        return getBuffer().createUI(entityPlayer);
    }

    @Override
    public void onMachineRemoved() {
        var buf = getBuffer();
        if (buf != null) {
            buf.removeProxy(this);
            proxySlotRecipeHandler = ProxySlotRecipeHandler.DEFAULT;
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        var tag = dataStick.getTag();
        if (tag == null || !tag.contains(MEPatternBufferPartMachine.DATA_STICK_POS, Tag.TAG_INT_ARRAY)) return InteractionResult.PASS;
        var posArray = tag.getIntArray(MEPatternBufferPartMachine.DATA_STICK_POS);
        if (posArray.length < 3) return InteractionResult.PASS;
        var target = findBuffer(new BlockPos(posArray[0], posArray[1], posArray[2]));
        if (target == null) return InteractionResult.PASS;
        bufferResolved = true;
        bind(target);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendWailaTooltip(CompoundTag data, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (!data.getBoolean(MEPatternBufferPartMachine.JADE_FORMED)) return;
        if (!data.getBoolean(JADE_BOUND)) {
            iTooltip.add(Component.translatable("gtceu.top.buffer_not_bound").withStyle(ChatFormatting.RED));
            return;
        }

        int[] pos = data.getIntArray(JADE_POS);
        iTooltip.add(Component.translatable("gtceu.top.buffer_bound_pos", pos[0], pos[1], pos[2])
                .withStyle(TooltipHelper.RAINBOW_HSL_SLOW));

        readBufferTag(iTooltip, data);
    }

    @Override
    public void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        if (!isFormed()) {
            data.putBoolean(MEPatternBufferPartMachine.JADE_FORMED, false);
            return;
        }
        data.putBoolean(MEPatternBufferPartMachine.JADE_FORMED, true);
        var buffer = getBuffer();
        if (buffer == null) {
            data.putBoolean(JADE_BOUND, false);
            return;
        }
        data.putBoolean(JADE_BOUND, true);

        var pos = buffer.getPos();
        data.putIntArray(JADE_POS, new int[] { pos.getX(), pos.getY(), pos.getZ() });
        writeBufferTag(data, buffer);
    }
}
