package vazkii.botania.common;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import vazkii.botania.common.menu.FlowerPouchContainer;

/** 植物魔法复刻内容的界面类型注册。 */
public final class BotaniaMenus {

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister
            .create(ForgeRegistries.MENU_TYPES, "botania");

    public static final RegistryObject<MenuType<FlowerPouchContainer>> FLOWER_BAG = MENUS.register("flower_bag",
            () -> IForgeMenuType.create(FlowerPouchContainer::fromNetwork));

    private BotaniaMenus() {}

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
