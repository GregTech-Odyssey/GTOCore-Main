package com.gtocore.mixin.extrabotany;

import com.gregtechceu.gtceu.common.item.armor.ArmorSuiteFeatures;

import net.minecraft.world.entity.player.Player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * 盖亚守护者 III 的绿色缴械地雷：身上有纳米肌体 / 夸克高科 II 起的装备且电量足够时耗电解析，主手物品不被打落。
 * 地雷本身的伤害与负面效果不受影响（在父类 explode 中处理）。
 */
@Mixin(value = SkullLandMineEntity.Disarm.class, remap = false)
public class SkullLandMineDisarmMixin {

    @WrapOperation(method = "explode",
                   at = @At(value = "INVOKE",
                            target = "Lio/github/lounode/extrabotany/common/entity/SkullLandMineEntity$Disarm;getVictimPlayers()Ljava/util/List;"))
    private List<Player> gtocore$skipResistedPlayers(SkullLandMineEntity.Disarm self, Operation<List<Player>> original) {
        List<Player> players = original.call(self);
        ObjectArrayList<Player> victims = new ObjectArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            // 旁观与创造模式本来就被跳过，不为其扣电
            if (!player.isSpectator() && !player.isCreative() && ArmorSuiteFeatures.payGaiaMine(player)) continue;
            victims.add(player);
        }
        return victims;
    }
}
