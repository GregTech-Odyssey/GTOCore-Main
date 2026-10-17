package com.gtocore.api.electrochemistry;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;

/** Amounts are molar equivalents, not millibuckets. */
public record IonAmount(IonDefinition ion, long amount) {

    public static final DataSyncCodec<IonAmount> CODEC = CombinedCodec.composite(
            Ions.REGISTRY_KEY.combinedCodec(), IonAmount::ion,
            DataSyncCodec.LONG_CODEC, IonAmount::amount,
            IonAmount::new);

    public IonAmount {
        if (amount <= 0) throw new IllegalArgumentException("Ion amount must be positive");
    }
}
