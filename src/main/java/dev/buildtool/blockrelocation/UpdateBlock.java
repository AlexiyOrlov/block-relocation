package dev.buildtool.blockrelocation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateBlock {
    BlockPos pos;
    BlockState blockState;
    CompoundTag entityData;

    public UpdateBlock(BlockPos pos, BlockState blockState, CompoundTag tag) {
        this.pos = pos;
        this.blockState = blockState;
        entityData = tag;
    }
}
