package com.gtocore.common.item.misc;

import com.gregtechceu.gtceu.api.item.ComponentItem;

/**
 * 器官物品：装进器官修改器里对应部位的槽位才生效，每格只放一件。
 */
public abstract class OrganItemBase extends ComponentItem {

    private final OrganType organType;

    protected OrganItemBase(Properties properties, OrganType organType) {
        super(properties);
        this.organType = organType;
    }

    public OrganType getOrganType() {
        return organType;
    }
}
