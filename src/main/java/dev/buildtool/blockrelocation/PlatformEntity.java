package dev.buildtool.blockrelocation;

import dev.buildtool.blockrelocation.api.BlockGrabber;
import dev.buildtool.satako.BlockEntity2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class PlatformEntity extends BlockEntity2 implements BlockGrabber {
    /**
     * true = open, false = closed
     */
    HashMap<Direction, Boolean> openStates = new HashMap<>(6, 1);

    public PlatformEntity(BlockPos position, BlockState blockState) {
        super(BlockRelocation.PLATFORM_ENTITY.get(), position, blockState);
        openStates.put(Direction.UP, true);
        openStates.put(Direction.EAST, true);
        openStates.put(Direction.WEST, true);
        openStates.put(Direction.SOUTH, true);
        openStates.put(Direction.NORTH, true);
        openStates.put(Direction.DOWN, true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveToTag(tag, openStates);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadFromTag(tag, openStates);
    }

    @Override
    public boolean isSideOpen(Direction side) {
        return openStates.get(side);
    }
}
