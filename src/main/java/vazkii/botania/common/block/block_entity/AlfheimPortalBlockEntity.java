package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import vazkii.botania.common.BotaniaBlocks;
import vazkii.botania.common.alfheim.Alfheim;
import vazkii.botania.common.alfheim.AlfheimAccess;
import vazkii.botania.common.alfheim.AlfheimPortalHandler;
import vazkii.botania.common.alfheim.AlfheimTeleporter;
import vazkii.botania.common.block.ReplicaVisualBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

/**
 * 精灵门方块实体：校验 5×5×5 结构并统计周围自然水晶，满足条件时把门切换为激活状态。
 */
public class AlfheimPortalBlockEntity extends BotaniaBlockEntity {

    private static final int PYLON_RANGE = 6;
    private static final int MIN_PYLONS = 2;
    private static final long MANA_COST_OPENING = 200_000L;
    private static final long MANA_COST_PER_TICK = 500L;

    public int ticksOpen;

    public AlfheimPortalBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.ALF_PORTAL.get(), pos, state);
    }

    public AlfheimPortalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlfheimPortalBlockEntity self) {
        // 精灵门在亚尔夫海姆没有意义，与原模组一样直接关闭
        boolean active = !Alfheim.isAlfheim(level) && self.checkMultiblock() && self.countPylons() >= MIN_PYLONS;
        ReplicaVisualBlocks.PortalState wanted = active ? ReplicaVisualBlocks.PortalState.ON_X : ReplicaVisualBlocks.PortalState.OFF;
        if (state.getValue(ReplicaVisualBlocks.PORTAL_STATE) != wanted) {
            level.setBlock(pos, state.setValue(ReplicaVisualBlocks.PORTAL_STATE, wanted), 3);
        }
        if (active) {
            if (self.ticksOpen == 0 && !self.consumeMana(MANA_COST_OPENING)) {
                return;
            }
            if (self.ticksOpen > 0 && !self.consumeMana(MANA_COST_PER_TICK)) {
                self.ticksOpen = 0;
                return;
            }
            self.ticksOpen++;
            self.teleportPlayers(level);
        } else {
            self.ticksOpen = 0;
        }
    }

    private boolean isLog(BlockPos pos) {
        if (level == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(BotaniaBlocks.LIVINGWOOD_LOG.get()) || state.is(BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG.get());
    }

    private boolean isGlass(BlockPos pos) {
        return level.getBlockState(pos).is(BotaniaBlocks.ELF_GLASS.get());
    }

    /** 从周围魔力池按需扣除魔力，返回是否足够。 */
    private boolean consumeMana(long amount) {
        if (level == null) {
            return false;
        }
        long remaining = amount;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-PYLON_RANGE, -1, -PYLON_RANGE),
                worldPosition.offset(PYLON_RANGE, 2, PYLON_RANGE))) {
            if (remaining <= 0) {
                break;
            }
            if (level.getBlockEntity(pos) instanceof ManaPoolBlockEntity pool && pool.getCurrentManaLong() > 0) {
                remaining -= pool.removeMana(remaining, false);
            }
        }
        return remaining <= 0;
    }

    /**
     * 只把满足准入条件的玩家从主世界送进亚尔夫海姆。门内计时到点时给一次提示，
     * 玩家不满足条件就停在门里等下一次进出，不重复刷屏。
     */
    private void teleportPlayers(Level level) {
        AABB area = new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1,
                worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            if (!(player instanceof ServerPlayer serverPlayer) || !AlfheimPortalHandler.setInPortal(level, player)) {
                continue;
            }
            if (level.dimension() != Level.OVERWORLD) {
                serverPlayer.sendSystemMessage(Component.translatable("message.mythicbotany.alfheim_overworld_only"));
                continue;
            }
            if (!AlfheimAccess.canEnter(serverPlayer)) {
                serverPlayer.sendSystemMessage(Component.translatable("gtocore.entry_alfheim.unmet"));
                continue;
            }
            if (!AlfheimTeleporter.teleportToAlfheim(serverPlayer, worldPosition)) {
                serverPlayer.sendSystemMessage(Component.translatable("message.mythicbotany.alfheim_not_loaded"));
            }
        }
    }

    /** 按原模组的多方块形状校验：底部横梁、顶部横梁与中心玻璃、四角柱与角玻璃。 */
    public boolean checkMultiblock() {
        if (level == null) {
            return false;
        }
        BlockPos base = worldPosition;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!isLog(base.relative(dir))) {
                return false;
            }
            BlockPos top = base.relative(dir).above(4);
            if (!isLog(top)) {
                return false;
            }
        }
        if (!isGlass(base.above(4))) {
            return false;
        }
        for (int dx = -2; dx <= 2; dx += 4) {
            for (int dz = -2; dz <= 2; dz += 4) {
                for (int y = 1; y <= 3; y++) {
                    if (!isLog(base.offset(dx, y, dz))) {
                        return false;
                    }
                }
                if (!isGlass(base.offset(dx, 2, dz))) {
                    return false;
                }
            }
        }
        return true;
    }

    public int countPylons() {
        if (level == null) {
            return 0;
        }
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-PYLON_RANGE, -1, -PYLON_RANGE),
                worldPosition.offset(PYLON_RANGE, 2, PYLON_RANGE))) {
            if (level.getBlockState(pos).is(BotaniaBlocks.NATURA_PYLON.get())) {
                count++;
            }
        }
        return count;
    }
}
