package vazkii.botania.common;

import com.gtolib.api.registries.GTORegistration;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

import com.gto.registrate.util.entry.BlockEntry;
import com.gto.registrate.util.entry.ItemEntry;
import com.gto.registrate.util.nullness.NonNullBiConsumer;
import com.gto.registrate.util.nullness.NonNullFunction;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import vazkii.botania.common.block.AlfheimPortalBlock;
import vazkii.botania.common.block.BlockReturnPortal;
import vazkii.botania.common.block.BotaniaFlowerBlock;
import vazkii.botania.common.block.BuriedPetalBlock;
import vazkii.botania.common.block.FloatingFlowerBlock;
import vazkii.botania.common.block.ManaSpreaderBlock;
import vazkii.botania.common.block.MysticalMushroomBlock;
import vazkii.botania.common.block.PetalApothecaryBlock;
import vazkii.botania.common.block.PottedFlowerBlock;
import vazkii.botania.common.block.PylonBlock;
import vazkii.botania.common.block.ReplicaVisualBlocks;
import vazkii.botania.common.block.RunicAltarBlock;
import vazkii.botania.common.block.corporea.CorporeaIndexBlock;
import vazkii.botania.common.block.mana.LargeManaPoolBlock;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.item.CorporeaIndexItem;
import vazkii.botania.common.item.MysticalPetalItem;
import vazkii.botania.common.item.PylonItem;
import vazkii.botania.common.item.ReplicaArmorItem;

/**
 * 复刻内容的注册包装：统一方块/物品的基础属性、模型占位与中文名登记。
 *
 * <p>
 * 方块与物品实例在类加载时立即创建并保存在映射中，注册由 Registrate 条目完成；
 * 这样其他代码可以在 Forge 注册事件之前通过 {@link #getBlock(String)} / {@link #getItem(String)} 取到实例。
 * </p>
 *
 * <p>
 * 需要朝向、柱状、楼梯、台阶、栅栏、墙、玻璃板、双层花或连接状态的方块，用对应的注册方法，
 * 使原模组的方块状态与模型可以直接套用；这些类型只提供状态，不移植原模组的逻辑行为。
 * </p>
 */
public final class BotaniaReplica {

    public static final BotaniaReplica BOTANIA = new BotaniaReplica(BotaniaRegistration.BOTANIA);
    public static final BotaniaReplica MYTHICBOTANY = new BotaniaReplica(GTORegistration.MYTHICBOTANY);
    public static final BotaniaReplica EXTRABOTANY = new BotaniaReplica(GTORegistration.EXTRABOTANY);

    private final GTORegistration registration;
    private final Object2ObjectOpenHashMap<String, Block> BLOCKS = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, Item> ITEMS = new Object2ObjectOpenHashMap<>();

    private BotaniaReplica(GTORegistration registration) {
        this.registration = registration;
    }

    /** 神秘花与荧光花：不遮挡相邻面、无碰撞，只能放在泥土标签的方块上，骨粉会长成高神秘花。 */
    public BlockEntry<Block> flower(String id, String cn, String en) {
        Block block = new BotaniaFlowerBlock(colorOf(id), propsFor(id).noOcclusion().noCollission());
        return register(id, cn, en, block);
    }

    /** 微光蘑菇：可以放在任意非空气方块上。 */
    public BlockEntry<Block> mushroom(String id, String cn, String en) {
        Block block = new MysticalMushroomBlock(colorOf(id), propsFor(id).noOcclusion().noCollission());
        return register(id, cn, en, block);
    }

    /** 埋藏花瓣：一格高的 1 像素薄层，无碰撞。 */
    public BlockEntry<Block> buriedPetal(String id, String cn, String en) {
        Block block = new BuriedPetalBlock(colorOf(id),
                propsFor(id).noOcclusion().noCollission().lightLevel(state -> 4).sound(SoundType.MOSS));
        return register(id, cn, en, block);
    }

    /** 神秘花瓣物品：点在泥土上时铺出埋藏花瓣。 */
    public ItemEntry<Item> petal(String id, String cn, String en) {
        return item(id, cn, en, properties -> new MysticalPetalItem(colorOf(id), properties));
    }

    /** 从注册名推导颜色，例如 {@code light_blue_mystical_flower} 得到淡蓝色。 */
    private static DyeColor colorOf(String id) {
        for (DyeColor color : DyeColor.values()) {
            if (id.startsWith(color.getName() + "_")) {
                return color;
            }
        }
        return DyeColor.WHITE;
    }

    /** 福鲁伊克斯魔力池：挂在 ME 网络上时池内魔力即网络魔力。 */
    public BlockEntry<Block> fluixManaPool(String id, String cn, String en) {
        return register(id, cn, en, new vazkii.botania.forge.integration.ae2.FluixManaPoolBlock(propsFor(id).noOcclusion()));
    }

    /** 魔力泵：与矿车交换魔力。 */
    public BlockEntry<Block> pump(String id, String cn, String en) {
        return register(id, cn, en, new vazkii.botania.common.block.ManaPumpBlock(propsFor(id).noOcclusion()));
    }

    /** 亚尔夫海姆返回门。 */
    public BlockEntry<Block> returnPortal(String id, String cn, String en) {
        return register(id, cn, en, new BlockReturnPortal(propsFor(id).noOcclusion()));
    }

    /** 精灵门：按多方块结构激活。 */
    public BlockEntry<Block> alfheimPortal(String id, String cn, String en) {
        return register(id, cn, en, new AlfheimPortalBlock(propsFor(id).noOcclusion()));
    }

    /** 魔力发射器：右键沿视线发射魔力弹。 */
    public BlockEntry<Block> spreader(String id, String cn, String en) {
        return register(id, cn, en, new ManaSpreaderBlock(propsFor(id).noOcclusion()));
    }

    /** 符文祭坛：投入材料后吸收魔力产出符文。 */
    public BlockEntry<Block> runicAltar(String id, String cn, String en) {
        return register(id, cn, en, new RunicAltarBlock(propsFor(id).noOcclusion()));
    }

    /** 花药台：投掷花瓣合成，形状与原模组一致。 */
    public BlockEntry<Block> apothecary(String id, String cn, String en) {
        return register(id, cn, en, new PetalApothecaryBlock(propsFor(id).noOcclusion()));
    }

    /** 盆栽：碰撞箱与花盆一致，且不遮挡相邻面。 */
    public BlockEntry<Block> potted(String id, String cn, String en) {
        return register(id, cn, en, new PottedFlowerBlock(propsFor(id).noOcclusion()));
    }

    /** 浮空花：碰撞箱为原模组的居中立方体，且不遮挡相邻方块的面。 */
    public BlockEntry<Block> floatingFlower(String id, String cn, String en) {
        return register(id, cn, en, new FloatingFlowerBlock(propsFor(id).noOcclusion()));
    }

    /** 不遮挡相邻面的方块，用于浮空岛这类小于整格的造型。 */
    public BlockEntry<Block> noOcclusion(String id, String cn, String en) {
        return register(id, cn, en, new Block(propsFor(id).noOcclusion()));
    }

    /** 实心藤：外形与藤蔓一致。 */
    public BlockEntry<Block> solidVine(String id, String cn, String en) {
        return register(id, cn, en, new vazkii.botania.common.block.SolidVineBlock(propsFor(id)));
    }

    /** 无状态方块。 */
    public BlockEntry<Block> block(String id, String cn, String en) {
        return register(id, cn, en, new Block(propsFor(id)));
    }

    /** 由调用方给出方块实例的注册，用于自带方块实体等需要在构造时决定类型的方块。 */
    public <T extends Block> BlockEntry<T> block(String id, String cn, String en, T block) {
        return register(id, cn, en, block);
    }

    /** 柱状方块，状态 {@code axis=x/y/z}。 */
    public BlockEntry<Block> axis(String id, String cn, String en) {
        return register(id, cn, en, new RotatedPillarBlock(propsFor(id)));
    }

    /** 水平四向方块，状态 {@code facing=north/south/east/west}。 */
    public BlockEntry<Block> facing(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.FacingHorizontal(propsFor(id)));
    }

    /** 水平四向且不遮挡相邻面的方块。 */
    public BlockEntry<Block> facingNoOcclusion(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.FacingHorizontal(propsFor(id).noOcclusion()));
    }

    /** 水平四向方块，状态 {@code facing=north/south/east/west}。 */
    public BlockEntry<Block> facingAny(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.FacingAny(propsFor(id)));
    }

    /** 楼梯，状态 {@code facing/half/shape}。 */
    public BlockEntry<Block> stairs(String id, String cn, String en) {
        return register(id, cn, en, new StairBlock(Blocks.STONE.defaultBlockState(), propsFor(id)));
    }

    /** 台阶，状态 {@code type=bottom/top/double}。 */
    public BlockEntry<Block> slab(String id, String cn, String en) {
        return register(id, cn, en, new SlabBlock(propsFor(id)));
    }

    /** 栅栏，状态 {@code north/east/south/west}。 */
    public BlockEntry<Block> fence(String id, String cn, String en) {
        return register(id, cn, en, new FenceBlock(propsFor(id).noOcclusion()));
    }

    /** 栅栏门，状态 {@code facing/in_wall/open}。 */
    public BlockEntry<Block> fenceGate(String id, String cn, String en) {
        return register(id, cn, en, new FenceGateBlock(propsFor(id).noOcclusion(), WoodType.OAK));
    }

    /** 墙，状态 {@code up/north/east/south/west}。 */
    public BlockEntry<Block> wall(String id, String cn, String en) {
        return register(id, cn, en, new WallBlock(propsFor(id)));
    }

    /** 玻璃板，状态 {@code north/east/south/west}。 */
    public BlockEntry<Block> pane(String id, String cn, String en) {
        return register(id, cn, en, new IronBarsBlock(propsFor(id).noOcclusion()));
    }

    /** 双层花，状态 {@code half=lower/upper}。 */
    public BlockEntry<Block> doublePlant(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.DoublePlant(propsFor(id).noCollission()));
    }

    /** 只有通电状态的方块，状态 {@code powered}。 */
    public BlockEntry<Block> powered(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.Powered(propsFor(id)));
    }

    /** 魔力池。变体决定容量与外观，方块实体统一。 */
    public BlockEntry<Block> manaPool(String id, String cn, String en, ManaPoolBlock.Variant variant) {
        return register(id, cn, en, new ManaPoolBlock(variant, propsFor(id).noOcclusion()));
    }

    /** 水晶。变体决定颜色、附魔加成与渲染模型。 */
    public BlockEntry<Block> pylon(String id, String cn, String en, PylonBlock.Variant variant) {
        return register(id, cn, en, new PylonBlock(variant, propsFor(id).noOcclusion()),
                (block, properties) -> new PylonItem((PylonBlock) block, properties));
    }

    /** 多媒体索引，方块模型为空，由方块实体渲染器绘制。 */
    public BlockEntry<Block> corporeaIndex(String id, String cn, String en) {
        return register(id, cn, en, new CorporeaIndexBlock(propsFor(id).noOcclusion()),
                (block, properties) -> new CorporeaIndexItem((CorporeaIndexBlock) block, properties));
    }

    /** 大型魔力池，状态为四向与对角连接；物品一次放置 3×3，只有核心掉落物品。 */
    public BlockEntry<Block> largePool(String id, String cn, String en) {
        BotaniaLang.add("block." + registration.getModid() + "." + id, cn, en);
        Block block = new LargeManaPoolBlock(propsFor(id).noOcclusion());
        BLOCKS.put(id, block);
        return registration.block(id, p -> block)
                .initialProperties(() -> Blocks.STONE)
                .properties(p -> p.strength(2.0F, 10.0F))
                .blockstate(NonNullBiConsumer.noop())
                .defaultLoot()
                .item(vazkii.botania.common.item.block.LargeManaPoolItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    /** 幽灵铁轨，状态 {@code shape}。 */
    public BlockEntry<Block> railShape(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.RailShaped(propsFor(id).noCollission()));
    }

    /** 精灵传送门，状态 {@code state}。 */
    public BlockEntry<Block> portalState(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.PortalStated(propsFor(id)));
    }

    /** 合成箱，状态 {@code pattern}。 */
    public BlockEntry<Block> cratePattern(String id, String cn, String en) {
        return register(id, cn, en, new ReplicaVisualBlocks.CratePatterned(propsFor(id)));
    }

    /** 不创建对应物品的方块，用于原模组中的纯逻辑方块。 */
    public BlockEntry<Block> blockNoItem(String id, String cn, String en) {
        BotaniaLang.add("block." + registration.getModid() + "." + id, cn, en);
        Block block = new Block(propsFor(id).noCollission().noLootTable());
        BLOCKS.put(id, block);
        return registration.block(id, p -> block)
                .initialProperties(() -> Blocks.STONE)
                .properties(p -> p.noCollission().noLootTable())
                .blockstate(NonNullBiConsumer.noop())
                .register();
    }

    public ItemEntry<Item> item(String id, String cn, String en) {
        return item(id, cn, en, Item::new);
    }

    /** 不可堆叠物品，用于自带库存的物品（如采花袋）。 */
    public ItemEntry<Item> unstackableItem(String id, String cn, String en, NonNullFunction<Item.Properties, ? extends Item> factory) {
        BotaniaLang.add("item." + registration.getModid() + "." + id, cn, en);
        Item item = factory.apply(new Item.Properties().stacksTo(1));
        ITEMS.put(id, item);
        return registration.item(id, p -> item)
                .model(NonNullBiConsumer.noop())
                .register();
    }

    /** 通用物品注册，由工厂决定物品类型。 */
    public ItemEntry<Item> item(String id, String cn, String en, NonNullFunction<Item.Properties, ? extends Item> factory) {
        BotaniaLang.add("item." + registration.getModid() + "." + id, cn, en);
        Item item = factory.apply(new Item.Properties());
        ITEMS.put(id, item);
        return registration.item(id, p -> item)
                .model(NonNullBiConsumer.noop())
                .register();
    }

    // ==================== 装备与工具 ====================

    public ItemEntry<Item> sword(String id, String cn, String en, Tier tier) {
        return item(id, cn, en, p -> new SwordItem(tier, 3, -2.4F, p));
    }

    public ItemEntry<Item> pickaxe(String id, String cn, String en, Tier tier) {
        return item(id, cn, en, p -> new PickaxeItem(tier, 1, -2.8F, p));
    }

    public ItemEntry<Item> axe(String id, String cn, String en, Tier tier) {
        return item(id, cn, en, p -> new AxeItem(tier, 6.0F, -3.2F, p));
    }

    public ItemEntry<Item> shovel(String id, String cn, String en, Tier tier) {
        return item(id, cn, en, p -> new ShovelItem(tier, 1.5F, -3.0F, p));
    }

    public ItemEntry<Item> hoe(String id, String cn, String en, Tier tier) {
        return item(id, cn, en, p -> new HoeItem(tier, -2, -1.0F, p));
    }

    public ItemEntry<Item> shears(String id, String cn, String en) {
        return item(id, cn, en, ShearsItem::new);
    }

    /** 锤类工具，按镐处理。 */
    public ItemEntry<Item> hammer(String id, String cn, String en, Tier tier) {
        return item(id, cn, en, p -> new PickaxeItem(tier, 2, -3.0F, p));
    }

    public ItemEntry<Item> armor(String id, String cn, String en, ArmorMaterial material, ArmorItem.Type type) {
        return item(id, cn, en, p -> new ReplicaArmorItem(material, type, p));
    }

    public ItemEntry<Item> bow(String id, String cn, String en) {
        return item(id, cn, en, BowItem::new);
    }

    public ItemEntry<Item> shield(String id, String cn, String en) {
        return item(id, cn, en, ShieldItem::new);
    }

    /**
     * 按 id 取方块实例。
     *
     * <p>
     * 方块在类加载时创建并放进映射，所以注册事件之前也能取；注册完成后请优先使用条目字段的
     * {@code get()} / {@code asStack()}，只有动态 id 或注册前的代码才用这里。
     * </p>
     */
    public Block getBlock(String id) {
        return BLOCKS.get(id);
    }

    /**
     * 按 id 取物品实例。
     *
     * <p>
     * 物品在类加载时创建并放进映射，所以注册事件之前也能取；注册完成后请优先使用条目字段的
     * {@code get()} / {@code asStack()}，只有动态 id 或注册前的代码才用这里。
     * </p>
     */
    public Item getItem(String id) {
        return ITEMS.get(id);
    }

    /** 方块属性按 id 查原模组的硬度、音效、光照等基础信息，未收录的回落到默认值。 */
    private BlockBehaviour.Properties propsFor(String id) {
        return vazkii.botania.common.block.BotaniaBlockProperties.apply(registration.getModid() + ":" + id, BlockBehaviour.Properties.of());
    }

    private <T extends Block> BlockEntry<T> register(String id, String cn, String en, T block) {
        return register(id, cn, en, block, BlockItem::new);
    }

    private <T extends Block> BlockEntry<T> register(String id, String cn, String en, T block,
                                                     java.util.function.BiFunction<Block, Item.Properties, ? extends BlockItem> itemFactory) {
        BotaniaLang.add("block." + registration.getModid() + "." + id, cn, en);
        BLOCKS.put(id, block);
        return registration.block(id, p -> block)
                .initialProperties(() -> Blocks.STONE)
                .properties(p -> p.strength(1.5F, 6.0F))
                .blockstate(NonNullBiConsumer.noop())
                .defaultLoot()
                .item(itemFactory::apply)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }
}
