package com.gtocore.common.wireless.energy.map;

public final class GridMapToggles {

    private boolean tags = true;
    private boolean idleLines = true;
    private boolean motion = true;
    private boolean legend;
    private boolean hudCollapsed;

    public boolean isTags() {
        return tags;
    }

    public void setTags(boolean tags) {
        this.tags = tags;
    }

    public boolean isIdleLines() {
        return idleLines;
    }

    public void setIdleLines(boolean idleLines) {
        this.idleLines = idleLines;
    }

    public boolean isMotion() {
        return motion;
    }

    public void setMotion(boolean motion) {
        this.motion = motion;
    }

    public boolean isLegend() {
        return legend;
    }

    public void setLegend(boolean legend) {
        this.legend = legend;
    }

    public boolean isHudCollapsed() {
        return hudCollapsed;
    }

    public void setHudCollapsed(boolean hudCollapsed) {
        this.hudCollapsed = hudCollapsed;
    }
}
