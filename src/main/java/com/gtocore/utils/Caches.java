package com.gtocore.utils;

import com.glodblock.github.extendedae.common.me.taglist.TagPriorityList;

public final class Caches {

    public static TagPriorityList getTagPriorityList(String white, String black) {
        return TagPriorityList.getCached(white, black);
    }
}
