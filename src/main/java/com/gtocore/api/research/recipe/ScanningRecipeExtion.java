package com.gtocore.api.research.recipe;

import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.common.item.DataCrystalItem;

import com.gtolib.api.annotation.DataGeneratorScanned;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

import com.fast.recipesearch.IntLongMap;
import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.gto.datasynclib.datastream.codec.DataCodec;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.util.DataCodecs;
import com.gto.datasynclib.util.StreamCodecs;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@DataGeneratorScanned
public class ScanningRecipeExtion extends RecipeExtension<ScanningRecipeExtion.AEKeyDataCrystal> {

    public static final ScanningRecipeExtion INSTANCE = new ScanningRecipeExtion("scanning_recipe");

    public ScanningRecipeExtion(String name) {
        super(name, GTOCodecs.AEKEYDATACRYSTAL_CODEC, false);
    }

    @Override
    public boolean handle(IO io, @NotNull IRecipeHandlerHolder holder, @Nullable RecipeHandlerUnit unit, @NotNull GTRecipe recipe, boolean simulate) {
        var aeKeyDataCrystal = recipe.data.getData(INSTANCE);
        if (aeKeyDataCrystal == null) {
            return false;
        }
        if (io == IO.OUT) {
            ItemStack dataCrystal = aeKeyDataCrystal.dataCystal().copy();
            var aeKeys = aeKeyDataCrystal.aeKeys();
            UUID team = aeKeyDataCrystal.team();
            for (var entry : aeKeys) {
                DataCrystalItem.addResearchData(dataCrystal, DataScanningManager.scanData(entry.getKey(), team, entry.getLongValue(), simulate));
            }
            if (simulate) {
                return unit == null ? holder.simulateOutputItem(dataCrystal) : unit.handleItem(io, List.of(new Content<>(ItemIngredient.of(dataCrystal), 1)), true);
            } else {
                if (unit == null) {
                    holder.outputItem(dataCrystal);
                } else {
                    unit.outputItem(dataCrystal);
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {}

    @Override
    public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
        return 0;
    }

    @Override
    public void setParallel(GTRecipe recipe, long parallel) {}

    @Override
    public void addInfo(GTRecipeDefinition recipe, WidgetGroup group, int xOffset, MutableInt yOffset) {}

    @Override
    public int getInfoHeight(GTRecipeDefinition recipe) {
        return 0;
    }

    public record AEKeyDataCrystal(KeyCounter aeKeys, ItemStack dataCystal, UUID team) {}

    public static AEKeyDataCrystal create(KeyCounter aeKeys, ItemStack dataCystal, UUID team) {
        return new AEKeyDataCrystal(aeKeys, dataCystal, team);
    }

    public static AEKeyDataCrystal create(AEKey aeKey, ItemStack dataCystal, UUID team) {
        var keyCounter = new KeyCounter();
        keyCounter.add(aeKey, 1);
        return new AEKeyDataCrystal(keyCounter, dataCystal, team);
    }

    public static final DataCodec<AEKeyDataCrystal> DATA_CODEc = new DataCodec<>() {

        @Override
        public AEKeyDataCrystal decode(@NotNull Data data, int dataVersion) {
            var d = data.asListData();
            var keyCounter = new KeyCounter();
            for (var entry : d.getList(0)) {
                var gs = GTOCodecs.GENERIC_STACK_DATA_CODEC.decode(entry, dataVersion);
                keyCounter.add(gs.what(), gs.amount());
            }
            var d2 = DataCodecs.ITEM_STACK_CODEC.decode(d.get(1), dataVersion);
            var team = DataCodec.UUID_CODEC.decode(d.get(2), dataVersion);
            return new AEKeyDataCrystal(keyCounter, d2, team);
        }

        @Override
        public @NotNull Data encode(AEKeyDataCrystal obj) {
            var aekeys = obj.aeKeys();
            var aeMapData = new ListData(aekeys.size());
            for (var entry : aekeys) {
                var tag = entry.getKey().toTagGeneric();
                tag.putLong("#", entry.getLongValue());
                aeMapData.add(DataCodecs.COMPOUND_TAG_CODEC.encode(tag));
            }
            var data = new ListData(3);
            data.add(aeMapData);
            data.add(DataCodecs.ITEM_STACK_CODEC.encode(obj.dataCystal()));
            data.add(DataCodec.UUID_CODEC.encode(obj.team()));
            return data;
        }
    };
    public static final ByteStreamCodec<AEKeyDataCrystal> DATA_STREAM_CODEc = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, AEKeyDataCrystal obj) {
            buf.writeInt(obj.aeKeys().size());
            for (var entry : obj.aeKeys()) {
                AEKey.writeKey(buf, entry.getKey());
                buf.writeVarLong(entry.getLongValue());
            }
            StreamCodecs.ITEM_STACK_CODEC.encode(buf, obj.dataCystal());
            ByteStreamCodec.UUID_CODEC.encode(buf, obj.team());
        }

        @Override
        public AEKeyDataCrystal decode(FriendlyByteBuf buf) {
            var size = buf.readInt();
            var keyCounter = new KeyCounter();
            for (int i = 0; i < size; i++) {
                var entry = GTOCodecs.GENERIC_STACK_STREAM_CODEC.decode(buf);
                keyCounter.add(entry.what(), entry.amount());
            }
            var d = StreamCodecs.ITEM_STACK_CODEC.decode(buf);
            var team = ByteStreamCodec.UUID_CODEC.decode(buf);
            return new AEKeyDataCrystal(keyCounter, d, team);
        }
    };
}
