package com.gtocore.integration.ftbquests;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.datastream.data.Data;
import dev.architectury.fluid.FluidStack;
import dev.emi.emi.api.EmiApi;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import lombok.Setter;

public class TechNodeTask extends AbstractBooleanTask {

    static TaskType TECHNODE;
    @Setter
    private TechNode node;

    public TechNodeTask(long id, Quest quest) {
        super(id, quest);
        node = ComponentNodes.ComponentInAssemblyLineluv;
    }

    public TaskType getType() {
        return TECHNODE;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putByteArray("node", GTOCodecs.TECH_NODE_DATA_CODEC.encode(node).writeToBytes());
        // GTOCodecs.TECH_NODE_DATA_CODEC.toCodec(0).encodeStart(NbtOps.INSTANCE, node).result().ifPresent((tag) ->
        // nbt.put("node", tag));
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        if (nbt.contains("node")) {
            node = GTOCodecs.TECH_NODE_DATA_CODEC.decode(Data.readData(nbt.getByteArray("node")));
        }
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        GTOCodecs.TECH_NODE_STREAM_CODEC.encode(buffer, node);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        node = GTOCodecs.TECH_NODE_STREAM_CODEC.decode(buffer);
    }

    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        var manager = node.getManager();
        config.addEnum("structure", node, this::setNode,
                NameMap.of(ComponentNodes.ComponentInAssemblyLineluv, manager.getAllNodes().toArray(new TechNode[0])).create());
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return Component.translatable("gtocore.research.tech_node", node.getDisplayName().withStyle(style -> style.withColor(ChatFormatting.AQUA)));
    }

    public int autoSubmitOnPlayerTick() {
        return 20;
    }

    public boolean checkOnLogin() {
        return false;
    }

    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        return TechTreeSavedData.isUnlocked(player, node);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawGUI(TeamData teamData, GuiGraphics graphics, int x, int y, int w, int h) {
        new TechNodeEmiStack(node).render(graphics, x, y, 0, TechNodeEmiStack.RENDER_ICON);
    }

    @Override
    public Icon getAltIcon() {
        return switch (node.icon) {
            case AEItemKey itemIcon -> ItemIcon.getItemIcon(itemIcon.item);
            case AEFluidKey itemIcon -> {
                var f = FluidStack.create(itemIcon.fluid, 1000);
                yield Icon.getIcon(ClientUtils.getStillTexture(f)).withTint(Color4I.rgb(ClientUtils.getFluidColor(f)));
            }
            case null, default -> super.getAltIcon();
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onButtonClicked(Button button, boolean canClick) {
        button.playClickSound();

        EmiApi.displayUses(new TechNodeEmiStack(node));
    }
}
