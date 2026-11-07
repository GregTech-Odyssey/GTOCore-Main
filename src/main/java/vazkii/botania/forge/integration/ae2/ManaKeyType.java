package vazkii.botania.forge.integration.ae2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.api.stacks.AEKeyType;

public final class ManaKeyType extends AEKeyType<ManaKey> {

    public static final ResourceLocation ID = new net.minecraft.resources.ResourceLocation("botania", "mana");
    public static final ResourceLocation LEGACY_ID = new ResourceLocation("appbot", "mana");
    public static final String NAME_KEY = "botania.ae2.mana";
    public static final int AMOUNT_PER_BYTE = 500;
    public static final ManaKeyType TYPE = new ManaKeyType();

    private ManaKeyType() {
        super(ID, ManaKey.class, Component.translatable(NAME_KEY));
    }

    @Override
    public ManaKey readFromPacket(FriendlyByteBuf input) {
        return ManaKey.KEY;
    }

    @Override
    public ManaKey loadKeyFromTag(CompoundTag tag) {
        return ManaKey.KEY;
    }

    @Override
    public int getAmountPerOperation() {
        return AMOUNT_PER_BYTE;
    }

    @Override
    public int getAmountPerByte() {
        return AMOUNT_PER_BYTE;
    }
}
