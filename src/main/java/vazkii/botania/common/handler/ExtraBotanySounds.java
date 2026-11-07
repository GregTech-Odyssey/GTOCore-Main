package vazkii.botania.common.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 额外植物学复刻的音效事件。 */
public final class ExtraBotanySounds {

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister
            .create(ForgeRegistries.SOUND_EVENTS, "extrabotany");

    private ExtraBotanySounds() {}

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private static RegistryObject<SoundEvent> make(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("extrabotany", id)));
    }

    public static final RegistryObject<SoundEvent> CAMERA_USE = make("item.camera.use");
    public static final RegistryObject<SoundEvent> CAMERA_CHARGE = make("item.camera.charge");
    public static final RegistryObject<SoundEvent> CAMERA_FOCUS = make("item.camera.focus");
    public static final RegistryObject<SoundEvent> EXCALIBUR_ATTACK = make("item.excalibur.attack");
    public static final RegistryObject<SoundEvent> FAILNAUGHT_SHOOT = make("item.failnaught.shoot");
    public static final RegistryObject<SoundEvent> FEATHER_OF_JINGWEI_SHOOT = make("item.feather_of_jingwei.shoot");
    public static final RegistryObject<SoundEvent> PLAYER_BACKFIRE = make("entity.player.hurt_backfire");
    public static final RegistryObject<SoundEvent> REWARD_BAG_OPEN = make("item.reward_bag.open");
    public static final RegistryObject<SoundEvent> PANDORAS_BOX_OPEN = make("item.pandoras_box.open");
    public static final RegistryObject<SoundEvent> WALKING_CANE_USE = make("item.walking_cane.use");
    public static final RegistryObject<SoundEvent> MUSIC_GAIA3 = make("music.gaia3");
    public static final RegistryObject<SoundEvent> ARMOR_EQUIP_MAID = make("item.armor.equip_maid");
    public static final RegistryObject<SoundEvent> ARMOR_EQUIP_IDOL = make("item.armor.equip_idol");
    public static final RegistryObject<SoundEvent> HAMMER_USE = make("item.hammer.use");
    public static final RegistryObject<SoundEvent> ARMOR_EQUIP_GOBLIN = make("item.armor.equip_goblin");
    public static final RegistryObject<SoundEvent> ARMOR_EQUIP_WARRIOR = make("item.armor.equip_warrior");
    public static final RegistryObject<SoundEvent> BELL_FLOWER_RING = make("block.bellflower.ring");
}
