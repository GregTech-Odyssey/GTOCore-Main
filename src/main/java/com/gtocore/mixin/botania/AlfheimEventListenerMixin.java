package com.gtocore.mixin.botania;

// TODO 恢复维度及访问条件
// @Mixin(mythicbotany.EventListener.class)
public abstract class AlfheimEventListenerMixin {

    // /**
    // * @author .
    // * @reason .
    // */
    // @Overwrite(remap = false)
    // @SubscribeEvent
    // public void alfPortalUpdate(ElvenPortalUpdateEvent event) {
    // BlockEntity portal = event.getPortalTile();
    // if (!event.isOpen() || portal.getLevel() == null || portal.getLevel().isClientSide) {
    // return;
    // }
    // Level level = portal.getLevel();
    // Dimension dimension = Dimension.from(level);
    // if (dimension == Dimension.ALFHEIM) {
    // if (portal instanceof AlfheimPortalBlockEntity alfPortal) {
    // alfPortal.consumeMana(new ArrayList<>(), 0, true);
    // }
    // return;
    // }
    // if (!AlfheimPortalHandler.shouldCheck(level)) {
    // return;
    // }
    // List<Player> playersInPortal = level.getEntitiesOfClass(Player.class, event.getAabb());
    // if (playersInPortal.isEmpty()) {
    // return;
    // }
    // BlockPos portalPos = portal.getBlockPos();
    // for (Player player : playersInPortal) {
    // if (player instanceof ServerPlayer serverPlayer && AlfheimHelper.gtocore$canPlayerUsePortal(serverPlayer)) {
    // if (AlfheimPortalHandler.setInPortal(serverPlayer.level(), serverPlayer)) {
    // if (dimension == Dimension.OVERWORLD) {
    // if (!AlfheimTeleporter.teleportToAlfheim(serverPlayer, portalPos)) {
    // serverPlayer.sendSystemMessage(Component.translatable("message.mythicbotany.alfheim_not_loaded"));
    // }
    // } else {
    // serverPlayer.sendSystemMessage(Component.translatable("message.mythicbotany.alfheim_overworld_only"));
    // }
    //
    // }
    // }
    // }
    // }
    //
    // /**
    // * @author .
    // * @reason .
    // */
    // @Overwrite(remap = false)
    // @SubscribeEvent
    // public void playerTick(TickEvent.PlayerTickEvent event) {
    // Player player = event.player;
    // if (player.tickCount % 20 == 1 && player instanceof ServerPlayer s && Alfheim.DIMENSION ==
    // player.level().dimension() && !player.isCreative()) {
    // if (MythicConfig.lockAlfheim && !AlfheimHelper.gtocore$canPlayerUsePortal(s))
    // player.kill();
    // }
    // }
}
