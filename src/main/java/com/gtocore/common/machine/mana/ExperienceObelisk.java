package com.gtocore.common.machine.mana;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.common.data.GTOTickTimeMonitors;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.IntSupplier;

import static com.gtocore.common.data.GTOFluids.XP_JUICE;
import static com.gtocore.data.tag.Tags.XP_JUICE_TAG;

@DataGeneratorScanned
public class ExperienceObelisk extends MetaMachine implements IFancyUIMachine, IDropSaveMachine {

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor manaMonitor = holder.monitorTick(GTOTickTimeMonitors.MANA, this::absorbXpOrb);

    @SaveToDisk
    private final NotifiableInventory<AEFluidKey> experienceTank;
    private final AEFluidKey xpKey;
    private final XpJuiceHandler xpHandler = new XpJuiceHandler();
    @SaveToDisk(defaultValue = "0")
    @Getter
    private int currentConfigAmount = 0;
    @SaveToDisk(defaultValue = "false")
    @Getter
    private boolean isConfiguringLevels = false;

    @SaveToDisk(defaultValue = "false")
    @Getter
    private boolean vacuumHopperMode = false;
    private final ConditionalSubscriptionHandler tickSubs;

    public ExperienceObelisk(MetaMachineBlockEntity holder) {
        super(holder);
        experienceTank = NotifiableInventory.fluids(this, 1, Integer.MAX_VALUE, IO.NONE, IO.BOTH);
        experienceTank.setFilter(k -> k instanceof AEFluidKey f && f.getFluid() == XP_JUICE.getSource());
        xpKey = AEFluidKey.of(XP_JUICE.getSource());
        tickSubs = new ConditionalSubscriptionHandler(this, manaMonitor, 20, this::isVacuumHopperMode);
    }

    private void absorbXpOrb() {
        this.tickSubs.updateSubscription();
        Level world = holder.getLevel();
        if (world == null) return;
        var aabb = new AABB(holder.getBlockPos()).inflate(14.0);
        var xpOrbs = world.getEntitiesOfClass(ExperienceOrb.class, aabb);
        for (var xpOrb : xpOrbs) {
            int juiceAmount = xpOrb.getValue();
            int xpAbsorbed = (int) experienceTank.insert(xpKey, xpToFluid(juiceAmount), false);
            if (xpAbsorbed > 0) {
                xpOrb.discard();
                int remainingXp = xpOrb.getValue() - xpAbsorbed;
                if (remainingXp > 0) {
                    ExperienceOrb orb = new ExperienceOrb(
                            world,
                            xpOrb.getX(),
                            xpOrb.getY(),
                            xpOrb.getZ(),
                            remainingXp);
                    world.addFreshEntity(orb);
                }
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubs.initialize(getLevel());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        tickSubs.unsubscribe();
    }

    @Override
    public @Nullable IKeyHandler<AEFluidKey> getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return xpHandler;
    }

    private int storedFluid() {
        return (int) experienceTank.storage.amountAt(0);
    }

    private int drainFluid(long amount) {
        var key = experienceTank.storage.keyAt(0);
        return key == null ? 0 : (int) experienceTank.storage.extract(0, key, amount, false);
    }

    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        // 该机器没有自动输出面，非潜行扳手点击不会改变任何状态，却会消耗扳手耐久并拦截 UI 打开，
        // 导致手持 GT 工具时无法进入界面使用经验修补。此处放行，仅保留潜行扳手改变朝向的行为。
        if (!playerIn.isShiftKeyDown()) return InteractionResult.PASS;
        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var player = widget.getGui().entityPlayer;
        return UIElement.column(116).layout(l -> l.gapAll(4).alignCenter())
                .addChildren(
                        TextLine.constant(116, LANG_CONFIGURE_AMOUNT).bindClientColor(UITheme::panelText),
                        createConfigAmountWidget(),
                        UIElement.centeredRow(16).layout(l -> l.gapAll(4)).addChildren(
                                createLevelsModeToggleButton(),
                                TextLine.of(96, () -> Component.translatable(LANG_STORED_EXPERIENCE, EnchantmentUtils.getLevelForExperience(fluidToXp(storedFluid())))).bindClientColor(UITheme::panelText)),
                        UIElement.centeredRow(16).layout(l -> l.gapAll(4)).addChildren(
                                createTransferConfiguredButton(player, true),
                                createTransferConfiguredButton(player, false),
                                createTransferAllButton(player, true),
                                createTransferAllButton(player, false),
                                createVacuumHopperModeButton(),
                                fixToolsWithMendingEnchantmentButton(player)));
    }

    private IntInputWidget createConfigAmountWidget() {
        var intWidget = new IntInputWidget(this::getCurrentConfigAmount, this::setCurrentConfigAmount);
        intWidget.setMax(100000000);
        intWidget.setMin(0);
        intWidget.setValue(currentConfigAmount);
        return intWidget;
    }

    private SwitchWidget createLevelsModeToggleButton() {
        SwitchWidget[] holder = new SwitchWidget[1];
        SwitchWidget toggleButton = new SwitchWidget(
                0, 0, 16, 16,
                (clickData, pressed) -> {
                    isConfiguringLevels = pressed;
                    if (holder[0] != null) {
                        updateLevelsModeTooltip(holder[0], pressed);
                    }
                });
        holder[0] = toggleButton;
        toggleButton.setSupplier(this::isConfiguringLevels);
        toggleButton.setTexture(new GuiTextureGroup(GuiTextures.BUTTON, GTOGuiTextures.SMALL_XP_ORB),
                new GuiTextureGroup(GuiTextures.BUTTON, GTOGuiTextures.LARGE_XP_ORB.scale(0.8f)));
        updateLevelsModeTooltip(toggleButton, isConfiguringLevels);
        return toggleButton;
    }

    private static void updateLevelsModeTooltip(SwitchWidget widget, boolean levels) {
        widget.setHoverTooltips(levels ? LANG_CONFIGURE_LEVELS : LANG_CONFIGURE_POINTS);
    }

    private ButtonWidget createTransferConfiguredButton(Player player, boolean toPlayer) {
        var button = createTransferButton(player, () -> calculateConfiguredAmount(player, toPlayer));
        if (toPlayer) {
            button.setButtonTexture(GuiTextures.BUTTON_RIGHT.copy().rotate(45));
            button.setHoverTooltips(LANG_TRANSFER_TO_PLAYER);
        } else {
            button.setButtonTexture(GuiTextures.BUTTON_LEFT.copy().rotate(45));
            button.setHoverTooltips(LANG_TRANSFER_TO_MACHINE);
        }
        return button;
    }

    private ButtonWidget createTransferAllButton(Player player, boolean toPlayer) {
        IntSupplier amountSupplier = toPlayer ? () -> fluidToXp(storedFluid()) : () -> -getExperiencePoints(player);
        var button = createTransferButton(player, amountSupplier);
        if (toPlayer) {
            button.setButtonTexture(GuiTextures.BUTTON_RIGHT.copy().rotate(45));
            button.setHoverTooltips(LANG_TRANSFER_ALL_TO_PLAYER);
        } else {
            button.setButtonTexture(GuiTextures.BUTTON_LEFT.copy().rotate(45));
            button.setHoverTooltips(LANG_TRANSFER_ALL_TO_MACHINE);
        }
        return button;
    }

    private SwitchWidget createVacuumHopperModeButton() {
        SwitchWidget button = new SwitchWidget(0, 0, 16, 16, (clickData, pressed) -> setVacuumHopperMode(pressed));
        button.setSupplier(this::isVacuumHopperMode);
        button.setTexture(createToggleTexture(false), createToggleTexture(true));
        button.setHoverTooltips(LANG_VACUUM_HOPPER_MODE);
        return button;
    }

    private GuiTextureGroup createToggleTexture(boolean active) {
        var baseTexture = GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy();
        if (active) {
            return new GuiTextureGroup(GuiTextures.BUTTON, baseTexture.getSubTexture(0, 0.5, 1, 0.5).scale(0.8f));
        } else {
            return new GuiTextureGroup(GuiTextures.BUTTON, baseTexture.getSubTexture(0, 0, 1, 0.5).scale(0.8f));
        }
    }

    private int calculateConfiguredAmount(Player player, boolean toPlayer) {
        if (!isConfiguringLevels) {
            return toPlayer ? currentConfigAmount : -currentConfigAmount;
        }
        int targetLevel;
        if (toPlayer) {
            targetLevel = player.experienceLevel + currentConfigAmount;
        } else {
            targetLevel = Math.max(0, player.experienceLevel - currentConfigAmount);
        }
        return EnchantmentUtils.getTotalExperienceForLevel(targetLevel) - getExperiencePoints(player);
    }

    private ButtonWidget createTransferButton(Player player, IntSupplier amountToPlayer) {
        return new ButtonWidget(0, 0, 16, 16,
                (clickData -> {
                    int amount = amountToPlayer.getAsInt();
                    if (player instanceof ServerPlayer && amount != 0) {
                        int canTransfer = amount;
                        if (amount > 0) {
                            int beforeAdd = getExperiencePoints(player);
                            EnchantmentUtils.chargeExperience(player, -amount);
                            int afterAdd = getExperiencePoints(player);
                            int drained = fluidToXp(drainFluid(xpToFluid(afterAdd - beforeAdd)));
                            if (drained < canTransfer) {
                                EnchantmentUtils.chargeExperience(player, canTransfer - drained);
                            }
                        } else {
                            canTransfer = fluidToXp((int) experienceTank.insert(xpKey, xpToFluid(-canTransfer), true));
                            if (EnchantmentUtils.chargeExperience(player, canTransfer)) {
                                experienceTank.insert(xpKey, xpToFluid(canTransfer), false);
                            }
                        }
                    }
                }));
    }

    private Widget fixToolsWithMendingEnchantmentButton(Player player) {
        return new ButtonWidget(0, 0, 16, 16,
                (clickData -> {
                    if (player instanceof ServerPlayer) {
                        int extracted = drainFluid(storedFluid());
                        int remainingExp = repairPlayerItems(player, extracted, extracted);
                        if (remainingExp > 0) {
                            experienceTank.insert(xpKey, remainingExp, false);
                        }
                    }
                }))
                .setButtonTexture(GuiTextures.BUTTON, GuiTextures.TOOL_SLOT_OVERLAY.copy().scale(0.88f))
                .setHoverTooltips(LANG_REPAIR_MENDING_TOOLS);
    }

    /// @see ExperienceOrb#repairPlayerItems(Player, int)
    private int repairPlayerItems(Player p_147093_, int remainingExp, int initialValue) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, p_147093_, ItemStack::isDamaged);
        if (entry != null) {
            ItemStack itemstack = entry.getValue();
            int i = Math.min((int) (initialValue * itemstack.getXpRepairRatio()), itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - i);
            int j = remainingExp - this.durabilityToXp(i);
            return j > 0 ? this.repairPlayerItems(p_147093_, j, initialValue) : 0;
        } else {
            return remainingExp;
        }
    }

    private int durabilityToXp(int pDurability) {
        return pDurability * 10;
    }

    public void setVacuumHopperMode(boolean vacuumHopperMode) {
        this.vacuumHopperMode = vacuumHopperMode;
        this.tickSubs.updateSubscription();
    }

    @Override
    public boolean saveBreak() {
        return storedFluid() > 0;
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        IDropSaveMachine.super.saveToItem(tag);
        tag.putInt("xp", storedFluid());
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        IDropSaveMachine.super.loadFromItem(tag);
        int xpAmount = tag.getInt("xp");
        experienceTank.storage.set(0, xpKey, xpAmount);
    }

    private final class XpJuiceHandler implements IKeyHandler<AEFluidKey> {

        private AEFluidKey normalize(AEFluidKey key) {
            return key.getFluid().is(XP_JUICE_TAG) ? xpKey : key;
        }

        @Override
        public AEKeyType keyType() {
            return AEKeyTypes.FLUIDS;
        }

        @Override
        public int size() {
            return experienceTank.size();
        }

        @Override
        public @Nullable AEFluidKey keyAt(int slot) {
            return experienceTank.keyAt(slot);
        }

        @Override
        public long amountAt(int slot) {
            return experienceTank.amountAt(slot);
        }

        @Override
        public long slotLimit(int slot) {
            return experienceTank.slotLimit(slot);
        }

        @Override
        public long insert(int slot, AEFluidKey key, long amount, boolean simulate) {
            return experienceTank.insert(slot, normalize(key), amount, simulate);
        }

        @Override
        public long extract(int slot, AEFluidKey key, long amount, boolean simulate) {
            return experienceTank.extract(slot, key, amount, simulate);
        }

        @Override
        public long insert(AEFluidKey key, long amount, boolean simulate) {
            return experienceTank.insert(normalize(key), amount, simulate);
        }

        @Override
        public long extract(AEFluidKey key, long amount, boolean simulate) {
            return experienceTank.extract(key, amount, simulate);
        }

        @Override
        public IKeyHandler<AEFluidKey> unrestricted() {
            return experienceTank.unrestricted();
        }
    }

    private void setCurrentConfigAmount(int integer) {
        this.currentConfigAmount = integer;
    }

    private static int getExperiencePoints(Player player) {
        return (int) (EnchantmentUtils.getTotalExperienceForLevel(player.experienceLevel) + (player.experienceProgress) * player.getXpNeededForNextLevel());
    }

    private static int xpToFluid(int xp) {
        return (int) Mth.clamp(xp * 20L, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static int fluidToXp(int fluid) {
        return fluid / 20;
    }

    @RegisterLanguage(cn = "单位：经验等级", en = "Unit: Experience Levels")
    private static final Component LANG_CONFIGURE_LEVELS = Component.translatable("gtocore.machine.experience_obelisk.configure.enabled");
    @RegisterLanguage(cn = "单位：经验值", en = "Unit: Experience Points")
    private static final Component LANG_CONFIGURE_POINTS = Component.translatable("gtocore.machine.experience_obelisk.configure.disabled");
    @RegisterLanguage(cn = "设定经验值转移数量", en = "Transfer the configured experience amount")
    private static final Component LANG_CONFIGURE_AMOUNT = Component.translatable("gtocore.machine.experience_obelisk.configure_amount");
    @RegisterLanguage(cn = "从玩家转移设定的经验值到机器", en = "Transfer the configured experience from player to machine")
    private static final Component LANG_TRANSFER_TO_MACHINE = Component.translatable("gtocore.machine.experience_obelisk.transfer_to_machine");
    @RegisterLanguage(cn = "从机器转移设定的经验值到玩家", en = "Transfer the configured experience from machine to player")
    private static final Component LANG_TRANSFER_TO_PLAYER = Component.translatable("gtocore.machine.experience_obelisk.transfer_to_player");
    @RegisterLanguage(cn = "转移所有的经验值到玩家", en = "Transfer all experience from machine to player")
    private static final Component LANG_TRANSFER_ALL_TO_PLAYER = Component.translatable("gtocore.machine.experience_obelisk.transfer_all_to_player");
    @RegisterLanguage(cn = "转移所有的经验值到机器", en = "Transfer all experience from machine to player")
    private static final Component LANG_TRANSFER_ALL_TO_MACHINE = Component.translatable("gtocore.machine.experience_obelisk.transfer_all_to_machine");
    @RegisterLanguage(cn = "真空箱子模式", en = "Vacuum Hopper Mode")
    private static final Component LANG_VACUUM_HOPPER_MODE = Component.translatable("gtocore.machine.experience_obelisk.vacuum_hopper_mode");
    @RegisterLanguage(cn = "已存储%s级经验", en = "Stored %s Levels of Experience")
    public static final String LANG_STORED_EXPERIENCE = "gtocore.machine.experience_obelisk.stored_experience";
    @RegisterLanguage(cn = "修复带有经验修补附魔的工具", en = "Repair tools with Mending enchantment")
    private static final Component LANG_REPAIR_MENDING_TOOLS = Component.translatable("gtocore.machine.experience_obelisk.repair_mending_tools");
}
