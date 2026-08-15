package com.gtocore.common.machine.multiblock.part;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.RecipeHelper;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.fast.recipesearch.IntLongMap;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
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
import java.util.List;

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
    private final RecipeHandlerUnit handlerListIn;

    public NeutronIrradiationPartMachine(MetaMachineBlockEntity holder, int capacity) {
        super(holder);
        this.capacity = capacity;
        Arrays.fill(initialTime = new int[capacity], 0);
        Arrays.fill(time = new int[capacity], 0);
        Arrays.fill(fluxRequirements = new float[capacity], 0);
        Arrays.fill(dirtySlots = new boolean[capacity], true);
        Arrays.fill(outputStacks = new ItemStack[capacity], ItemStack.EMPTY);
        inventory = new StackHandler(this, capacity);
        handlerListIn = RecipeHandlerUnit.of(IO.IN, inventory);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        radiationSubs = subscribeServerTick(radiationSubs, this::tick, 5);
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
                    inventory.setStackInSlot(i, outputStacks[i]);
                    outputStacks[i] = ItemStack.EMPTY;
                    continue;
                }
            }
            if (dirtySlots[i]) {
                handlerListIn.findRecipe(GTORecipeTypes.NEUTRON_IRRADIATION_RECIPES, (u, r) -> handlerListIn.handleRecipeItem(IO.IN, r.toRuntime(), RecipeHelper.copyContents(r.itemInputs, 1), false));
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

    private CustomItemStackHandler createCustomHandler(int capacity) {
        return new CustomItemStackHandler(capacity) {

            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                dirtySlots[slot] = true;
                if (inventory.handling || isRemote()) return;
                outputStacks[slot] = ItemStack.EMPTY;
                time[slot] = 0;
                initialTime[slot] = 0;
                fluxRequirements[slot] = 0;
                inventory.onContentsChanged();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
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
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int slot = index;
                var x1 = 4 + x * (18 + 6);
                container.addWidget(new SlotWidget(inventory.storage, index++, x1, 4 + y * 18, true, true)
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

    private static final class StackHandler extends NotifiableItemStackHandler {

        boolean handling;

        private StackHandler(NeutronIrradiationPartMachine machine, int capacity) {
            super(machine, capacity, IO.IN, IO.BOTH, machine::createCustomHandler);
        }

        @Override
        public NeutronIrradiationPartMachine getMachine() {
            return (NeutronIrradiationPartMachine) super.getMachine();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
            handling = true;
            boolean changed = false;
            var size = storage.size;
            var ingredient = items.getFirst();
            if (ingredient.isEmpty()) {
                return true;
            }
            if (io == IO.IN) {
                for (int slot = 0; slot < size; ++slot) {
                    if (!getMachine().outputStacks[slot].isEmpty()) {
                        continue;
                    }
                    ItemStack stored = storage.stacks[slot];
                    int count = stored.getCount();
                    if (count == 0) continue;
                    if (ingredient.inner.test(stored)) {
                        changed = true;
                        ingredient.shrink(count);
                        if (!simulate) {
                            var output = recipe.itemOutputs;
                            if (!output.isEmpty()) {
                                getMachine().outputStacks[slot] = output.getFirst().inner.getInnerItemStack().copyWithCount(count);
                                getMachine().time[slot] = recipe.duration;
                                getMachine().initialTime[slot] = recipe.duration;
                                getMachine().fluxRequirements[slot] = (int) recipe.data.getFloat(GTORecipeDataKeys.NEUTRON_FLUX);
                                // getMachine().fluxRequirements[slot] =
                                // recipe.data.getFloat(GTORecipeDataKeys.NEUTRON_FLUX);
                            }
                        }
                        if (ingredient.amount <= 0) {
                            items.removeFirst();
                        }
                    }
                    break;
                }
            }
            handling = false;
            if (changed) storage.markAsChanged();
            return items.isEmpty();
        }

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
        }

        @Override
        public void fillSearchMap(GTRecipeType type, IntLongMap map) {
            for (int i = 0; i < storage.size; ++i) {
                if (!getMachine().outputStacks[i].isEmpty()) {
                    continue;
                }
                var stack = storage.stacks[i];
                var amount = stack.getCount();
                if (amount > 0) {
                    type.convertItem(stack, amount, map);
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
