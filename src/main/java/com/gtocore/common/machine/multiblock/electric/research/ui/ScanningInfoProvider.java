package com.gtocore.common.machine.multiblock.electric.research.ui;

import appeng.api.stacks.AEKey;

import java.util.Set;

public interface ScanningInfoProvider {

    Set<AEKey> getAvailableAEKeys();

    Set<AEKey> getSelectedAEKeys();

    void reloadAvailableAEKeys();

    void exportSelectedAEKeys(Set<AEKey> selectedKeys);

    WorkMode getWorkMode();

    void setWorkMode(WorkMode workMode);

    enum WorkMode {
        SCAN_UNLEARNED_ONLY, // 持续寻找未学习的物品并扫描
        SCAN_UNLEARNED_ONCE, // 扫描一次未学习的物品后停止
        SCAN_SELECTED_ONLY, // 只扫描选定的物品
        SCAN_SELECTED_ONCE, // 扫描一次选定的物品后停止
    }
}
