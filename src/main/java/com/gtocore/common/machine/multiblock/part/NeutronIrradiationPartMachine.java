package com.gtocore.common.machine.multiblock.part;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.GTOTickTimeMonitors;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeStackAdapter;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.recipesearch.IntLongMap;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@DataGeneratorScanned
public final class NeutronIrradiationPartMachine extends MultiblockPartMachine implements IMachineLife {

    private final int capacity;

    @SaveToDisk
    private final StackHandler inventory;
    @Getter
    @Setter
    @SyncToClient
    @SaveToDisk(defaultValue = "0")
    private long neutronFlux; // in eV
    @SaveToDisk
    @SyncToClient
    private final int[] time;
    @SaveToDisk
    @SyncToClient
    private final int[] initialTime;
    @SaveToDisk
    @SyncToClient
    private final float[] fluxRequirements; // in keV
    @SaveToDisk
    @SyncToClient
    private final ItemStack[] outputStacks;
    private final boolean[] dirtySlots;

    private TickableSubscription radiationSubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor radiationMonitor = holder.monitorTick(GTOTickTimeMonitors.RADIATION, this::tick);
    private final RecipeHandlerUnit handlerListIn;

    public NeutronIrradiationPartMachine(MetaMachineBlockEntity holder, int capacity) {
        super(holder);
        this.capacity = capacity;
        Arrays.fill(initialTime = new int[capacity], 0);
        Arrays.fill(time = new int[capacity], 0);
        Arrays.fill(fluxRequirements = new float[capacity], 0);
        Arrays.fill(dirtySlots = new boolean[capacity], true);
        Arrays.fill(outputStacks = new ItemStack[capacity], ItemStack.EMPTY);
        inventory = new StackHandler(capacity);
        handlerListIn = RecipeHandlerUnit.of(IO.IN, inventory);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        radiationSubs = subscribeServerTick(radiationSubs, radiationMonitor, 5);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (radiationSubs != null) {
            radiationSubs.unsubscribe();
            radiationSubs = null;
        }
    }

    private void tick() {
        int neutronFluxChange = 0;
        for (int i = 0; i < capacity; ++i) {
            if (time[i] > 0 && neutronFlux >= fluxRequirements[i] * 1000) {
                neutronFluxChange += outputStacks[i].getCount();
                time[i] -= 5;
                if (time[i] <= 0) {
                    inventory.storage.setStackInSlot(i, outputStacks[i]);
                    outputStacks[i] = ItemStack.EMPTY;
                    continue;
                }
            }
            if (dirtySlots[i]) {
                handlerListIn.findRecipe(GTORecipeTypes.NEUTRON_IRRADIATION_RECIPES, (u, r) -> startIrradiation(r));
                dirtySlots[i] = false;
            }
        }
        neutronFlux -= neutronFluxChange;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    @Override
    public boolean canShared() {
        return false;
    }

    private boolean startIrradiation(GTRecipeDefinition recipe) {
        var inputs = recipe.itemInputs;
        if (inputs.size() == 0) return true;
        var ingredient = inputs.ingredient(0);
        var stacks = inventory.storage.stacks;
        for (int slot = 0; slot < capacity; ++slot) {
            if (!outputStacks[slot].isEmpty()) continue;
            ItemStack stored = stacks[slot];
            int count = stored.getCount();
            if (count == 0) continue;
            if (!ingredient.test(stored)) return false;
            var output = recipe.itemOutputs;
            if (output.size() > 0) {
                outputStacks[slot] = Keys.toStack(output.outputKey(0), count);
                time[slot] = recipe.duration;
                initialTime[slot] = recipe.duration;
                fluxRequirements[slot] = (int) recipe.data.getFloat(GTORecipeDataKeys.NEUTRON_FLUX);
            }
            inventory.storage.markAsChanged();
            return count >= inputs.amount(0);
        }
        return false;
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(capacity);
        int colSize = rowSize;
        if (capacity == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var group = new WidgetGroup(0, 0, (18 + 6) * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, (18 + 6) * rowSize + 8, 18 * colSize + 8) {

            @Override
            public void detectAndSendChanges() {
                super.detectAndSendChanges();
                if (getOffsetTimer() % 10 == 0) requestSync();
            }
        };
        var handler = new ForgeStackAdapter(inventory.storage);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int slot = index;
                var x1 = 4 + x * (18 + 6);
                container.addWidget(new SlotWidget(handler, index++, x1, 4 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT).setIngredientIO(IngredientIO.INPUT)
                        .setOnAddedTooltips((s, tooltips) -> {
                            if (!outputStacks[slot].isEmpty()) {
                                tooltips.add(Component.translatable(OUTPUT, outputStacks[slot].getHoverName()).withStyle(ChatFormatting.GRAY));
                            }
                            if (time[slot] > 0 && initialTime[slot] > 0) {
                                tooltips.add(Component.translatable(IRRADIATION_TIME,
                                        FormattingUtil.formatNumber2Places(time[slot] / 20f), FormattingUtil.formatNumber2Places(initialTime[slot] / 20f)).withStyle(ChatFormatting.GRAY));
                            }
                            if (fluxRequirements[slot] > 0) {
                                var sufficient = neutronFlux >= fluxRequirements[slot] * 1000;
                                tooltips.add(Component.translatable(NEUTRON_FLUX,
                                        Component.literal(FormattingUtil.formatNumberReadable(neutronFlux)).withStyle(sufficient ? ChatFormatting.GREEN : ChatFormatting.GOLD),
                                        FormattingUtil.formatNumberReadable((long) (fluxRequirements[slot] * 1000))));
                                if (neutronFlux < fluxRequirements[slot] * 1000) {
                                    tooltips.add(Component.translatable(INSUFFICIENT_NEUTRON_FLUX).withStyle(ChatFormatting.RED));
                                }
                            }
                        }));

                container.addWidget(new ImageWidget(x1 + 18, 4 + y * 18, 6, 18, GuiTextures.SLOT) {

                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                        Position position = getPosition();
                        Size size = getSize();
                        if (getBorder() > 0) {
                            DrawerHelper.drawBorder(graphics, position.x, position.y, size.width, size.height, getBorderColor(), getBorder());
                        }
                        drawOverlay(graphics, mouseX, mouseY, partialTicks);
                        // Draw progress bar
                        if (time[slot] > 0 && initialTime[slot] > 0) {
                            float progress = 1.0f - (float) time[slot] / initialTime[slot];
                            int barHeight = (int) (progress * (size.height - 2));
                            graphics.fill(position.x + 1, position.y + size.height - barHeight + 1, position.x + size.width - 1, position.y + size.height - 1, 0xFF00FF00);
                        }
                    }
                });
            }
        }
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }

    private final class Slots extends StackInventory {

        private Slots(int size) {
            super(size);
        }

        @Override
        public void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            dirtySlots[slot] = true;
            if (isRemote()) return;
            outputStacks[slot] = ItemStack.EMPTY;
            time[slot] = 0;
            initialTime[slot] = 0;
            fluxRequirements[slot] = 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }

    private final class StackHandler extends NotifiableStackInventory {

        private StackHandler(int capacity) {
            super(NeutronIrradiationPartMachine.this, new Slots(capacity), IO.IN, IO.BOTH);
        }

        @Override
        public void fillSearchMap(GTRecipeType type, IntLongMap map) {
            var stacks = storage.stacks;
            for (int i = 0; i < storage.size; ++i) {
                if (!outputStacks[i].isEmpty()) {
                    continue;
                }
                var stack = stacks[i];
                var amount = stack.getCount();
                if (amount > 0) {
                    type.convertKey(Keys.item(stack), amount, map);
                }
            }
        }
    }

    @RegisterLanguage(cn = "中子通量：%s/%s", en = "Neutron Flux: %s/%s")
    public static final String NEUTRON_FLUX = "gtocore.machine.neutron_irradiation.flux";
    @RegisterLanguage(cn = "中子通量不足", en = "Insufficient Neutron Flux")
    public static final String INSUFFICIENT_NEUTRON_FLUX = "gtocore.machine.neutron_irradiation.insufficient_flux";
    @RegisterLanguage(cn = "辐照时间：%ss/%ss", en = "Irradiation Time: %s/%s")
    public static final String IRRADIATION_TIME = "gtocore.machine.neutron_irradiation.time";
    @RegisterLanguage(cn = "辐照产物：%s", en = "Irradiation Output: %s")
    public static final String OUTPUT = "gtocore.machine.neutron_irradiation.output";
}
