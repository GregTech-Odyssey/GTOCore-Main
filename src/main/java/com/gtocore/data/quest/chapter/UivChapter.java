package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.machines.MultiBlockA;

import com.gtolib.api.quest.QuestChapter;

public final class UivChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("uiv", c -> c
            .title("14. 极巨压(UIV)")
            .subtitle("Tier11-UIV")
            .icon(MultiBlockA.MAGNETIC_CONFINEMENT_DIMENSIONALITY_SHOCK_DEVICE)
            .alwaysInvisible());
}
