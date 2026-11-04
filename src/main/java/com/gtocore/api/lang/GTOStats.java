package com.gtocore.api.lang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.tooltip.StatKind;

import lombok.experimental.UtilityClass;

@UtilityClass
@DataGeneratorScanned
public class GTOStats {

    @RegisterLanguage(key = "gtocore.tip.stat.fly_speed", cn = "飞行速度", en = "Flight speed")
    public final StatKind FLY_SPEED = new StatKind("fly_speed", "gtocore.tip.stat.fly_speed", 0x22FF57, null);
    @RegisterLanguage(key = "gtocore.tip.stat.scene_range", cn = "取景范围", en = "Scene range")
    public final StatKind SCENE_RANGE = new StatKind("scene_range", "gtocore.tip.stat.scene_range", 0xcceF57, null);
    @RegisterLanguage(key = "gtocore.tip.stat.attack_damage", cn = "攻击伤害", en = "Attack damage")
    public final StatKind ATTACK_DAMAGE = new StatKind("attack_damage", "gtocore.tip.stat.attack_damage", 0xFF2222, null);
    @RegisterLanguage(key = "gtocore.tip.stat.reach_distance", cn = "交互距离", en = "Reach distance")
    public final StatKind REACH_DISTANCE = new StatKind("reach_distance", "gtocore.tip.stat.reach_distance", 0x22aFf7, null);
    @RegisterLanguage(key = "gtocore.tip.stat.search_range", cn = "搜寻范围", en = "Search range")
    public final StatKind SEARCH_RANGE = new StatKind("search_range", "gtocore.tip.stat.search_range", 0x83fa2, null);
}
