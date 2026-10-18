package com.gtocore.integration.emi.trades;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;

import io.github.prismwork.emitrades.util.TradeProfile;

public final class DetachedTradeProfile implements TradeProfile {

    private final VillagerProfession profession;
    private final VillagerTrades.ItemListing offer;
    private final int level;
    private final TradeEntitySnapshot villager;

    public DetachedTradeProfile(TradeProfile profile) {
        profession = profile.profession();
        offer = profile.offer();
        level = profile.level();
        villager = TradeEntitySnapshot.of(profile.villager());
    }

    @Override
    public VillagerProfession profession() {
        return profession;
    }

    @Override
    public VillagerTrades.ItemListing offer() {
        return offer;
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public AbstractVillager villager() {
        return villager == null ? null : (AbstractVillager) villager.entity();
    }
}
