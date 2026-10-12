package com.gtocore.api.electrochemistry;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HalfReaction {

    public static final DataSyncCodec<HalfReaction> CODEC = CombinedCodec.composite(
            CombinedCodec.list(IonAmount.CODEC), HalfReaction::ions,
            CombinedCodec.list(IonAmount.CODEC), HalfReaction::requiredIons,
            DataSyncCodec.BOOLEAN_CODEC, HalfReaction::fuel,
            DataSyncCodec.get(ElectrolyteType.class), HalfReaction::electrolyteType,
            // Empty collections are stored as null markers; composite decoding bypasses their codecs.
            (ions, requiredIons, fuel, electrolyteType) -> new HalfReaction(
                    ions == null ? Collections.emptyList() : ions,
                    requiredIons == null ? Collections.emptyList() : requiredIons,
                    fuel, electrolyteType));

    private final List<IonAmount> ions;
    private final List<IonAmount> requiredIons;
    private final boolean fuel;
    private final ElectrolyteType electrolyteType;
    private final long charge;
    private final Reference2LongOpenHashMap<IonDefinition> producedAmounts;
    private final Reference2LongOpenHashMap<IonDefinition> requiredAmounts;

    private HalfReaction(List<IonAmount> ions, List<IonAmount> requiredIons, boolean fuel, ElectrolyteType electrolyteType) {
        this.ions = ions;
        this.requiredIons = requiredIons;
        this.fuel = fuel;
        this.electrolyteType = Objects.requireNonNull(electrolyteType, "electrolyteType");
        long total = 0;
        for (var ion : ions) total = Math.addExact(total, Math.multiplyExact(ion.amount(), ion.ion().getCharge()));
        for (var ion : requiredIons) total = Math.subtractExact(total, Math.multiplyExact(ion.amount(), ion.ion().getCharge()));
        if (total == 0) throw new IllegalArgumentException("Half reaction must transfer charge");
        charge = total;
        producedAmounts = amounts(ions);
        requiredAmounts = amounts(requiredIons);
    }

    private static Reference2LongOpenHashMap<IonDefinition> amounts(List<IonAmount> ions) {
        if (ions.isEmpty()) return null;
        var amounts = new Reference2LongOpenHashMap<IonDefinition>(ions.size());
        for (var ion : ions) amounts.put(ion.ion(), Math.addExact(amounts.getLong(ion.ion()), ion.amount()));
        return amounts;
    }

    public List<IonAmount> ions() {
        return ions;
    }

    /** Ions consumed here, supplied exclusively by the other half reaction through the membrane. */
    public List<IonAmount> requiredIons() {
        return requiredIons;
    }

    public static Builder builder(ElectrolyteType electrolyteType) {
        return new Builder(electrolyteType);
    }

    public static final class Builder {

        private final ElectrolyteType electrolyteType;
        private List<IonAmount> ions = Collections.emptyList();
        private List<IonAmount> requiredIons = Collections.emptyList();
        private boolean fuel;

        private Builder(ElectrolyteType electrolyteType) {
            this.electrolyteType = electrolyteType;
        }

        public Builder ions(IonAmount... ions) {
            this.ions = Arrays.asList(ions);
            return this;
        }

        public Builder requiredIons(IonAmount... ions) {
            this.requiredIons = Arrays.asList(ions);
            return this;
        }

        public Builder fuel(boolean fuel) {
            this.fuel = fuel;
            return this;
        }

        public HalfReaction build() {
            return new HalfReaction(ions, requiredIons, fuel, electrolyteType);
        }
    }

    public boolean fuel() {
        return fuel;
    }

    public ElectrolyteType electrolyteType() {
        return electrolyteType;
    }

    public long charge() {
        return charge;
    }

    /** Compare scaled quantities without allowing the receiving half to supply its own inputs. */
    public boolean canSupply(HalfReaction receiver, long multiplier, long receiverMultiplier) {
        if (electrolyteType != receiver.electrolyteType) return false;
        if (receiver.requiredAmounts == null) return true;
        if (producedAmounts == null) return false;
        for (var it = receiver.requiredAmounts.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var required = it.next();
            if (Math.multiplyExact(producedAmounts.getLong(required.getKey()), multiplier) < Math.multiplyExact(required.getLongValue(), receiverMultiplier)) return false;
        }
        return true;
    }
}
