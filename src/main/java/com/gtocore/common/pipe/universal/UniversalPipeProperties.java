package com.gtocore.common.pipe.universal;

public record UniversalPipeProperties(long itemThroughput, long fluidThroughput) {

    public static final UniversalPipeProperties INSTANCE = new UniversalPipeProperties(0, 0);
}
