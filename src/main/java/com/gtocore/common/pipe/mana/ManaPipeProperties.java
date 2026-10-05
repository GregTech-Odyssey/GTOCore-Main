package com.gtocore.common.pipe.mana;

/**
 * 魔力管道的节点数据：本档每秒能过多少魔力。
 * <p>
 * 魔力管道不存货，只在发送方与接收方之间转手，所以这里的额度就是每秒能过手的上限：
 * 一档 0.1 池（一池 = 1000000 魔力），每档翻倍。
 */
public record ManaPipeProperties(long manaPerSecond) {

    public static final ManaPipeProperties INSTANCE = new ManaPipeProperties(0);
}
