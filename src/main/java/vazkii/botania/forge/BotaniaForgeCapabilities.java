package vazkii.botania.forge;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import vazkii.botania.api.mana.ManaReceiver;

/**
 * 复刻内容的 Forge 能力接线。能力常量只放在 {@link vazkii.botania.api.BotaniaForgeCapabilities}，
 * 这里只负责注册与挂载。
 */
public final class BotaniaForgeCapabilities {

    public static final ResourceLocation MANA_RECEIVER_ID = new ResourceLocation("botania", "mana_receiver");

    private BotaniaForgeCapabilities() {}

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.register(ManaReceiver.class));
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, BotaniaForgeCapabilities::attach);
    }

    private static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof ManaReceiver receiver) {
            event.addCapability(MANA_RECEIVER_ID, new Provider(receiver));
        }
    }

    /** 与植物魔法原实现一致：能力实例只建一次，避免每次查询都分配 {@link LazyOptional}。 */
    private static final class Provider implements ICapabilityProvider {

        private final LazyOptional<ManaReceiver> instance;

        private Provider(ManaReceiver receiver) {
            this.instance = LazyOptional.of(() -> receiver);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return capability == vazkii.botania.api.BotaniaForgeCapabilities.MANA_RECEIVER ? instance.cast() : LazyOptional.empty();
        }
    }
}
