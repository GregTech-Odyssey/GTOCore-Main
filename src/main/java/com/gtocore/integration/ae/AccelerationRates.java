package com.gtocore.integration.ae;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.IItem;

import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ItemLike;

import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEKeyType;
import appeng.api.upgrades.Upgrades;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.settings.TickRates;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.parts.automation.IOBusPart;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.glodblock.github.extendedae.common.tileentities.TileCircuitCutter;
import com.glodblock.github.extendedae.common.tileentities.TileExInscriber;
import com.glodblock.github.extendedae.common.tileentities.TileExMolecularAssembler;
import com.glodblock.github.extendedae.util.FCUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToLongFunction;
import java.util.function.Supplier;

/**
 * 受加速卡影响的 AE / EAE 机器的速度表与提示：EAE 专属速度表在此（mixin 引用），其余读 AE 源的公开静态方法与实例，提示按运行时数据生成。
 */
@DataGeneratorScanned
public final class AccelerationRates {

    @RegisterLanguage(cn = "各加速卡数量下的速度", en = "Speed by installed cards")
    public static final String HEADING = "gtocore.acceleration.heading";
    @RegisterLanguage(cn = "无加速卡", en = "No cards")
    public static final String NONE = "gtocore.acceleration.none";
    @RegisterLanguage(cn = "加速卡 ×%s", en = "Acceleration Card x%s")
    public static final String SPEED = "gtocore.acceleration.speed";
    @RegisterLanguage(cn = "超级加速卡 ×%s", en = "Super Acceleration Card x%s")
    public static final String SUPER = "gtocore.acceleration.super";
    @RegisterLanguage(cn = "%s：%s 物品/秒，%s B 流体/秒", en = "%s: %s items/s, %s B fluid/s")
    public static final String TRANSFER = "gtocore.acceleration.transfer";
    @RegisterLanguage(cn = "%s：丢出 %s 物品/秒", en = "%s: drops %s items/s")
    public static final String DROP = "gtocore.acceleration.drop";
    @RegisterLanguage(cn = "%s：%s 次/秒", en = "%s: %s crafts/s")
    public static final String CRAFTS = "gtocore.acceleration.crafts";
    @RegisterLanguage(cn = "%s：%s 次/秒（%s 线程合计）", en = "%s: %s crafts/s (%s threads)")
    public static final String CRAFTS_THREADS = "gtocore.acceleration.crafts_threads";
    @RegisterLanguage(cn = "%s：最高 %s AE/t", en = "%s: up to %s AE/t")
    public static final String ENERGY = "gtocore.acceleration.energy";
    @RegisterLanguage(cn = "%s：每发威力 ×%s", en = "%s: shot power x%s")
    public static final String SHOT = "gtocore.acceleration.shot";
    @RegisterLanguage(cn = "设定数量超过单轮可输出量时不工作", en = "Stops when the configured amount exceeds what one round can export")
    public static final String PRECISE_NOTE = "gtocore.acceleration.precise_note";

    private static final double TICKS_PER_SECOND = 20.0;
    private static final int EX_ASSEMBLER_PROGRESS_MULTIPLIER = 2;
    private static final long EX_IO_PORT_BASE_OPERATIONS = 2048;

    private AccelerationRates() {}

    public static long exIoPortOperations(int speedCards) {
        return EX_IO_PORT_BASE_OPERATIONS * switch (speedCards) {
            case 1 -> 2;
            case 2 -> 8;
            case 3 -> 32;
            case 4 -> 128;
            case 5 -> 512;
            default -> 1;
        };
    }

    public static int exAssemblerProgressPerTick(int speedCards) {
        return EX_ASSEMBLER_PROGRESS_MULTIPLIER * MolecularAssemblerBlockEntity.getProgressPerTick(speedCards);
    }

    public static int exAssemblerTicksPerCraft(int speedCards) {
        return Mth.positiveCeilDiv(MolecularAssemblerBlockEntity.MAX_PROGRESS, exAssemblerProgressPerTick(speedCards));
    }

    public static int circuitCutterTicksPerCraft(int speedCards) {
        return Mth.positiveCeilDiv(TileCircuitCutter.MAX_PROGRESS, FCUtil.speedCardMap(speedCards)) * TickRates.Inscriber.getMin();
    }

    public static double busOperationsPerSecond(IOBusPart part, int superCards, int speedCards) {
        var upgrades = part.getUpgrades();
        int multiplier = part.getOperationsPerWorkCall() / SuperSpeedCard.busOperations(upgrades);
        long operations = SuperSpeedCard.scale(IOBusPart::getOperationsForSpeedCards, upgrades.getMaxInstalled(AEItems.SPEED_CARD), superCards, speedCards);
        return (double) operations * multiplier * part.getWorkRepeats() * TICKS_PER_SECOND / part.getTickRates().getMin();
    }

    public static double ioPortOperationsPerSecond(IntToLongFunction table, ItemLike machine, int superCards, int speedCards) {
        long operations = SuperSpeedCard.scale(table, Upgrades.getMaxInstallable(AEItems.SPEED_CARD, machine), superCards, speedCards);
        return operations * TICKS_PER_SECOND / TickRates.IOPort.getMin();
    }

    public static void init() {
        bus(AEParts.IMPORT_BUS.asItem(), null);
        bus(AEParts.EXPORT_BUS.asItem(), null);
        bus(EPPItemAndBlock.EX_IMPORT_BUS, null);
        bus(EPPItemAndBlock.EX_EXPORT_BUS, null);
        bus(EPPItemAndBlock.THRESHOLD_EXPORT_BUS, null);
        bus(EPPItemAndBlock.TAG_EXPORT_BUS, null);
        bus(EPPItemAndBlock.MOD_EXPORT_BUS, null);
        bus(EPPItemAndBlock.PRECISE_EXPORT_BUS, Component.translatable(PRECISE_NOTE));
        ioPort(AEBlocks.IO_PORT, IOPortBlockEntity::getOperationsForSpeedCards);
        ioPort(EPPItemAndBlock.EX_IO_PORT, AccelerationRates::exIoPortOperations);
        machine(AEBlocks.MOLECULAR_ASSEMBLER, c -> Component.translatable(CRAFTS, label(0, c), number(TICKS_PER_SECOND / MolecularAssemblerBlockEntity.getTicksPerCraft(c))));
        machine(EPPItemAndBlock.EX_ASSEMBLER, c -> Component.translatable(CRAFTS_THREADS, label(0, c),
                number(TICKS_PER_SECOND / exAssemblerTicksPerCraft(c) * TileExMolecularAssembler.MAX_THREAD), TileExMolecularAssembler.MAX_THREAD));
        machine(AEBlocks.INSCRIBER, c -> Component.translatable(CRAFTS, label(0, c), number(TICKS_PER_SECOND / (InscriberBlockEntity.getTicksPerCraft(c) * TickRates.Inscriber.getMin()))));
        machine(EPPItemAndBlock.EX_INSCRIBER, c -> Component.translatable(CRAFTS_THREADS, label(0, c),
                number(TICKS_PER_SECOND / (InscriberBlockEntity.getTicksPerCraft(c) * TickRates.Inscriber.getMin()) * TileExInscriber.MAX_THREAD), TileExInscriber.MAX_THREAD));
        machine(EPPItemAndBlock.CIRCUIT_CUTTER, c -> Component.translatable(CRAFTS, label(0, c), number(TICKS_PER_SECOND / circuitCutterTicksPerCraft(c))));
        machine(AEBlocks.VIBRATION_CHAMBER, c -> Component.translatable(ENERGY, label(0, c),
                number(VibrationChamberBlockEntity.getMaxEnergyRate(AEConfig.instance().getVibrationChamberMaxEnergyPerGameTick(), c))));
        machine(AEItems.MATTER_CANNON, c -> Component.translatable(SHOT, label(0, c), MatterCannonItem.getShotPower(c)));
        machine(EPPItemAndBlock.ACTIVE_FORMATION_PLANE, c -> Component.translatable(DROP, label(0, c),
                number(IOBusPart.getOperationsForSpeedCards(c) * AEKeyType.items().getAmountPerOperation() * TICKS_PER_SECOND / TickRates.ExportBus.getMin())));
    }

    private interface Row {

        Component line(int cards);
    }

    private static void bus(IPartItem<?> item, Component note) {
        var rows = new Lazy(() -> {
            var part = (IOBusPart) item.createPart();
            var lines = new ArrayList<Component>();
            int maxSpeed = Upgrades.getMaxInstallable(AEItems.SPEED_CARD, item);
            int maxSuper = Upgrades.getMaxInstallable(SuperSpeedCard.ITEM.get(), item);
            for (int c = 0; c <= maxSpeed; c++) lines.add(transfer(0, c, busOperationsPerSecond(part, 0, c)));
            for (int c = 1; c <= maxSuper; c++) lines.add(transfer(c, 0, busOperationsPerSecond(part, c, 0)));
            return new Rows(lines, maxSpeed, maxSuper);
        });
        register(item, rows, note);
    }

    private static void ioPort(ItemLike item, IntToLongFunction table) {
        var rows = new Lazy(() -> {
            var lines = new ArrayList<Component>();
            int maxSpeed = Upgrades.getMaxInstallable(AEItems.SPEED_CARD, item);
            int maxSuper = Upgrades.getMaxInstallable(SuperSpeedCard.ITEM.get(), item);
            for (int c = 0; c <= maxSpeed; c++) lines.add(transfer(0, c, ioPortOperationsPerSecond(table, item, 0, c)));
            for (int c = 1; c <= maxSuper; c++) lines.add(transfer(c, 0, ioPortOperationsPerSecond(table, item, c, 0)));
            return new Rows(lines, maxSpeed, maxSuper);
        });
        register(item, rows, null);
    }

    private static void machine(ItemLike item, Row row) {
        var rows = new Lazy(() -> {
            var lines = new ArrayList<Component>();
            int max = Upgrades.getMaxInstallable(AEItems.SPEED_CARD, item);
            for (int c = 0; c <= max; c++) lines.add(row.line(c));
            return new Rows(lines, max, 0);
        });
        register(item, rows, null);
    }

    private static void register(ItemLike item, Lazy rows, Component note) {
        var spec = TipSpec.builder()
                .stats(l -> {
                    l.lines(() -> rows.get().summary());
                    if (note != null) l.line(TipRole.FEATURE, note);
                })
                .details(Component.translatable(HEADING), l -> l.lines(() -> rows.get().details()))
                .build();
        ((IItem) item.asItem()).gtolib$setTips(spec);
    }

    private static Component transfer(int speedCards, double operationsPerSecond) {
        return transfer(0, speedCards, operationsPerSecond);
    }

    private static Component transfer(int superCards, int speedCards, double operationsPerSecond) {
        return Component.translatable(TRANSFER, label(superCards, speedCards),
                number(operationsPerSecond * AEKeyType.items().getAmountPerOperation()),
                number(operationsPerSecond * AEKeyType.fluids().getAmountPerOperation() / AEKeyType.fluids().getAmountPerUnit()));
    }

    private static Component label(int superCards, int speedCards) {
        if (superCards > 0) return Component.translatable(SUPER, superCards);
        if (speedCards > 0) return Component.translatable(SPEED, speedCards);
        return Component.translatable(NONE);
    }

    private static String number(double value) {
        return FormattingUtil.formatNumbers(value);
    }

    private record Rows(List<Component> summary, List<Component> details) {

        Rows(List<Component> all, int maxSpeed, int maxSuper) {
            this(summaryOf(all, maxSpeed, maxSuper), detailsOf(all));
        }

        private static List<Component> summaryOf(List<Component> all, int maxSpeed, int maxSuper) {
            var out = new ArrayList<Component>(2);
            out.add(Tips.stat(all.get(maxSpeed)));
            if (maxSuper > 0) out.add(Tips.stat(all.get(all.size() - 1)));
            return List.copyOf(out);
        }

        private static List<Component> detailsOf(List<Component> all) {
            var out = new ArrayList<Component>(all.size());
            for (var c : all) out.add(Tips.detail(c));
            return List.copyOf(out);
        }
    }

    private static final class Lazy implements Supplier<Rows> {

        private final Supplier<Rows> factory;
        private Rows value;

        Lazy(Supplier<Rows> factory) {
            this.factory = factory;
        }

        @Override
        public Rows get() {
            var v = value;
            if (v == null) value = v = factory.get();
            return v;
        }
    }
}
