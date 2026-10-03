package com.gtocore.common.wireless.energy.map;

import com.gtolib.api.data.Galaxy;

public interface GridMapNavigator {

    boolean hasGalaxy(Galaxy galaxy);

    boolean isGalaxyShown(Galaxy galaxy);

    void showGalaxy(Galaxy galaxy);

    void locateCurrent();

    void focusLine(int line);

    default void focusNode(int node) {}
}
