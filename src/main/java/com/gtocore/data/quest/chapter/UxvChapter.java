package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.machines.MultiBlockA;

import com.gtolib.api.quest.QuestChapter;

public final class UxvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("uxv", c -> c
            .title("15. 极顶压(UXV)")
            .subtitle("Tier12-UXV")
            .icon(MultiBlockA.QUANTUM_FORCE_TRANSFORMER)
            .alwaysInvisible());
}
