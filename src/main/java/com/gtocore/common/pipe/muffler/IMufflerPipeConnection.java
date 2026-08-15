package com.gtocore.common.pipe.muffler;

import org.jetbrains.annotations.Nullable;

public interface IMufflerPipeConnection {

    boolean isConnectedToMufflerNet();

    @Nullable
    IMufflerConduction getMufflerOutput();

    default boolean hasMufflerOutput() {
        return getMufflerOutput() != null;
    }
}
