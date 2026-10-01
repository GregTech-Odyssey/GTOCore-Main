package com.gtocore.config;

import com.gtolib.api.rule.RuleManager;

import org.jetbrains.annotations.ApiStatus;

@Deprecated(since = "0.6.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
public enum Difficulty {

    Easy,
    Normal,
    Expert;

    public static Difficulty current() {
        return values()[RuleManager.preset().defaults().ordinal()];
    }
}
