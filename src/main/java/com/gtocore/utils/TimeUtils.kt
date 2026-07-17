package com.gtocore.utils

import kotlin.time.Duration

fun Duration.toTicks(): Int = (this.inWholeMilliseconds.toDouble() / 1000 * 20).toInt()
