package com.gtocore.common.item;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.tool.IExDataItem;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.utils.TeamUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@DataGeneratorScanned
public class DataCrystalItem extends Item implements IExDataItem {

    @RegisterLanguage(cn = "§n空数据", en = "§nEmpty Data")
    public static final String EMPTY_NBT_TAG = "gtocore.tooltip.item.empty_data";
    @RegisterLanguage(cn = "队伍%s的积累数据", en = "Accumulated Data of Team %s")
    public static final String TEAM_DATA_TAG = "gtocore.tooltip.item.team_data";
    @RegisterLanguage(cn = "§n容量使用: %s", en = "§nCapacity Used: %s")
    public static final String CAPACITY_TAG = "gtocore.tooltip.item.capacity";
    @RegisterLanguage(cn = "[占%sB]", en = "[Occupying %sB]")
    public static final String OCCUPY_TAG = "gtocore.tooltip.item.occupy";
    @RegisterLanguage(cn = "数据[%s]", en = "Data[%s]")
    public static final String DATA_TAG = "gtocore.tooltip.item.data";

    public static final String usedCapTag = "usedCap";
    public static final String storageTag = "storage";
    public static final String teamTag = "team";
    public final int tier;
    public final long dataCapacity;

    public DataCrystalItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        this.dataCapacity = 2L << (5 * tier + 6);
    }

    public static boolean setDataCrystalData(ItemStack output, UUID team, ResearchPoints c) {
        setTeamUUID(output, team);
        var outputTest = output.copy();
        for (var entry : c.reference2LongEntrySet()) {
            ResearchTag rt = entry.getKey();
            long amount = entry.getLongValue();
            if (!addResearchData(outputTest, rt, amount)) {
                return false;
            }
        }
        output.setTag(outputTest.getTag());
        return true;
    }

    @Override
    public boolean requireDataBank() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        UUID teamUUID = getTeamUUID(stack);
        if (teamUUID != null) {
            tooltip.add(Component.translatable(TEAM_DATA_TAG, TeamUtil.findTeamName(teamUUID)));
        } else {
            tooltip.add(Component.translatable(EMPTY_NBT_TAG));
        }
        long usedCap = stack.getOrCreateTag().getLong(usedCapTag);
        tooltip.add(Component.translatable(CAPACITY_TAG,
                Component.literal(String.format("%sB/%sB", FormattingUtil.formatNumberReadable(usedCap), FormattingUtil.formatNumberReadable(dataCapacity))).withStyle(ChatFormatting.GREEN)));
        for (var entry : getResearchData(stack).reference2LongEntrySet()) {
            ResearchTag rt = entry.getKey();
            long amount = entry.getLongValue();
            tooltip.add(Component.translatable(DATA_TAG, rt.getDisplayName())
                    .append("x" + FormattingUtil.formatNumbers(amount))
                    .append(Component.translatable(OCCUPY_TAG, FormattingUtil.formatNumberReadable(amount * rt.getBytePerPoint())))
                    .withStyle(style -> style.withColor(rt.getColor())));
        }
    }

    private static void setTeamUUID(ItemStack stack, UUID teamUUID) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(teamTag, teamUUID);
    }

    public static UUID getTeamUUID(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(teamTag)) return null;
        return tag.getUUID(teamTag);
    }

    public static boolean addResearchData(ItemStack stack, ResearchTag rt, long amount) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag storage = tag.getCompound(storageTag);
        long usedCap = tag.getLong(usedCapTag);
        long a = usedCap + amount * rt.getBytePerPoint();
        if (a > (stack.getItem() instanceof DataCrystalItem dataCrystalItem ? dataCrystalItem.dataCapacity : 0)) {
            return false;
        }
        long currentAmount = storage.getLong(rt.getName());
        storage.putLong(rt.getName(), currentAmount + amount);
        tag.put(storageTag, storage);
        tag.putLong(usedCapTag, a);
        return true;
    }

    public static ResearchPoints getResearchData(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag storage = tag.getCompound(storageTag);
        ResearchPoints dataMap = new ResearchPoints();
        for (String key : storage.getAllKeys()) {
            ResearchTag rt = ResearchTag.TAGS.get(key);
            if (rt != null) {
                dataMap.put(rt, storage.getLong(key));
            }
        }
        return dataMap;
    }
}
