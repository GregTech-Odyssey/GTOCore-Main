package com.gtocore.common.machine.tesseract;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.world.entity.player.Player;

import java.util.List;

@DataGeneratorScanned
public interface ITesseractMarkerInteractable {

    @RegisterLanguage(cn = "高亮显示已绑定的目标", en = "Highlight Bound Targets")
    String HIGHLIGHT_TEXT = "gui.gtceu.machine.tesseract.highlight_targets";
    @RegisterLanguage(cn = "成功将目标写入传送方块", en = "Successfully Written Targets to Tesseract")
    String WRITE_SUCCESS_TEXT = "gui.gtceu.machine.tesseract.write_targets_success";
    @RegisterLanguage(cn = "成功导入目标传送方块的配置", en = "Successfully Imported Targets from Tesseract")
    String IMPORT_SUCCESS_TEXT = "gui.gtceu.machine.tesseract.import_targets_success";
    @RegisterLanguage(cn = "坐标信息卡不足以存储更多目标", en = "Not Enough Space on Coordinate Card for More Targets")
    String WRITE_FAIL_TEXT = "gui.gtceu.machine.tesseract.write_targets_fail";
    @RegisterLanguage(cn = "标签枪中没有记录目标", en = "The marker has no targets recorded")
    String WRITE_EMPTY_TEXT = "gui.gtceu.machine.tesseract.write_targets_empty";
    @RegisterLanguage(cn = "标签枪中没有位于本维度的目标", en = "The marker has no targets in this dimension")
    String WRITE_NO_TARGET_TEXT = "gui.gtceu.machine.tesseract.write_targets_no_target";
    @RegisterLanguage(cn = "已写入，跳过 %s 个其他维度的目标", en = "Written; skipped %s target(s) in other dimensions")
    String WRITE_SKIPPED_TEXT = "gui.gtceu.machine.tesseract.write_targets_skipped";

    boolean onMarkerInteract(Player player, List<TesseractDirectedTarget> targets);

    List<TesseractDirectedTarget> getMarkerTargets();
}
