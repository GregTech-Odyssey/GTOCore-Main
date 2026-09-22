package com.gtocore.api.data;

import com.gtolib.api.data.Galaxy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * 太空资源获取类配方的产出索引，由各配方生成器在构建配方时顺带登记，
 * 供物品与流体的信息提示反查「哪种无人机 + 哪号电路 + 哪种燃料 / 在哪个星系或维度」能拿到它。
 * <p>
 * 只在物理客户端登记，专用服务端上所有登记均为空操作。
 */
public final class SpaceResourceIndex {

    private static final boolean ENABLED = GTCEu.isClientSide();

    /** 需要标注产地的类型，标注用的是星系还是维度由类型本身决定。 */
    public enum Place {
        NONE,
        GALAXY,
        DIMENSION
    }

    /** 五种资源获取型配方，顺序即提示中的分组顺序。 */
    public enum Type {

        MINING(Place.NONE),
        DRILLING(Place.NONE),
        DEBRIS(Place.GALAXY),
        PLANETARY_GAS(Place.DIMENSION),
        VOID_GAS(Place.DIMENSION);

        public final Place place;

        Type(Place place) {
            this.place = place;
        }
    }

    private static final Type[] TYPES = Type.values();

    /**
     * @param circuit     程序电路编号，0 表示该配方不需要电路
     * @param minDrone    最低无人机/穿梭机，该档及以上均可；null 表示该类型不需要无人机
     * @param fuels       可用燃料，任选其一；null 表示该类型不消耗燃料
     * @param place       产地（星系名或维度名），由 {@link Type#place} 决定含义；null 表示不限
     * @param chance      产出概率，以万分数计，0 表示必定产出
     * @param chanceBoost 每提升一个电压等级的概率加成，同样以万分数计
     */
    public record Entry(Type type, int circuit, @Nullable Item minDrone, FluidStack @Nullable [] fuels,
                        @Nullable Component place, int chance, int chanceBoost) {}

    private static final Reference2ObjectOpenHashMap<Material, ObjectArrayList<Entry>> BY_MATERIAL = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Item, ObjectArrayList<Entry>> BY_ITEM = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Fluid, ObjectArrayList<Entry>> BY_FLUID = new Reference2ObjectOpenHashMap<>();

    private SpaceResourceIndex() {}

    public static Type[] types() {
        return TYPES;
    }

    /** 太空采矿：产出整块矿石，按材料登记，原矿与矿粉共用。 */
    public static void addMining(int circuit, Item minDrone, FluidStack[] fuels, ItemStack[] outputs) {
        if (!ENABLED || fuels.length == 0) return;
        Entry entry = new Entry(Type.MINING, circuit, minDrone, fuels, null, 0, 0);
        for (ItemStack output : outputs) {
            Item item = output.getItem();
            var materialEntry = ChemicalHelper.getMaterialEntry(item);
            // 只有真正的矿石方块才按材料登记，其余产物（如天空石、远古残骸）只认物品本身
            if (TagPrefix.ORES.containsKey(materialEntry.tagPrefix())) {
                add(BY_MATERIAL, materialEntry.material(), entry);
            } else {
                add(BY_ITEM, item, entry);
            }
        }
    }

    /** 太空钻井。 */
    public static void addDrilling(int circuit, Item minDrone, FluidStack[] fuels, FluidStack output) {
        if (!ENABLED || fuels.length == 0) return;
        add(BY_FLUID, output.getFluid(), new Entry(Type.DRILLING, circuit, minDrone, fuels, null, 0, 0));
    }

    /** 太空浮游物质收集：产出的是粉末等成品物品，按物品登记，不牵连同材料的矿石。 */
    public static void addDebris(int circuit, Item minDrone, Galaxy galaxy, ItemLike output, int chance, int chanceBoost) {
        if (!ENABLED) return;
        Component name = Component.translatable("gtolib.galaxy.name." + galaxy.name());
        add(BY_ITEM, output.asItem(), new Entry(Type.DEBRIS, circuit, minDrone, null, name, chance, chanceBoost));
    }

    /** 行星气体抽取与虚空集气。 */
    public static void addGas(Type type, int circuit, ResourceKey<Level> dimension, Fluid output) {
        if (!ENABLED) return;
        ResourceLocation location = dimension.location();
        Component name = Component.translatable(location.getPath() + "." + location.getNamespace() + ".name");
        add(BY_FLUID, output, new Entry(type, circuit, null, null, name, 0, 0));
    }

    public static @Nullable ObjectArrayList<Entry> getByItem(Item item) {
        return BY_ITEM.get(item);
    }

    /**
     * 各石种的矿石方块、原矿与矿粉共用同一份采集信息；锭、板等更下游的形态不显示，
     * 避免把提示透传到整条加工链上。矿粉并非采矿模块的直接产出，提示文案由
     * {@link #isSecondaryForm} 区分。
     */
    public static @Nullable ObjectArrayList<Entry> getByMaterial(Item item) {
        var materialEntry = ChemicalHelper.getMaterialEntry(item);
        if (materialEntry.isEmpty()) return null;
        TagPrefix prefix = materialEntry.tagPrefix();
        if (prefix != TagPrefix.rawOre && prefix != TagPrefix.dust && !TagPrefix.ORES.containsKey(prefix)) return null;
        return BY_MATERIAL.get(materialEntry.material());
    }

    /** 矿粉要靠粉碎原矿得到，提示里须写明是「其原矿」可采，而不是矿粉本身。 */
    public static boolean isSecondaryForm(Item item) {
        return ChemicalHelper.getMaterialEntry(item).tagPrefix() == TagPrefix.dust;
    }

    public static @Nullable ObjectArrayList<Entry> getByFluid(Fluid fluid) {
        return BY_FLUID.get(fluid);
    }

    private static <K> void add(Reference2ObjectOpenHashMap<K, ObjectArrayList<Entry>> map, K key, Entry entry) {
        ObjectArrayList<Entry> entries = map.get(key);
        if (entries == null) {
            entries = new ObjectArrayList<>(2);
            map.put(key, entries);
        }
        entries.add(entry);
    }
}
