package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

/** 实心藤：外形与藤蔓一致，但有碰撞、不随机刻、不可替换。 */
public class SolidVineBlock extends VineBlock {

    public SolidVineBlock(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ItemStack getCloneItemStack(@NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull BlockState state) {
        return new ItemStack(Blocks.VINE);
    }
}
