package dev.buildtool.blockrelocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    public InteractionResult use(BlockState p_60503_, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (player.getItemInHand(hand).isEmpty()) {
            PlatformEntity platformEntity = (PlatformEntity) level.getBlockEntity(pos);
            Direction direction = blockHitResult.getDirection();
            platformEntity.toggleSide(direction, platformEntity.openStates);
            return InteractionResult.SUCCESS;
        }
        return super.use(p_60503_, level, pos, player, hand, blockHitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return BlockRelocation.PLATFORM_ENTITY.get().create(p_153215_, p_153216_);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity livingEntity, ItemStack p_49851_) {
        if (livingEntity != null && livingEntity.isShiftKeyDown()) {
            PlatformEntity platformEntity = (PlatformEntity) level.getBlockEntity(pos);
            if (livingEntity.getXRot() > 45) {
                platformEntity.toggleSide(Direction.DOWN, platformEntity.openStates);

            } else if (livingEntity.getXRot() < -45) {
                platformEntity.toggleSide(Direction.UP, platformEntity.openStates);
            } else {
                platformEntity.toggleSide(livingEntity.getDirection(), platformEntity.openStates);
            }
        }
    }
}
