package vazkii.botania.api.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.rune.RuneRitualRecipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 神话植物学的符文仪式配方。
 *
 * <p>
 * 中心符文放在大师符文支托上，周围符文按坐标摆放在符文支托上，投入物品丢在仪式范围内。
 * 配方记录耗魔总量与时长，仪式按每 tick 的消耗量从大师符文支托的内部魔力中扣除，魔力不足时暂停。
 * </p>
 */
public class RuneRitualRecipe implements Recipe<Container> {

    public static final ResourceLocation TYPE_ID = new ResourceLocation("mythicbotany", "rune_ritual");

    private static final Map<ResourceLocation, SpecialRuneInput> SPECIAL_INPUTS = new HashMap<>();
    private static final Map<ResourceLocation, SpecialRuneOutput> SPECIAL_OUTPUTS = new HashMap<>();

    public static void registerSpecialInput(SpecialRuneInput input) {
        if (SPECIAL_INPUTS.putIfAbsent(input.id, input) != null) {
            throw new IllegalStateException("Duplicate special rune ritual input: " + input.id);
        }
    }

    public static void registerSpecialOutput(SpecialRuneOutput output) {
        if (SPECIAL_OUTPUTS.putIfAbsent(output.id, output) != null) {
            throw new IllegalStateException("Duplicate special rune ritual output: " + output.id);
        }
    }

    @Nullable
    static SpecialRuneInput specialInput(ResourceLocation id) {
        return SPECIAL_INPUTS.get(id);
    }

    @Nullable
    static SpecialRuneOutput specialOutput(ResourceLocation id) {
        return SPECIAL_OUTPUTS.get(id);
    }

    private final ResourceLocation id;
    private final String group;
    private final Ingredient centerRune;
    private final List<RunePosition> runes;
    private final int mana;
    private final int ticks;
    private final List<Ingredient> inputs;
    private final List<ItemStack> outputs;
    @Nullable
    private final SpecialRuneInput specialInput;
    @Nullable
    private final SpecialRuneOutput specialOutput;

    public RuneRitualRecipe(ResourceLocation id, String group, Ingredient centerRune, List<RunePosition> runes,
                            int mana, int ticks, List<Ingredient> inputs, List<ItemStack> outputs,
                            @Nullable SpecialRuneInput specialInput, @Nullable SpecialRuneOutput specialOutput) {
        this.id = id;
        this.group = group;
        this.centerRune = centerRune;
        this.runes = ImmutableList.copyOf(runes);
        this.mana = Math.max(0, mana);
        this.ticks = Math.max(1, ticks);
        this.inputs = ImmutableList.copyOf(inputs);
        this.outputs = ImmutableList.copyOf(outputs);
        this.specialInput = specialInput;
        this.specialOutput = specialOutput;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RuneRitualRecipes.RUNE_RITUAL_TYPE.get();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RuneRitualRecipes.RUNE_RITUAL_SERIALIZER.get();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull String getGroup() {
        return group;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return outputs.size() == 1 ? outputs.get(0) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public Ingredient getCenterRune() {
        return centerRune;
    }

    public List<RunePosition> getRunes() {
        return runes;
    }

    public int getMana() {
        return mana;
    }

    public int getTicks() {
        return ticks;
    }

    /** 每 tick 的魔力消耗量，按总耗魔均摊到整个仪式时长。 */
    public int getManaPerTick() {
        return mana <= 0 ? 0 : Math.max(1, (int) ((mana + (long) ticks - 1) / ticks));
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    @Nullable
    public SpecialRuneInput getSpecialInput() {
        return specialInput;
    }

    @Nullable
    public SpecialRuneOutput getSpecialOutput() {
        return specialOutput;
    }

    /**
     * 一个符文在阵型里的位置。八个变换覆盖水平镜像、垂直镜像与 90 度旋转，配方只需写四分之一。
     */
    public static class RunePosition {

        private static final int HFLIP = 1;
        private static final int VFLIP = 1 << 1;
        private static final int ROTATE = 1 << 2;

        private final Ingredient rune;
        private final int x;
        private final int z;
        private final int[] xcoords = new int[8];
        private final int[] zcoords = new int[8];
        private final boolean consume;

        public RunePosition(Ingredient rune, int x, int z, boolean consume) {
            this.rune = rune;
            this.x = x;
            this.z = z;
            this.consume = (x == 0 && z == 0) || consume;
            xcoords[0] = x;
            zcoords[0] = z;
            xcoords[HFLIP] = -x;
            zcoords[HFLIP] = z;
            xcoords[VFLIP] = x;
            zcoords[VFLIP] = -z;
            xcoords[HFLIP | VFLIP] = -x;
            zcoords[HFLIP | VFLIP] = -z;
            for (int i = 0; i < (HFLIP | VFLIP); i++) {
                xcoords[i | ROTATE] = -zcoords[i];
                zcoords[i | ROTATE] = xcoords[i];
            }
        }

        public Ingredient getRune() {
            return rune;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public boolean isConsumed() {
            return consume;
        }

        public int getX(int transformIdx) {
            return transformIdx < 0 || transformIdx >= 8 ? x : xcoords[transformIdx];
        }

        public int getZ(int transformIdx) {
            return transformIdx < 0 || transformIdx >= 8 ? z : zcoords[transformIdx];
        }
    }

    public static class Serializer implements RecipeSerializer<RuneRitualRecipe> {

        @Override
        public @NotNull RuneRitualRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient centerRune = Ingredient.fromJson(json.get("center"));
            String group = GsonHelper.getAsString(json, "group", "");
            List<RunePosition> runes = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "runes")) {
                JsonObject runeJson = element.getAsJsonObject();
                Ingredient rune = Ingredient.fromJson(runeJson.get("rune"));
                int x = GsonHelper.getAsInt(runeJson, "x");
                int z = GsonHelper.getAsInt(runeJson, "z");
                boolean consume = GsonHelper.getAsBoolean(runeJson, "consume", false);
                runes.add(new RunePosition(rune, x, z, consume));
            }

            int mana = GsonHelper.getAsInt(json, "mana", 0);
            int ticks = GsonHelper.getAsInt(json, "ticks", 200);

            List<Ingredient> inputs = new ArrayList<>();
            if (json.has("inputs")) {
                for (JsonElement element : json.getAsJsonArray("inputs")) {
                    inputs.add(Ingredient.fromJson(element));
                }
            }

            List<ItemStack> outputs = new ArrayList<>();
            if (json.has("outputs")) {
                for (JsonElement element : json.getAsJsonArray("outputs")) {
                    outputs.add(CraftingHelper.getItemStack(element.getAsJsonObject(), true));
                }
            }

            return new RuneRitualRecipe(recipeId, group, centerRune, runes, mana, ticks, inputs, outputs,
                    readSpecial(json, "special_input", SPECIAL_INPUTS, "special rune ritual input"),
                    readSpecial(json, "special_output", SPECIAL_OUTPUTS, "special rune ritual output"));
        }

        @Nullable
        private static <T> T readSpecial(JsonObject json, String key, Map<ResourceLocation, T> registry, String what) {
            if (!json.has(key) || json.get(key).isJsonNull()) {
                return null;
            }
            ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, key));
            T value = registry.get(id);
            if (value == null) {
                throw new IllegalStateException("Unknown " + what + ": " + id);
            }
            return value;
        }

        @Override
        public @Nullable RuneRitualRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            Ingredient centerRune = Ingredient.fromNetwork(buffer);
            String group = buffer.readUtf();
            int size = buffer.readVarInt();
            List<RunePosition> runes = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Ingredient rune = Ingredient.fromNetwork(buffer);
                int x = buffer.readVarInt();
                int z = buffer.readVarInt();
                boolean consume = buffer.readBoolean();
                runes.add(new RunePosition(rune, x, z, consume));
            }

            int mana = buffer.readVarInt();
            int ticks = buffer.readVarInt();

            size = buffer.readVarInt();
            List<Ingredient> inputs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                inputs.add(Ingredient.fromNetwork(buffer));
            }

            size = buffer.readVarInt();
            List<ItemStack> outputs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                outputs.add(buffer.readItem());
            }

            return new RuneRitualRecipe(recipeId, group, centerRune, runes, mana, ticks, inputs, outputs,
                    readSpecial(buffer, SPECIAL_INPUTS), readSpecial(buffer, SPECIAL_OUTPUTS));
        }

        @Nullable
        private static <T> T readSpecial(FriendlyByteBuf buffer, Map<ResourceLocation, T> registry) {
            if (!buffer.readBoolean()) {
                return null;
            }
            ResourceLocation id = buffer.readResourceLocation();
            T value = registry.get(id);
            if (value == null) {
                throw new IllegalStateException("Unknown special rune ritual action: " + id);
            }
            return value;
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RuneRitualRecipe recipe) {
            recipe.centerRune.toNetwork(buffer);
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.runes.size());
            for (RunePosition rune : recipe.runes) {
                rune.getRune().toNetwork(buffer);
                buffer.writeVarInt(rune.getX());
                buffer.writeVarInt(rune.getZ());
                buffer.writeBoolean(rune.isConsumed());
            }

            buffer.writeVarInt(recipe.mana);
            buffer.writeVarInt(recipe.ticks);

            buffer.writeVarInt(recipe.inputs.size());
            for (Ingredient input : recipe.inputs) {
                input.toNetwork(buffer);
            }

            buffer.writeVarInt(recipe.outputs.size());
            for (ItemStack output : recipe.outputs) {
                buffer.writeItem(output);
            }

            writeSpecial(buffer, recipe.specialInput);
            writeSpecial(buffer, recipe.specialOutput);
        }

        private static <T> void writeSpecial(FriendlyByteBuf buffer, @Nullable T action) {
            if (action == null) {
                buffer.writeBoolean(false);
                return;
            }
            buffer.writeBoolean(true);
            if (action instanceof SpecialRuneInput input) {
                buffer.writeResourceLocation(input.id);
            } else if (action instanceof SpecialRuneOutput output) {
                buffer.writeResourceLocation(output.id);
            }
        }
    }
}
