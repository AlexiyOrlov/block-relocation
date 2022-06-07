package dev.buildtool.blockrelocation.api;

import dev.buildtool.blockrelocation.RelocatorBlock;
import dev.buildtool.blockrelocation.RelocatorScreen;
import dev.buildtool.blockrelocation.SetDirections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Predicate;

/**
 * This interface represents block entity which performs relocation of blocks via {@link BlockGrabber} instances
 */
public interface BlockMover {
    /**
     * Perform movement. Called in {@link RelocatorBlock#tick(BlockState, ServerLevel, BlockPos, Random)}
     */
    void move(ServerLevel serverLevel, Direction moveFrom);

    /**
     * @return what blockstates to grab
     */
    Predicate<BlockState> grabCondition();

    /**
     * Called in {@link RelocatorBlock#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)}
     *
     * @param direction block movement direction
     */
    void setFromDirection(Direction direction);

    /**
     * Called in {@link RelocatorScreen}
     */
    Direction getToFrom(Direction from);

    /**
     * Called in {@link RelocatorScreen#onClose()}
     */
    HashMap<Direction, Direction> getMovementDirections();

    /**
     * Called in {@link RelocatorScreen}
     */
    void setFromTo(Direction from, Direction to);

    /**
     * Called in {@link RelocatorBlock#tick(BlockState, ServerLevel, BlockPos, Random)}
     *
     * @return block movement direction
     */
    Direction getFromDirection();

    /**
     * Is set from {@link SetDirections packet}
     */
    void setMovementDirections(HashMap<Direction, Direction> movementDirections);

    /**
     * Modified version of {@link Level#setBlock(BlockPos, BlockState, int, int)}
     */
    static boolean setBlock(Level level, BlockPos at, BlockState blockState, int flags, int limit) {
        if (level.isOutsideBuildHeight(at)) {
            return false;
        } else if (!level.isClientSide && level.isDebug()) {
            return false;
        } else {
            LevelChunk levelchunk = level.getChunkAt(at);

            at = at.immutable(); // Forge - prevent mutable BlockPos leaks
            net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
            if (level.captureBlockSnapshots && !level.isClientSide) {
                blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.create(level.dimension(), level, at, flags);
                level.capturedBlockSnapshots.add(blockSnapshot);
            }

            BlockState old = level.getBlockState(at);
            int oldLight = old.getLightEmission(level, at);
            int oldOpacity = old.getLightBlock(level, at);

            BlockState blockstate = setBlockState(levelchunk, at, blockState);
            if (blockstate == null) {
                if (blockSnapshot != null) level.capturedBlockSnapshots.remove(blockSnapshot);
                return false;
            } else {
                BlockState blockstate1 = level.getBlockState(at);
                if ((flags & 128) == 0 && blockstate1 != blockstate && (blockstate1.getLightBlock(level, at) != oldOpacity || blockstate1.getLightEmission(level, at) != oldLight || blockstate1.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion())) {
                    level.getProfiler().push("queueCheckLight");
                    level.getChunkSource().getLightEngine().checkBlock(at);
                    level.getProfiler().pop();
                }

                return true;
            }
        }
    }

    /**
     * Modified version of {@link LevelChunk#setBlockState(BlockPos, BlockState, boolean)}
     */
    @javax.annotation.Nullable
    static BlockState setBlockState(LevelChunk chunk, BlockPos at, BlockState blockState) {
        int i = at.getY();
        LevelChunkSection levelchunksection = chunk.getSection(chunk.getSectionIndex(i));
        boolean flag = levelchunksection.hasOnlyAir();
        if (flag && blockState.isAir()) {
            return null;
        } else {
            int j = at.getX() & 15;
            int k = i & 15;
            int l = at.getZ() & 15;
            BlockState blockstate = levelchunksection.setBlockState(j, k, l, blockState);
            if (blockstate == blockState) {
                return null;
            } else {
                Block block = blockState.getBlock();
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(j, i, l, blockState);
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(j, i, l, blockState);
                chunk.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(j, i, l, blockState);
                chunk.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(j, i, l, blockState);

                boolean flag2 = blockstate.hasBlockEntity();
                if (chunk.getLevel().isClientSide) {
                    if ((!blockstate.is(block) || !blockState.hasBlockEntity()) && flag2) {
                        chunk.removeBlockEntity(at);
                    }
                }

                if (!levelchunksection.getBlockState(j, k, l).is(block)) {
                    return null;
                } else {
                    if (blockState.hasBlockEntity()) {
                        BlockEntity blockentity = chunk.getBlockEntity(at, LevelChunk.EntityCreationType.CHECK);
                        if (blockentity == null) {
                            blockentity = ((EntityBlock) block).newBlockEntity(at, blockState);
                            if (blockentity != null) {
                                chunk.addAndRegisterBlockEntity(blockentity);
                            }
                        } else {
                            blockentity.setBlockState(blockState);
                            chunk.updateBlockEntityTicker(blockentity);
                        }
                    }

                    chunk.setUnsaved(true);
                    return blockstate;
                }
            }
        }
    }
}
