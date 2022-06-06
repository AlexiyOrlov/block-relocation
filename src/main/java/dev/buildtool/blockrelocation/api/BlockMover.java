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
     * Modified version of {@link Level#setBlock(BlockPos, BlockState, int, int)}
     */
    static boolean setBlock(Level level, BlockPos p_46605_, BlockState p_46606_, int p_46607_, int p_46608_) {
        if (level.isOutsideBuildHeight(p_46605_)) {
            return false;
        } else if (!level.isClientSide && level.isDebug()) {
            return false;
        } else {
            LevelChunk levelchunk = level.getChunkAt(p_46605_);

            p_46605_ = p_46605_.immutable(); // Forge - prevent mutable BlockPos leaks
            net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
            if (level.captureBlockSnapshots && !level.isClientSide) {
                blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.create(level.dimension(), level, p_46605_, p_46607_);
                level.capturedBlockSnapshots.add(blockSnapshot);
            }

            BlockState old = level.getBlockState(p_46605_);
            int oldLight = old.getLightEmission(level, p_46605_);
            int oldOpacity = old.getLightBlock(level, p_46605_);

            BlockState blockstate = setBlockState(levelchunk, p_46605_, p_46606_, (p_46607_ & 64) != 0);
            if (blockstate == null) {
                if (blockSnapshot != null) level.capturedBlockSnapshots.remove(blockSnapshot);
                return false;
            } else {
                BlockState blockstate1 = level.getBlockState(p_46605_);
                if ((p_46607_ & 128) == 0 && blockstate1 != blockstate && (blockstate1.getLightBlock(level, p_46605_) != oldOpacity || blockstate1.getLightEmission(level, p_46605_) != oldLight || blockstate1.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion())) {
                    level.getProfiler().push("queueCheckLight");
                    level.getChunkSource().getLightEngine().checkBlock(p_46605_);
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
    static BlockState setBlockState(LevelChunk chunk, BlockPos p_62865_, BlockState p_62866_, boolean p_62867_) {
        int i = p_62865_.getY();
        LevelChunkSection levelchunksection = chunk.getSection(chunk.getSectionIndex(i));
        boolean flag = levelchunksection.hasOnlyAir();
        if (flag && p_62866_.isAir()) {
            return null;
        } else {
            int j = p_62865_.getX() & 15;
            int k = i & 15;
            int l = p_62865_.getZ() & 15;
            BlockState blockstate = levelchunksection.setBlockState(j, k, l, p_62866_);
            if (blockstate == p_62866_) {
                return null;
            } else {
                Block block = p_62866_.getBlock();
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(j, i, l, p_62866_);
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(j, i, l, p_62866_);
                chunk.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(j, i, l, p_62866_);
                chunk.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(j, i, l, p_62866_);

                boolean flag2 = blockstate.hasBlockEntity();
                if (chunk.getLevel().isClientSide) {
                    if ((!blockstate.is(block) || !p_62866_.hasBlockEntity()) && flag2) {
                        chunk.removeBlockEntity(p_62865_);
                    }
                }

                if (!levelchunksection.getBlockState(j, k, l).is(block)) {
                    return null;
                } else {
                    if (p_62866_.hasBlockEntity()) {
                        BlockEntity blockentity = chunk.getBlockEntity(p_62865_, LevelChunk.EntityCreationType.CHECK);
                        if (blockentity == null) {
                            blockentity = ((EntityBlock) block).newBlockEntity(p_62865_, p_62866_);
                            if (blockentity != null) {
                                chunk.addAndRegisterBlockEntity(blockentity);
                            }
                        } else {
                            blockentity.setBlockState(p_62866_);
                            chunk.updateBlockEntityTicker(blockentity);
                        }
                    }

                    chunk.setUnsaved(true);
                    return blockstate;
                }
            }
        }
    }

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
}
