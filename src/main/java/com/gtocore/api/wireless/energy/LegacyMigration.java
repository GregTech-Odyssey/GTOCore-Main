package com.gtocore.api.wireless.energy;

import com.gtolib.GTOCore;
import com.gtolib.api.GTOValues;
import com.gtolib.utils.GTOUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import com.hepdd.gtmthings.utils.TeamUtil;
import org.jetbrains.annotations.ApiStatus;

import java.math.BigInteger;

@Deprecated(since = "0.6.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
final class LegacyMigration {

    private static final String LEGACY_NAME = "wireless_energy_data";

    private LegacyMigration() {}

    static WirelessGrid migrate(DimensionDataStorage storage) {
        var grid = new WirelessGrid(true);
        var legacy = storage.get(LegacyData::new, LEGACY_NAME);
        if (legacy != null) {
            int records = 0;
            for (var entry : legacy.tag.getList("allEnergy", Tag.TAG_COMPOUND)) {
                var e = (CompoundTag) entry;
                if (!e.hasUUID(GTOValues.WIRELESS_ENERGY_UUID)) continue;
                records++;
                var account = grid.account(TeamUtil.getTeamUUID(e.getUUID(GTOValues.WIRELESS_ENERGY_UUID)));
                var sum = U126.toBig(account.pendingHi, account.pendingLo).add(parseBig(e.getString(GTOValues.WIRELESS_ENERGY_STORAGE)));
                account.pendingHi = U126.hi(sum);
                account.pendingLo = U126.lo(sum);
                long rate = Math.max(0, e.getLong(GTOValues.WIRELESS_ENERGY_RATE));
                if (rate > account.rate) {
                    account.rate = rate;
                    var pos = GTOUtils.readGlobalPos(e.getString(GTOValues.WIRELESS_ENERGY_DIMENSION), e.getLong(GTOValues.WIRELESS_ENERGY_POS));
                    if (pos != null) account.bindPos = pos;
                }
            }
            for (var account : grid.accounts.values()) account.rebuild();
            GTOCore.LOGGER.info("[无线电网] 从 {} 迁移了 {} 条记录，合并为 {} 个队伍账本", LEGACY_NAME, records, grid.accounts.size());
        }
        grid.setDirty();
        return grid;
    }

    private static BigInteger parseBig(String value) {
        try {
            return value.isEmpty() ? BigInteger.ZERO : new BigInteger(value).max(BigInteger.ZERO);
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }

    private static final class LegacyData extends SavedData {

        private final CompoundTag tag;

        private LegacyData(CompoundTag tag) {
            this.tag = tag;
        }

        @Override
        public CompoundTag save(CompoundTag compoundTag) {
            return tag;
        }
    }
}
