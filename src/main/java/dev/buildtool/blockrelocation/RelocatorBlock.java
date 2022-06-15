package dev.buildtool.blockrelocation;

import dev.buildtool.blockrelocation.api.BlockMover;
import dev.buildtool.satako.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RelocatorBlock extends Block implements EntityBlock {
    public RelocatorBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level level, BlockPos pos, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {
        if (level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockMover)
                openScreen((BlockMover) blockEntity, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen(BlockMover relocator, BlockPos itsPosition) {
        Minecraft.getInstance().setScreen(new RelocatorScreen(new TranslatableComponent("block_relocation.relocator"), relocator, itsPosition));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos position, Block neighbor, BlockPos neighborPosition, boolean notify) {
        super.neighborChanged(state, level, position, neighbor, neighborPosition, notify);
        Direction direction = Functions.getPowerIncomingDirection(neighborPosition, position, neighbor, level);
        if (direction != null) {
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity instanceof BlockMover blockMover) {
                blockMover.setFromDirection(direction);
                level.scheduleTick(position, this, 1);
            }
        }
    }

    @Override
    public void tick(BlockState p_60462_, ServerLevel level, BlockPos pos, Random p_60465_) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BlockMover blockMover) {
            blockMover.move(level, blockMover.getFromDirection());
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return BlockRelocation.RELOCATOR_ENTITY.get().create(p_153215_, p_153216_);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }
}
