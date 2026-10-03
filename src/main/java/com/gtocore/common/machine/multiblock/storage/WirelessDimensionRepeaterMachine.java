package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.api.wireless.energy.GridBody;
import com.gtocore.api.wireless.energy.IWirelessGridProvider;
import com.gtocore.api.wireless.energy.Provider;
import com.gtocore.api.wireless.energy.ProviderRegistry;
import com.gtocore.api.wireless.energy.WirelessText;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.wireless.energy.GridMapEntry;
import com.gtocore.common.wireless.energy.RelayTargets;
import com.gtocore.common.wireless.energy.map.GridMapMode;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.multiblock.NoRecipeLogicMultiblockMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@DataGeneratorScanned
public final class WirelessDimensionRepeaterMachine extends NoRecipeLogicMultiblockMachine implements IWirelessGridProvider, IBindable, ITierCasingMachine, IMachineLife, IMachineSubWindows {

    @RegisterLanguage(cn = "目标维度", en = "Target Dimension")
    private static final String TARGET = "gtocore.machine.wireless_dimension_repeater.target";
    @RegisterLanguage(cn = "线路连接到哪个维度；连接后两个维度可以互相送电", en = "The dimension this line connects to; both dimensions can then send energy to each other")
    private static final String TARGET_TIP = "gtocore.machine.wireless_dimension_repeater.target.tooltip";
    @RegisterLanguage(cn = "线路：%s ↔ %s", en = "Line: %s ↔ %s")
    private static final String LINE = "gtocore.machine.wireless_dimension_repeater.line";
    @RegisterLanguage(cn = "每个方向：%s · %sA", en = "Each way: %s · %sA")
    private static final String CAPACITY = "gtocore.machine.wireless_dimension_repeater.capacity";
    @RegisterLanguage(cn = "在星图中选择", en = "Pick on Grid Map")
    private static final String PICK = "gtocore.machine.wireless_dimension_repeater.pick";
    @RegisterLanguage(cn = "在星图中点击星球或异界维度，将其设为线路目标", en = "Click a planet or realm on the Grid Map to set it as the line target")
    private static final String PICK_TIP = "gtocore.machine.wireless_dimension_repeater.pick.tooltip";

    private final TierCasingTrait tierCasingTrait;
    @SaveToDisk
    private String target = "";
    @Nullable
    private ObjectList<ResourceKey<Level>> targetOptions;

    public WirelessDimensionRepeaterMachine(MetaMachineBlockEntity holder) {
        super(holder);
        tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
    }

    @Nullable
    private ResourceKey<Level> targetKey() {
        return RelayTargets.parse(target);
    }

    private ObjectList<ResourceKey<Level>> targetOptions() {
        var options = targetOptions;
        if (options == null) {
            var level = getLevel();
            options = level == null ? ObjectLists.singleton(null) : RelayTargets.options(level.dimension());
            if (level != null) targetOptions = options;
        }
        return options;
    }

    @Nullable
    private ResourceKey<Level> targetOption(int index) {
        var options = targetOptions();
        return index <= 0 || index >= options.size() ? null : options.get(index);
    }

    private int targetIndex() {
        var key = targetKey();
        return key == null ? 0 : Math.max(0, targetOptions().indexOf(key));
    }

    private void setTargetIndex(int index) {
        var key = targetOption(index);
        target = key == null ? "" : key.location().toString();
        register();
    }

    public boolean pickTarget(Player player, ResourceKey<Level> key) {
        if (key == null || isRemote() || !canEditTarget(player)) return false;
        var body = GridBody.of(key);
        if (!targetOptions().contains(body)) return false;
        target = body.location().toString();
        register();
        onChanged();
        return true;
    }

    private boolean canEditTarget(Player player) {
        var owner = getOwnerUUID();
        return owner != null && TeamUtil.getTeamUUID(owner).equals(TeamUtil.getTeamUUID(player.getUUID()));
    }

    private void register() {
        if (!isRemote() && isFormed()) ProviderRegistry.registerRelay(this, getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER), targetKey());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        register();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        ProviderRegistry.unregisterLater(this, 20);
    }

    @Override
    public void onMachineRemoved() {
        ProviderRegistry.unregister(this);
    }

    @Override
    public boolean isProvidingWirelessGrid() {
        return isFormed();
    }

    @Override
    public UIElement createUIWidget() {
        return MachineDisplay.page(this, this::addDisplayText, controls -> {
            controls.addCycle(TARGET, targetOptions().size(), index -> WirelessText.dimension(targetOption(index)), this::targetIndex, this::setTargetIndex, TARGET_TIP);
            controls.add(GridMapEntry.openButton(this, PICK, PICK_TIP));
        });
    }

    @Override
    public @Nullable ModularUI createSubWindow(String key, Player player) {
        return GridMapEntry.window(this, key, player, GridMapMode.PICK);
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        var level = getLevel();
        if (level == null) return;
        int tier = getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
        textList.add(Component.translatable(LINE, WirelessText.dimension(level.dimension()), WirelessText.dimension(targetKey())).withStyle(ChatFormatting.GRAY));
        if (tier >= 0 && tier <= GTValues.MAX) {
            textList.add(Component.translatable(CAPACITY, GTValues.VNF[tier], FormattingUtil.formatNumbers(Provider.Relay.AMPERAGE)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public boolean preferTeamName() {
        return true;
    }
}
