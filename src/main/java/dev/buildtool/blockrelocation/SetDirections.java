package dev.buildtool.blockrelocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;

public class SetDirections {
    HashMap<Direction, Direction> directionHashMap;
    BlockPos platformPosition;

    public SetDirections(HashMap<Direction, Direction> directionHashMap, BlockPos platformPosition) {
        this.directionHashMap = directionHashMap;
        this.platformPosition = platformPosition;
    }
}
