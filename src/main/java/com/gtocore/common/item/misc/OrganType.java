package com.gtocore.common.item.misc;

/**
 * 器官部位。槽位数固定：翅膀 4 格，其余各 1 格。
 * <p>
 * 声明顺序即器官修改器里的排列顺序与玩家器官库存的槽位顺序（身体器官在前，翅膀在后）。
 */
public enum OrganType {

    EYE("eye", "gtocore.organ_type.eye", "眼睛", "Eye", 1),
    LUNG("lung", "gtocore.organ_type.lung", "肺", "Lung", 1),
    HEART("heart", "gtocore.organ_type.heart", "心脏", "Heart", 1),
    LIVER("liver", "gtocore.organ_type.liver", "肝脏", "Liver", 1),
    SPINE("spine", "gtocore.organ_type.spine", "脊椎", "Spine", 1),
    LEFT_ARM("left_arm", "gtocore.organ_type.left_arm", "左臂", "Left Arm", 1),
    RIGHT_ARM("right_arm", "gtocore.organ_type.right_arm", "右臂", "Right Arm", 1),
    LEFT_LEG("left_leg", "gtocore.organ_type.left_leg", "左腿", "Left Leg", 1),
    RIGHT_LEG("right_leg", "gtocore.organ_type.right_leg", "右腿", "Right Leg", 1),
    WING("wing", "gtocore.organ_type.wing", "翅膀", "Wing", 4);

    public static final OrganType[] VALUES = values();
    /// 身体器官：除翅膀外的九种，每种一格；套装等级按它们计算
    public static final OrganType[] BODY = { EYE, LUNG, HEART, LIVER, SPINE, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG };
    /// 所有部位的槽位总数
    public static final int TOTAL_SLOTS;

    static {
        int slot = 0;
        for (var type : VALUES) {
            type.firstSlot = slot;
            slot += type.slotCount;
        }
        TOTAL_SLOTS = slot;
    }

    /// 物品 id、贴图目录与物品标签里用的名字
    public final String key;
    public final String translationKey;
    public final String cn;
    public final String en;
    public final int slotCount;
    private int firstSlot;

    OrganType(String key, String translationKey, String cn, String en, int slotCount) {
        this.key = key;
        this.translationKey = translationKey;
        this.cn = cn;
        this.en = en;
        this.slotCount = slotCount;
    }

    /** 该部位在玩家器官库存里的第一个槽位。 */
    public int firstSlot() {
        return firstSlot;
    }

    public boolean isBody() {
        return this != WING;
    }
}
