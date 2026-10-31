package com.gtocore.integration.ae;

import com.gtocore.mixin.mc.BlockAccessor;
import com.gtocore.mixin.mc.ItemAccessor;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.IItem;
import com.gtolib.utils.register.BlockRegisterUtils;
import com.gtolib.utils.register.ItemRegisterUtils;

import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartModels;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.upgrades.Upgrades;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.items.parts.PartItem;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.misc.InterfacePart;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;

import com.gto.registrate.util.entry.BlockEntityEntry;
import com.gto.registrate.util.entry.BlockEntry;
import com.gto.registrate.util.entry.ItemEntry;

import com.glodblock.github.extendedae.client.gui.GuiExInterface;
import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.glodblock.github.extendedae.common.blocks.BlockBaseGui;
import com.glodblock.github.extendedae.common.parts.PartExInterface;
import com.glodblock.github.extendedae.common.tileentities.TileExInterface;
import com.glodblock.github.extendedae.container.ContainerExInterface;
import com.glodblock.github.extendedae.util.Ae2Reflect;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

import static com.gtolib.api.registries.GTORegistration.GTO;

/**
 * ME接口分级：(I) 即 AE 接口，(II)~(V) 为 36 槽方块与面板，可堆叠倍率从 4096 起每级 ×64；
 * 高级接口潜行右键低级接口原地替换并保留配置与存储，ME 超大接口读档时迁为 (II)。
 */
@DataGeneratorScanned
public final class TieredInterfaces {

    public static final int TIERS = 5;
    private static final int SLOTS = 36;
    private static final long[] MULTIPLIERS = { 1, 4096, 262144, 16777216, 1073741824 };
    private static final TagKey<Item>[] CIRCUITS = new TagKey[] { CustomTags.MV_CIRCUITS, CustomTags.EV_CIRCUITS, CustomTags.LuV_CIRCUITS, CustomTags.UV_CIRCUITS, CustomTags.UEV_CIRCUITS };

    @RegisterLanguage(cn = "配置槽位与存储槽位各 %s 个", en = "%s config slots and %s storage slots")
    public static final String SLOTS_KEY = "gtocore.me_interface.slots";
    @RegisterLanguage(cn = "每槽可存放 %s 倍堆叠：物品最多 %s 个，流体最多 %s B", en = "Each slot holds %sx a stack: up to %s items or %s B of fluid")
    public static final String CAPACITY = "gtocore.me_interface.capacity";
    @RegisterLanguage(cn = "潜行右键同形态的低级 ME接口或扩展接口可直接替换，保留其配置与存储", en = "Sneak right-click a lower-tier ME Interface or Extended Interface of the same form to replace it, keeping its config and storage")
    public static final String UPGRADE = "gtocore.me_interface.upgrade";
    @RegisterLanguage(cn = "已停用，请使用 ME接口(II)~(V)", en = "Discontinued, please use ME Interface (II)-(V)")
    public static final String DISCONTINUED = "gtocore.me_interface.discontinued";
    @RegisterLanguage(cn = "ME接口(II~V)", en = "ME Interface (II-V)")
    public static final String GROUP = "gtocore.me_interface.group";
    @RegisterLanguage(cn = "ME接口(I)", en = "ME Interface (I)")
    public static final String NAME_I_BLOCK = "block.gtocore.me_interface_i";
    @RegisterLanguage(cn = "ME接口(I)", en = "ME Interface (I)")
    public static final String NAME_I_PART = "item.gtocore.cable_me_interface_i";

    private static final BlockEntry<InterfaceBlock>[] BLOCKS = new BlockEntry[TIERS];
    private static final BlockEntityEntry<InterfaceEntity>[] TYPES = new BlockEntityEntry[TIERS];
    private static final ItemEntry<InterfacePartItem>[] PARTS = new ItemEntry[TIERS];
    private static final PartModel[][] TIER_MODELS = new PartModel[TIERS][];

    public static final MenuType<ContainerExInterface> MENU = MenuTypeBuilder
            .create(ContainerExInterface::new, InterfaceLogicHost.class)
            .withMenuTitle(TieredInterfaces::title)
            .build("gtocore_tiered_interface");

    static {
        for (int tier = 1; tier < TIERS; tier++) register(tier);
    }

    private TieredInterfaces() {}

    private static String suffix(int tier) {
        return SpeedCards.roman(tier).toLowerCase(Locale.ROOT);
    }

    private static void register(int tier) {
        var roman = SpeedCards.roman(tier);
        var id = "me_interface_" + suffix(tier);
        var block = BlockRegisterUtils.block(id, "ME接口(" + roman + ")", p -> new InterfaceBlock(tier))
                .lang("ME Interface (" + roman + ")")
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item((b, p) -> new InterfaceBlockItem(b, p, tier))
                .build()
                .register();
        BLOCKS[tier] = block;
        TYPES[tier] = GTO.blockEntity(id, InterfaceEntity::new).validBlocks(block).register();
        var partId = "cable_me_interface_" + suffix(tier);
        PARTS[tier] = ItemRegisterUtils.item(partId, "ME接口(" + roman + ")", p -> new InterfacePartItem(p, tier))
                .lang("ME Interface (" + roman + ")")
                .model((ctx, prov) -> prov.getBuilder(ctx.getName())
                        .parent(new ModelFile.UncheckedModelFile(AppEng.makeId("item/cable_interface")))
                        .texture("front", GTOCore.id("part/" + id))
                        .texture("sides", GTOCore.id("part/" + id + "_sides"))
                        .texture("back", GTOCore.id("part/" + id + "_back"))
                        .texture("particle", GTOCore.id("part/" + id + "_back")))
                .register();
        var front = GTOCore.id("part/" + id);
        TIER_MODELS[tier] = new PartModel[] {
                new PartModel(front, AppEng.makeId("part/interface_off")),
                new PartModel(front, AppEng.makeId("part/interface_on")),
                new PartModel(front, AppEng.makeId("part/interface_has_channel"))
        };
        PartModels.registerModels(front);
    }

    public static ItemLike block(int tier) {
        return tier == 0 ? AEBlocks.INTERFACE : BLOCKS[tier].get();
    }

    public static ItemLike part(int tier) {
        return tier == 0 ? AEParts.INTERFACE : PARTS[tier].get();
    }

    public static void init() {
        ((BlockAccessor) AEBlocks.INTERFACE.block()).gtocore$setDescriptionId(NAME_I_BLOCK);
        ((ItemAccessor) AEParts.INTERFACE.asItem()).gtocore$setDescriptionId(NAME_I_PART);
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(TieredInterfaces::onCommonSetup);
        bus.addListener(TieredInterfaces::onLoadComplete);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (int tier = 1; tier < TIERS; tier++) {
                var type = TYPES[tier].get();
                BLOCKS[tier].get().setBlockEntity(InterfaceEntity.class, type, null, null);
                AEBaseBlockEntity.registerBlockEntityItem(type, BLOCKS[tier].get().asItem());
            }
        });
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            var discontinued = TipSpec.builder().lines(l -> l.line(TipRole.WARNING, Component.translatable(DISCONTINUED))).build();
            for (var item : new ItemLike[] { EPPItemAndBlock.EX_INTERFACE, EPPItemAndBlock.EX_INTERFACE_PART, EPPItemAndBlock.INTERFACE_UPGRADE }) {
                ((IItem) item.asItem()).gtolib$setTips(discontinued);
            }
            for (int tier = 0; tier < TIERS; tier++) {
                var tips = tips(tier);
                ((IItem) block(tier).asItem()).gtolib$setTips(tips);
                ((IItem) part(tier).asItem()).gtolib$setTips(tips);
                if (tier == 0) continue;
                for (var machine : new ItemLike[] { block(tier), part(tier) }) {
                    Upgrades.add(AEItems.FUZZY_CARD, machine, 1, GROUP);
                    Upgrades.add(AEItems.CRAFTING_CARD, machine, 1, GROUP);
                }
            }
        });
    }

    private static TipSpec tips(int tier) {
        int slots = tier == 0 ? 9 : SLOTS;
        long multiplier = MULTIPLIERS[tier];
        long items = AEKeyType.items().getAmountPerUnit() * 64L * multiplier;
        long buckets = GenericSlotCapacities.getMap().getOrDefault(AEKeyType.fluids(), 0L) * multiplier / AEKeyType.fluids().getAmountPerUnit();
        return TipSpec.builder().lines(l -> {
            l.line(TipRole.FEATURE, Component.translatable(SLOTS_KEY, slots, slots));
            l.line(TipRole.FEATURE, Component.translatable(CAPACITY, FormattingUtil.formatNumbers(multiplier), FormattingUtil.formatNumbers(items), FormattingUtil.formatNumbers(buckets)));
            if (tier > 0) l.line(TipRole.FEATURE, Component.translatable(UPGRADE));
        }).build();
    }

    private static Component title(InterfaceLogicHost host) {
        if (host instanceof Nameable nameable && nameable.hasCustomName()) return nameable.getCustomName();
        return host.getMainMenuIcon().getHoverName();
    }

    private static void applyTier(InterfaceLogic logic, int tier) {
        long multiplier = MULTIPLIERS[tier];
        Ae2Reflect.setInterfaceConfig(logic, new TieredInventory(GenericStackInv.Mode.CONFIG_STACKS, () -> Ae2Reflect.onInterfaceConfigChange(logic), false, multiplier));
        Ae2Reflect.setInterfaceStorage(logic, new TieredInventory(GenericStackInv.Mode.STORAGE, () -> Ae2Reflect.onInterfaceStorageChange(logic), true, multiplier));
        logic.getConfig().useRegisteredCapacities();
        logic.getStorage().useRegisteredCapacities();
    }

    private static int tierOf(Object host) {
        if (host instanceof InterfaceEntity entity) return entity.tier();
        if (host instanceof TieredPart part) return part.tier;
        var type = host.getClass();
        return type == InterfaceBlockEntity.class || type == InterfacePart.class || type == TileExInterface.class || type == PartExInterface.class ? 0 : -1;
    }

    private static void returnItem(Player player, ItemStack held, ItemStack old) {
        if (!player.getAbilities().instabuild) held.shrink(1);
        player.getInventory().placeItemBackInInventory(old);
    }

    public static void recipes() {
        for (int tier = 1; tier < TIERS; tier++) {
            var suffix = suffix(tier);
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("me_interface_" + suffix), new ItemStack(block(tier)),
                    " A ",
                    "ACA",
                    " A ",
                    'A', ConventionTags.INTERFACE, 'C', CIRCUITS[tier]);
            VanillaRecipeHelper.addShapelessRecipe(GTOCore.id("cable_me_interface_" + suffix), new ItemStack(part(tier)), new ItemStack(block(tier)));
            VanillaRecipeHelper.addShapelessRecipe(GTOCore.id("me_interface_" + suffix + "_from_part"), new ItemStack(block(tier)), new ItemStack(part(tier)));
        }
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static final String LEGACY_NAMESPACE = "expatternprovider";
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static final String LEGACY_BLOCK = "oversize_interface";
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static final String LEGACY_PART = "oversize_interface_part";
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static final String LEGACY_ENTITY_ID = LEGACY_NAMESPACE + ":" + LEGACY_BLOCK;

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static void remap(MissingMappingsEvent event) {
        var block = new ResourceLocation(LEGACY_NAMESPACE, LEGACY_BLOCK);
        var part = new ResourceLocation(LEGACY_NAMESPACE, LEGACY_PART);
        for (var mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, LEGACY_NAMESPACE)) {
            if (mapping.getKey().equals(block)) mapping.remap(BLOCKS[1].get());
        }
        for (var mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, LEGACY_NAMESPACE)) {
            if (mapping.getKey().equals(block)) mapping.remap(BLOCKS[1].get().asItem());
            else if (mapping.getKey().equals(part)) mapping.remap(PARTS[1].get());
        }
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static void migrateBlockEntityTag(CompoundTag tag) {
        if (LEGACY_ENTITY_ID.equals(tag.getString("id"))) tag.putString("id", TYPES[1].getId().toString());
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static boolean isLegacyId(ResourceLocation id) {
        return LEGACY_NAMESPACE.equals(id.getNamespace()) && (LEGACY_BLOCK.equals(id.getPath()) || LEGACY_PART.equals(id.getPath()));
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static boolean isLegacy(Object machine) {
        return machine == EPPItemAndBlock.OVERSIZE_INTERFACE || machine == EPPItemAndBlock.OVERSIZE_INTERFACE_PART;
    }

    private static final class TieredInventory extends ConfigInventory {

        private final boolean overstacking;
        private final long multiplier;

        private TieredInventory(GenericStackInv.Mode mode, Runnable listener, boolean overstacking, long multiplier) {
            super(null, mode, SLOTS, listener, overstacking);
            this.overstacking = overstacking;
            this.multiplier = multiplier;
        }

        @Override
        public void useRegisteredCapacities() {
            GenericSlotCapacities.getMap().forEach((type, capacity) -> setCapacity(type, saturatedMultiply(capacity, multiplier)));
        }

        @Override
        public long getMaxAmount(AEKey key) {
            if (!overstacking && key instanceof AEItemKey item) {
                return Math.min(saturatedMultiply(item.getMaxStackSize(), multiplier), getCapacity(key.getType()));
            }
            return getCapacity(key.getType());
        }

        private static long saturatedMultiply(long a, long b) {
            long high = Math.multiplyHigh(a, b);
            long low = a * b;
            return high != 0 || low < 0 ? Long.MAX_VALUE : low;
        }
    }

    public static final class InterfaceBlock extends BlockBaseGui<InterfaceEntity> {

        private final int tier;

        private InterfaceBlock(int tier) {
            super(AEBaseBlock.metalProps());
            this.tier = tier;
        }

        @Override
        public void openGui(InterfaceEntity entity, Player player) {
            entity.openMenu(player, MenuLocators.forBlockEntity(entity));
        }
    }

    public static final class InterfaceEntity extends TileExInterface {

        public InterfaceEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        private int tier() {
            return ((InterfaceBlock) getBlockState().getBlock()).tier;
        }

        @Override
        protected InterfaceLogic createLogic() {
            var logic = new InterfaceLogic(getMainNode(), this, getBlockState().getBlock().asItem(), SLOTS);
            applyTier(logic, tier());
            return logic;
        }

        @Override
        public void openMenu(Player player, MenuLocator locator) {
            MenuOpener.open(MENU, player, locator);
        }

        @Override
        public void returnToMainMenu(Player player, ISubMenu subMenu) {
            MenuOpener.returnTo(MENU, player, subMenu.getLocator());
        }

        @Override
        public ItemStack getMainMenuIcon() {
            return new ItemStack(getBlockState().getBlock());
        }
    }

    public static final class TieredPart extends PartExInterface {

        private final int tier;

        public TieredPart(IPartItem<?> partItem) {
            super(partItem);
            this.tier = ((InterfacePartItem) partItem).tier;
            applyTier(getInterfaceLogic(), tier);
        }

        @Override
        public void openMenu(Player player, MenuLocator locator) {
            MenuOpener.open(MENU, player, locator);
        }

        @Override
        public void returnToMainMenu(Player player, ISubMenu subMenu) {
            MenuOpener.returnTo(MENU, player, subMenu.getLocator());
        }

        @Override
        public void exposedStorageChanged() {
            var host = getHost();
            if (host != null) host.partStorageChanged();
        }

        @Override
        public IPartModel getStaticModels() {
            if (isActive() && isPowered()) return TIER_MODELS[tier][2];
            return isPowered() ? TIER_MODELS[tier][1] : TIER_MODELS[tier][0];
        }
    }

    public static final class InterfaceBlockItem extends AEBaseBlockItem {

        private final int tier;

        private InterfaceBlockItem(Block block, Properties properties, int tier) {
            super(block, properties);
            this.tier = tier;
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            var player = context.getPlayer();
            if (player == null || !InteractionUtil.isInAlternateUseMode(player)) return super.onItemUseFirst(stack, context);
            var level = context.getLevel();
            var pos = context.getClickedPos();
            var entity = level.getBlockEntity(pos);
            if (entity == null) return super.onItemUseFirst(stack, context);
            int current = tierOf(entity);
            if (current < 0 || current >= tier) return super.onItemUseFirst(stack, context);
            if (!level.isClientSide()) {
                var tag = entity.saveWithoutMetadata();
                var old = new ItemStack(entity.getBlockState().getBlock());
                ((AEBaseBlockEntity) entity).clearContent();
                level.setBlock(pos, getBlock().defaultBlockState(), Block.UPDATE_ALL);
                if (level.getBlockEntity(pos) instanceof InterfaceEntity replaced) {
                    replaced.load(tag);
                    replaced.setChanged();
                    replaced.markForUpdate();
                }
                returnItem(player, stack, old);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    public static final class InterfacePartItem extends PartItem<TieredPart> {

        private final int tier;

        private InterfacePartItem(Properties properties, int tier) {
            super(properties, TieredPart.class, TieredPart::new);
            this.tier = tier;
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            var player = context.getPlayer();
            if (player == null || !InteractionUtil.isInAlternateUseMode(player)) return super.onItemUseFirst(stack, context);
            var level = context.getLevel();
            if (!(level.getBlockEntity(context.getClickedPos()) instanceof IPartHost host)) return super.onItemUseFirst(stack, context);
            var selected = host.selectPartWorld(context.getClickLocation());
            if (selected.part == null) return super.onItemUseFirst(stack, context);
            int current = tierOf(selected.part);
            if (current < 0 || current >= tier) return super.onItemUseFirst(stack, context);
            if (!level.isClientSide()) {
                var tag = new CompoundTag();
                selected.part.writeToNBT(tag);
                var old = new ItemStack(selected.part.getPartItem());
                var replaced = host.replacePart(this, selected.side, player, context.getHand());
                if (replaced != null) {
                    replaced.readFromNBT(tag);
                    host.markForSave();
                    host.markForUpdate();
                    returnItem(player, stack, old);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    public static final class Screen extends GuiExInterface {

        public Screen(ContainerExInterface menu, Inventory playerInventory, Component title, ScreenStyle style) {
            super(menu, playerInventory, title, style);
            setTextContent(AEBaseScreen.TEXT_ID_DIALOG_TITLE, title);
        }
    }
}
