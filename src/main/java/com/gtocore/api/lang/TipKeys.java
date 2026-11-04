package com.gtocore.api.lang;

import com.gtolib.api.lang.CNEN;

import com.gregtechceu.gtceu.GTCEu;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class TipKeys {

    public static final String PREFIX = "gtocore.lang";
    public static final Object2ObjectOpenHashMap<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

    private TipKeys() {}

    public static String key(String cn, String en) {
        return key(cn, en, PREFIX);
    }

    public static String key(String cn, String en, String prefix) {
        String key = (prefix != null ? prefix : "") + "." + en.hashCode();
        key = key.replace("..", ".");
        if (LANG != null) LANG.put(key, new CNEN(cn, en));
        return key;
    }
}
