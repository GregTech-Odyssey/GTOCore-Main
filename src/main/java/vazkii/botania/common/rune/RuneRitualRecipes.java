package vazkii.botania.common.rune;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import vazkii.botania.api.recipe.RuneRitualRecipe;

/**
 * 符文仪式的配方类型与序列化器注册，以及配方的特殊投入/产出。
 */
public final class RuneRitualRecipes {

    private static final String NAMESPACE = "mythicbotany";

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, NAMESPACE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, NAMESPACE);

    public static final RegistryObject<RecipeType<RuneRitualRecipe>> RUNE_RITUAL_TYPE = RECIPE_TYPES.register("rune_ritual", () -> new RecipeType<>() {

        @Override
        public String toString() {
            return "mythicbotany:rune_ritual";
        }
    });

    public static final RegistryObject<RecipeSerializer<RuneRitualRecipe>> RUNE_RITUAL_SERIALIZER = RECIPE_SERIALIZERS.register("rune_ritual", RuneRitualRecipe.Serializer::new);

    private RuneRitualRecipes() {}

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        RuneRitualRecipe.registerSpecialInput(WanderingTraderRuneInput.INSTANCE);
        RuneRitualRecipe.registerSpecialOutput(MjoellnirRuneOutput.INSTANCE);
    }
}
