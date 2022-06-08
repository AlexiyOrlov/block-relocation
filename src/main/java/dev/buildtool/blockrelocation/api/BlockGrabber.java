package dev.buildtool.blockrelocation.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This interface represents a block entity which can "grab" adjacent blocks
 * and can be grabbed by other BlockGrabbers.
 */
public interface BlockGrabber {
    /**
     * @return true = open, false = closed
     */
    boolean isSideOpen(Direction side);

    /**
     * Toggles side state
     *
     * @param on      side clicked
     * @param sideMap state map
     */
    default void toggleSide(Direction on, HashMap<Direction, Boolean> sideMap) {
        sideMap.put(on, !sideMap.get(on));
    }

    /**
     * Save side states to the NBT
     */
    default void saveToTag(CompoundTag compoundTag, HashMap<Direction, Boolean> sideConfig) {
        sideConfig.forEach((direction, aBoolean) -> compoundTag.putBoolean(direction.getName(), aBoolean));
    }

    /**
     * Load side states from NBT
     */
    default void loadFromTag(CompoundTag compoundTag, HashMap<Direction, Boolean> sideMap) {
        for (Direction direction : Direction.values()) {
            sideMap.put(direction, compoundTag.getBoolean(direction.getName()));
        }
    }

    static HashSet<BlockPos> getConnectedObjects(Class<?> of, Direction[] checkedSides, BlockPos pos, Level world, HashSet<BlockPos> positions, int limit) {
        assert limit > 1 : "Limit must be >1";
        if (world.getBlockEntity(pos) != null && of.isInstance(world.getBlockEntity(pos))) {
            positions.add(pos);
        }
        if (positions.size() >= limit) {
            return positions;
        }
        for (Direction checkedSide : checkedSides) {
            BlockPos side = pos.relative(checkedSide);
            BlockEntity next = world.getBlockEntity(side);

            if (of.isInstance(next)) {
                if (!positions.contains(side)) {
                    getConnectedObjects(of, ArrayUtils.removeElement(Direction.values(), checkedSide.getOpposite()), side, world, positions, limit);
                } else {
                    positions.add(side);
                    if (positions.size() >= limit) {
                        return positions;
                    }
                }
            }
        }

        return positions;
    }
}
