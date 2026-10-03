package com.gtocore.client.screen.starmap.rocket;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

@DataGeneratorScanned
public final class RocketLang {

    @RegisterLanguage(cn = "T%s", en = "T%s")
    public static final String TIER_VALUE = "gtocore.starmap.tier_value";
    @RegisterLanguage(cn = "未乘坐火箭", en = "Not in a rocket")
    public static final String NO_ROCKET = "gtocore.starmap.no_rocket";
    @RegisterLanguage(cn = "可前往 %s / %s", en = "%s / %s reachable")
    public static final String REACHABLE = "gtocore.starmap.reachable";
    @RegisterLanguage(cn = "当前位置", en = "You are here")
    public static final String CURRENT = "gtocore.starmap.current";
    @RegisterLanguage(cn = "已解锁", en = "Unlocked")
    public static final String UNLOCKED = "gtocore.starmap.unlocked";
    @RegisterLanguage(cn = "未解锁：需要先用行星扫描卫星取得该星球的数据", en = "Locked: scan this planet with a planet scan satellite first")
    public static final String LOCKED = "gtocore.starmap.locked";
    @RegisterLanguage(cn = "点击查看详情", en = "Click for details")
    public static final String CLICK_FOR_DETAILS = "gtocore.starmap.click_for_details";
    @RegisterLanguage(cn = "点击星球查看详情并前往", en = "Click a planet to see its details and travel there")
    public static final String HELP = "gtocore.starmap.help";
    @RegisterLanguage(cn = "前往", en = "Travel")
    public static final String LAND = "gtocore.starmap.land";
    @RegisterLanguage(cn = "空间站", en = "Space Stations")
    public static final String STATIONS = "gtocore.starmap.stations";
    @RegisterLanguage(cn = "这颗星球的轨道上还没有空间站", en = "No space station in this orbit yet")
    public static final String NO_STATION = "gtocore.starmap.no_station";
    @RegisterLanguage(cn = "建造空间站", en = "Build Space Station")
    public static final String CONSTRUCT = "gtocore.starmap.construct";
    @RegisterLanguage(cn = "材料不足或该位置已有空间站", en = "Missing materials, or a station already occupies this spot")
    public static final String CONSTRUCT_UNAVAILABLE = "gtocore.starmap.construct_unavailable";
    @RegisterLanguage(cn = "未解锁", en = "Locked")
    public static final String LOCKED_SHORT = "gtocore.starmap.locked_short";
    @RegisterLanguage(cn = "所需火箭", en = "Rocket needed")
    public static final String LABEL_TIER = "gtocore.starmap.label.tier";
    @RegisterLanguage(cn = "重力", en = "Gravity")
    public static final String LABEL_GRAVITY = "gtocore.starmap.label.gravity";
    @RegisterLanguage(cn = "大气", en = "Atmosphere")
    public static final String LABEL_ATMOSPHERE = "gtocore.starmap.label.atmosphere";
    @RegisterLanguage(cn = "温度", en = "Temperature")
    public static final String LABEL_TEMPERATURE = "gtocore.starmap.label.temperature";
    @RegisterLanguage(cn = "太阳能", en = "Solar power")
    public static final String LABEL_SOLAR = "gtocore.starmap.label.solar";
    @RegisterLanguage(cn = "%s m/s²", en = "%s m/s²")
    public static final String GRAVITY_VALUE = "gtocore.starmap.gravity_value";
    @RegisterLanguage(cn = "%s ℃", en = "%s °C")
    public static final String TEMPERATURE_VALUE = "gtocore.starmap.temperature_value";
    @RegisterLanguage(cn = "可呼吸", en = "Breathable")
    public static final String BREATHABLE = "gtocore.starmap.breathable";
    @RegisterLanguage(cn = "无氧", en = "No oxygen")
    public static final String NO_OXYGEN = "gtocore.starmap.no_oxygen";
    @RegisterLanguage(cn = "%s的卫星", en = "Moon of %s")
    public static final String SATELLITE_OF = "gtocore.starmap.satellite_of";
    @RegisterLanguage(cn = "不可前往，可前往它的卫星", en = "Cannot land here; its moons can be visited")
    public static final String MOONS_ONLY = "gtocore.starmap.moons_only";
    @RegisterLanguage(cn = "你已在这颗星球上", en = "You are already here")
    public static final String HERE = "gtocore.starmap.here";
    @RegisterLanguage(cn = "WASD 或方向键平移地图，按住 Shift 加速", en = "WASD or arrow keys pan the map; hold Shift to go faster")
    public static final String HELP_KEYS = "gtocore.starmap.help_keys";
    @RegisterLanguage(cn = "需要 T%s 火箭", en = "Needs a T%s rocket")
    public static final String REQUIRED_ROCKET = "gtocore.starmap.required_rocket";
    @RegisterLanguage(cn = "当前火箭：T%s", en = "Your rocket: T%s")
    public static final String CURRENT_ROCKET = "gtocore.starmap.current_rocket";
    @RegisterLanguage(cn = "当前火箭", en = "Your rocket")
    public static final String LABEL_CURRENT_ROCKET = "gtocore.starmap.label.current_rocket";
    @RegisterLanguage(cn = "火箭等级不够，换更高阶的火箭才能前往", en = "Your rocket tier is too low for this destination")
    public static final String ROCKET_TOO_LOW = "gtocore.starmap.rocket_too_low";

    @RegisterLanguage(cn = "异界维度", en = "Other Dimensions")
    public static final String REALMS = "gtocore.starmap.realms";
    @RegisterLanguage(cn = "无法乘火箭前往", en = "Cannot be reached by rocket")
    public static final String NOT_TRAVELABLE = "gtocore.starmap.not_travelable";
    @RegisterLanguage(cn = "%s K", en = "%s K")
    public static final String KELVIN_VALUE = "gtocore.starmap.kelvin_value";

    private RocketLang() {}
}
