package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.machines.MultiBlockD;

import com.gtolib.api.quest.QuestChapter;

public final class OpvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("opv", c -> c
            .title("16. 过载压(OpV)")
            .subtitle("Tier13-OpV")
            .icon(MultiBlockD.EYE_OF_HARMONY)
            .alwaysInvisible());
}
