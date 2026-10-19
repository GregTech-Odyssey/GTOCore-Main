package com.gtocore.integration.emi;

import dev.ftb.mods.ftbxmodcompat.ftbquests.jei.FTBQuestsJEIIntegration;

public class NotDevCompat {

    public static void clearRuntime() {
        FTBQuestsJEIIntegration.runtime = null;
    }
}
