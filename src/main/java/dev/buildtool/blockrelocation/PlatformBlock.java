package dev.buildtool.blockrelocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PlatformBlock extends Block implements EntityBlock {
    public PlatformBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_60508_) {
        if (player.getItemInHand(hand).isEmpty()) {
            PlatformEntity platformEntity = (PlatformEntity) p_60504_.getBlockEntity(pos);
            Direction direction = p_60508_.getDirection();
            platformEntity.openStates.put(direction, !platformEntity.openStates.get(direction));
            return InteractionResult.SUCCESS;
        }
        return super.use(p_60503_, p_60504_, pos, player, hand, p_60508_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return BlockRelocation.PLATFORM_ENTITY.get().create(p_153215_, p_153216_);
    }
}
