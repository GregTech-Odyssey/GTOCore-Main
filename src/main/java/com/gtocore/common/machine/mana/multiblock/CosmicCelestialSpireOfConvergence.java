package com.gtocore.common.machine.mana.multiblock;

import com.gtocore.client.renderer.RingStructureData;
import com.gtocore.client.renderer.StructureVBO;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.machine.mana.CelestialHandler;
import com.gtocore.data.IdleReason;

import com.gtolib.api.GTOValues;
import com.gtolib.utils.ClientUtil;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.gto.datasynclib.annotations.SaveToDisk;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gtocore.common.machine.mana.CelestialHandler.*;

public class CosmicCelestialSpireOfConvergence extends ManaMultiblockMachine {

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor manaMonitor = holder.monitorTick(GTOTickTimeMonitors.MANA, this::tickUpdate);

    private final CelestialHandler celestialHandler;

    @Getter
    @SaveToDisk(defaultValue = "0")
    private long solaris = 0;
    @Getter
    @SaveToDisk(defaultValue = "0")
    private long lunara = 0;
    @Getter
    @SaveToDisk(defaultValue = "0")
    private long voidflux = 0;
    @Getter
    @SaveToDisk(defaultValue = "0")
    private long stellarm = 0;

    private CelestialHandler.Mode mode = CelestialHandler.Mode.OVERWORLD;

    @SaveToDisk(defaultValue = "0")
    private short accelerate = 0;

    private int timing;
    private final ConditionalSubscriptionHandler tickSubs;

    public CosmicCelestialSpireOfConvergence(MetaMachineBlockEntity holder) {
        super(holder);
        this.celestialHandler = new CelestialHandler(5000000000000000000L);
        tickSubs = new ConditionalSubscriptionHandler(this, manaMonitor, 10, this::isFormed);
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        int solarisCost = recipe.data.getInt(SOLARIS);
        int lunaraCost = recipe.data.getInt(LUNARA);
        int voidfluxCost = recipe.data.getInt(VOIDFLUX);
        int stellarmCost = recipe.data.getInt(STELLARM);
        int anyCost = recipe.data.getInt(ANY);

        long parallel = 0;
        if (solarisCost > 0) parallel = this.solaris / solarisCost;
        else if (lunaraCost > 0) parallel = this.lunara / lunaraCost;
        else if (voidfluxCost > 0) parallel = this.voidflux / voidfluxCost;
        else if (stellarmCost > 0) parallel = this.stellarm / stellarmCost;
        else if (anyCost > 0)
            parallel = (this.solaris + this.lunara + this.voidflux + this.stellarm) / anyCost;
        if (parallel == 0) {
            if (solarisCost <= 0 && lunaraCost <= 0 && voidfluxCost <= 0 && stellarmCost <= 0 && anyCost <= 0) {
                IdleReason.NOT_APPLICABLE.setReason(this);
            } else {
                long need = solarisCost > 0 ? solarisCost : lunaraCost > 0 ? lunaraCost : voidfluxCost > 0 ? voidfluxCost : stellarmCost > 0 ? stellarmCost : anyCost;
                long have = solarisCost > 0 ? this.solaris : lunaraCost > 0 ? this.lunara : voidfluxCost > 0 ? this.voidflux : stellarmCost > 0 ? this.stellarm : this.solaris + this.lunara + this.voidflux + this.stellarm;
                IdleReason.CELESTIAL_SHORT.setReason(this, need, have);
            }
            return null;
        }
        recipe = ParallelLogic.accurateParallel(this, unit, recipe, parallel);

        if (recipe == null) return null;
        parallel = recipe.parallels;

        ResourceResult deductResult = null;
        if (solarisCost > 0) {
            deductResult = celestialHandler.deductResource(SOLARIS, solarisCost, parallel, this.solaris, this.lunara, this.voidflux, this.stellarm);
        } else if (lunaraCost > 0) {
            deductResult = celestialHandler.deductResource(LUNARA, lunaraCost, parallel, this.solaris, this.lunara, this.voidflux, this.stellarm);
        } else if (voidfluxCost > 0) {
            deductResult = celestialHandler.deductResource(VOIDFLUX, voidfluxCost, parallel, this.solaris, this.lunara, this.voidflux, this.stellarm);
        } else if (stellarmCost > 0) {
            deductResult = celestialHandler.deductResource(STELLARM, stellarmCost, parallel, this.solaris, this.lunara, this.voidflux, this.stellarm);
        } else {
            deductResult = celestialHandler.deductResource(ANY, anyCost, parallel, this.solaris, this.lunara, this.voidflux, this.stellarm);
        }

        if (deductResult.success()) {
            this.solaris = deductResult.solaris();
            this.lunara = deductResult.lunara();
            this.voidflux = deductResult.voidflux();
            this.stellarm = deductResult.stellarm();
        }
        return recipe;
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (MultiblockPage.isScreenText()) return;
        if (isFormed()) {
            textList.add(Component.translatable("gtocore.machine.oc_amount", accelerate)
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("gtocore.machine.steam_parallel_machine.oc")))));
        }

        if (this.solaris > 0)
            textList.add(Component.translatable("gtocore.celestial_condenser." + SOLARIS, this.solaris));
        if (this.lunara > 0)
            textList.add(Component.translatable("gtocore.celestial_condenser." + LUNARA, this.lunara));
        if (this.voidflux > 0)
            textList.add(Component.translatable("gtocore.celestial_condenser." + VOIDFLUX, this.voidflux));
        if (this.stellarm > 0)
            textList.add(Component.translatable("gtocore.celestial_condenser." + STELLARM, this.stellarm));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gtocore.celestial_condenser.solaris", MultiblockPage.numberText(() -> solaris, ""));
        page.addReading("gtocore.celestial_condenser.lunara", MultiblockPage.numberText(() -> lunara, ""));
        page.addReading("gtocore.celestial_condenser.voidflux", MultiblockPage.numberText(() -> voidflux, ""));
        page.addReading("gtocore.celestial_condenser.stellarm", MultiblockPage.numberText(() -> stellarm, ""));
    }

    @Override
    public void addControls(ControlPanel controls) {
        super.addControls(controls);
        controls.addInt("gtocore.machine.steam_parallel_machine.oc_amount", () -> accelerate, value -> accelerate = (short) value, 0, 4, "gtocore.machine.steam_parallel_machine.oc")
                .disabled(() -> !isFormed(), MultiblockPage.STATE_UNFORMED);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        tickSubs.initialize(getLevel());
    }

    @Override
    public void onStructureFormedClient() {
        super.onStructureFormedClient();
        removeBlockFromWorld();
    }

    @Override
    public void onStructureInvalidClient() {
        super.onStructureFormedClient();
        addBlockToWorld();
    }

    private void tickUpdate() {
        Level world = getLevel();
        if (world == null) return;
        if (timing == 0) {
            getRecipeLogic().updateTickSubscription();
            timing = 40;
        } else {
            timing--;
        }
        Resource updatedResources = celestialHandler.increase(world, getMultiple() * 100, this.solaris, this.lunara, this.voidflux, this.stellarm, this.mode);
        this.solaris = updatedResources.solaris();
        this.lunara = updatedResources.lunara();
        this.voidflux = updatedResources.voidflux();
        this.stellarm = updatedResources.stellarm();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() != null) {
            this.mode = celestialHandler.initMode(getLevel());
        }
    }

    private int getMultiple() {
        if (accelerate > 0) {
            int cost = GTOValues.MANA[accelerate * 2 + 4] * 2;
            if (cost > removeMana(cost, 1, false)) {
                accelerate = 0;
            } else {
                if (cost > removeMana(cost, 1, true)) {
                    accelerate = 0;
                }
            }
        }
        return 1 << (accelerate * 5);
    }

    private boolean removeBlockFromWorld() {
        String[][] structure = RingStructureData.tinyLight;
        for (int x = 0; x < structure.length; x++) {
            String[] plane = structure[x];
            for (int y = 0; y < plane.length; y++) {
                String row = plane[y];
                for (int z = 0; z < row.length(); z++) {
                    char letter = row.charAt(z);
                    if (letter == ' ') continue;
                    BlockPos realPos = getRealPos(x, y, z);
                    if (!getLevel().isLoaded(realPos)) return false;
                    getLevel().setBlock(realPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE);
                    ClientUtil.getPreventUpdate(getLevel()).add(realPos.asLong());
                }
            }
        }
        return true;
    }

    private boolean addBlockToWorld() {
        StructureVBO ringStructure = (new StructureVBO())
                .addMapping('X', GTOBlocks.THE_SOLARIS_LENS.get())
                .addMapping('[', RegistriesUtils.getBlock("ars_nouveau:sky_block"));

        String[][] structure = RingStructureData.tinyLight;
        ringStructure.assignStructure(structure);

        for (int x = 0; x < structure.length; x++) {
            String[] plane = structure[x];
            for (int y = 0; y < plane.length; y++) {
                String row = plane[y];
                for (int z = 0; z < row.length(); z++) {
                    char letter = row.charAt(z);
                    if (letter == ' ') continue;
                    BlockPos realPos = getRealPos(x, y, z);
                    if (!getLevel().isLoaded(realPos)) return false;
                    BlockState blockState = ringStructure.mapper.get(letter).defaultBlockState();
                    ClientUtil.getPreventUpdate(getLevel()).remove(realPos.asLong());
                    getLevel().setBlock(realPos, blockState, Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
        return true;
    }

    private BlockPos getRealPos(int x, int y, int z) {
        String[][] structure = RingStructureData.tinyLight;
        BlockPos.MutableBlockPos pos = BlockPos.ZERO.offset(5 + structure.length / 2 - x, -structure[0].length / 2 + y + 8, -structure[0][0].length() / 2 + z).mutable();
        switch (getFrontFacing()) {
            case EAST -> pos.set(-pos.getX(), pos.getY(), -pos.getZ());
            case NORTH -> pos.set(-pos.getZ(), pos.getY(), pos.getX());
            case SOUTH -> pos.set(pos.getZ(), pos.getY(), -pos.getX());
        }
        return pos.offset(this.getPos());
    }
}
