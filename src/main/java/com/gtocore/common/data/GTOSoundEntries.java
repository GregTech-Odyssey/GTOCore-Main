package com.gtocore.common.data;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import static com.gtolib.api.registries.GTORegistration.GTM;

public final class GTOSoundEntries {

    public static final SoundEntry DTPF = GTM.sound("dtpf").build();
    public static final SoundEntry FUSIONLOOP = GTM.sound("fusionloop").build();

    public static void init() {}
}
