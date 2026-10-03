package com.gtocore.mixin.jade;

import com.gtocore.common.blockentity.TesseractBlockEntity;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachine;
import com.gtocore.integration.Mods;
import com.gtocore.integration.jade.GTOJadePlugin;
import com.gtocore.integration.lang.LangAdaptor;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerList;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MufflerPartMachine;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.integration.modules.jade.JadeModule;

import com.google.common.collect.ImmutableList;
import dev.shadowsoffire.apotheosis.adventure.compat.AdventureHwylaPlugin;
import dev.shadowsoffire.apotheosis.ench.compat.EnchHwylaPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import snownee.jade.Jade;
import snownee.jade.addon.core.CorePlugin;
import snownee.jade.addon.universal.UniversalPlugin;
import snownee.jade.addon.vanilla.VanillaPlugin;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.util.CommonProxy;

import java.util.ArrayList;
import java.util.List;

@Mixin(CommonProxy.class)
public class CommonProxyMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    private void loadComplete(FMLLoadCompleteEvent event) {
        var plugins = ImmutableList.<IWailaPlugin>builder().add(new VanillaPlugin(), new UniversalPlugin(), new CorePlugin(), new JadeModule(), new GTOJadePlugin(), new AdventureHwylaPlugin(), new EnchHwylaPlugin());
        if (Mods.LANG.isLoaded()) LangAdaptor.addPlugin(plugins);
        for (IWailaPlugin plugin : plugins.build()) {
            plugin.register(WailaCommonRegistration.INSTANCE);
            if (CommonProxy.isPhysicallyClient()) {
                plugin.registerClient(WailaClientRegistration.INSTANCE);
            }
        }
        Jade.loadComplete();
    }

    @Redirect(method = "createItemCollector", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/capabilities/CapabilityProvider;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"), remap = false)
    @SuppressWarnings("unchecked")
    private static <T> LazyOptional<T> createItemCollector(CapabilityProvider<?> instance, Capability<T> capability) {
        if (instance instanceof MetaMachineBlockEntity blockEntity && !(blockEntity instanceof TesseractBlockEntity)) {
            if (blockEntity.metaMachine instanceof MEPatternPartMachine<?>) return LazyOptional.empty();
            if (blockEntity.metaMachine instanceof MufflerPartMachine mufflerPartMachine) {
                var inventory = mufflerPartMachine.getInventory();
                return LazyOptional.of(() -> new ForgeItemAdapter(inventory)).cast();
            }
            var ts = blockEntity.metaMachine.getTraits();
            List<IKeyHandler<AEItemKey>> filteredTraits = new ArrayList<>(ts.size());
            for (var t : ts) {
                if (t instanceof IKeyHandler<?> handler && handler.keyType() == AEKeyType.items()) {
                    filteredTraits.add((IKeyHandler<AEItemKey>) handler);
                }
            }
            if (!filteredTraits.isEmpty()) {
                return LazyOptional.of(() -> new ForgeItemAdapter(new KeyHandlerList<>(AEKeyType.items(), filteredTraits))).cast();
            }
        }
        return instance.getCapability(capability);
    }

    @Redirect(method = "wrapFluidStorage", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/capabilities/CapabilityProvider;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"), remap = false)
    @SuppressWarnings("unchecked")
    private static <T> LazyOptional<T> wrapFluidStorage(CapabilityProvider<?> instance, Capability<T> capability) {
        if (instance instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.metaMachine instanceof MEPatternPartMachine<?>) return LazyOptional.empty();
            var ts = blockEntity.metaMachine.getTraits();
            List<IKeyHandler<AEFluidKey>> filteredTraits = new ArrayList<>(ts.size());
            for (var t : ts) {
                if (t instanceof IKeyHandler<?> handler && handler.keyType() == AEKeyType.fluids()) {
                    filteredTraits.add((IKeyHandler<AEFluidKey>) handler);
                }
            }
            if (!filteredTraits.isEmpty()) {
                return LazyOptional.of(() -> new ForgeFluidAdapter(new KeyHandlerList<>(AEKeyType.fluids(), filteredTraits))).cast();
            }
        }
        return instance.getCapability(capability);
    }
}
