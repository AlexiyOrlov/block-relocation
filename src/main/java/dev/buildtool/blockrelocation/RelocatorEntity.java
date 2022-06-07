package dev.buildtool.blockrelocation;

import dev.buildtool.blockrelocation.api.BlockGrabber;
import dev.buildtool.blockrelocation.api.BlockMover;
import dev.buildtool.satako.BlockEntity2;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

public class RelocatorEntity extends BlockEntity2 implements BlockMover {

    HashMap<Direction, Direction> movementDirections = new HashMap<>(6, 1);
    Direction moveFrom;

    public RelocatorEntity(BlockPos position, BlockState blockState) {
        super(BlockRelocation.RELOCATOR_ENTITY.get(), position, blockState);
        movementDirections.put(Direction.UP, Direction.DOWN);
        movementDirections.put(Direction.DOWN, Direction.UP);
        movementDirections.put(Direction.EAST, Direction.WEST);
        movementDirections.put(Direction.WEST, Direction.EAST);
        movementDirections.put(Direction.NORTH, Direction.SOUTH);
        movementDirections.put(Direction.SOUTH, Direction.NORTH);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("From up", (byte) movementDirections.get(Direction.UP).ordinal());
        tag.putByte("From down", (byte) movementDirections.get(Direction.DOWN).ordinal());
        tag.putByte("From east", (byte) movementDirections.get(Direction.EAST).ordinal());
        tag.putByte("From west", (byte) movementDirections.get(Direction.WEST).ordinal());
        tag.putByte("From north", (byte) movementDirections.get(Direction.NORTH).ordinal());
        tag.putByte("From south", (byte) movementDirections.get(Direction.SOUTH).ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("From up")) {
            movementDirections.put(Direction.UP, Direction.values()[tag.getByte("From up")]);
            movementDirections.put(Direction.DOWN, Direction.values()[tag.getByte("From down")]);
            movementDirections.put(Direction.EAST, Direction.values()[tag.getByte("From east")]);
            movementDirections.put(Direction.WEST, Direction.values()[tag.getByte("From west")]);
            movementDirections.put(Direction.NORTH, Direction.values()[tag.getByte("From north")]);
            movementDirections.put(Direction.SOUTH, Direction.values()[tag.getByte("From south")]);
        }
    }

    @Override
    public void move(ServerLevel serverLevel, Direction moveFrom) {
        LevelTicks<Block> blockLevelTicks = serverLevel.getBlockTicks();
        Direction moveTo = movementDirections.get(moveFrom);
        HashSet<BlockPos> connectedPlatforms = BlockGrabber.getConnectedObjects(BlockGrabber.class, Direction.values(), getBlockPos(), serverLevel, new HashSet<>(BlockRelocation.GRABBER_BATCH_LIMIT.get()), BlockRelocation.GRABBER_BATCH_LIMIT.get());
        HashSet<BlockPos> grabbedPositions = new HashSet<>(connectedPlatforms.size());
        connectedPlatforms.forEach(blockPos -> {
            BlockEntity blockEntity2 = serverLevel.getBlockEntity(blockPos);
            if (blockEntity2 instanceof BlockGrabber blockGrabber) {
                for (Direction direction : Direction.values()) {
                    BlockPos sideOfPlatform = blockPos.relative(direction);
                    if (blockGrabber.isSideOpen(direction)) {
                        BlockState sideBlock = serverLevel.getBlockState(sideOfPlatform);
                        if (grabCondition().test(sideBlock)) {
                            grabbedPositions.add(sideOfPlatform);
                        }
                    }
                }
            }
        });
        if (moveTo != null) {
            HashMap<BlockPos, BlockState> blockers = new HashMap<>();
            grabbedPositions.forEach(blockPos -> {
                BlockPos posAhead = blockPos.relative(moveTo);
                BlockState blockAhead = serverLevel.getBlockState(posAhead);
                if (grabCondition().test(blockAhead) && !grabbedPositions.contains(posAhead)) {
                    blockers.put(posAhead, blockAhead);
                }
            });


            if (blockers.isEmpty()) {
                HashSet<BlockSnapshot> blockSnapshots = new HashSet<>(grabbedPositions.size());
                HashSet<BlockPos> checkedPositions = new HashSet<>(grabbedPositions.size());
                HashSet<Entity> entities = new HashSet<>();
                grabbedPositions.forEach(blockPos -> {
                    BlockEntity blockEntity2 = serverLevel.getBlockEntity(blockPos);
                    if (blockEntity2 instanceof BlockGrabber blockGrabber) {
                        for (Direction direction : Direction.values()) {
                            if (blockGrabber.isSideOpen(direction)) {
                                BlockPos sideOfPlatform = blockPos.relative(direction);
                                BlockState sideBlock = serverLevel.getBlockState(sideOfPlatform);
                                if (grabCondition().test(sideBlock)) {
                                    if (!checkedPositions.contains(sideOfPlatform)) {
                                        checkedPositions.add(sideOfPlatform);
                                        entities.addAll(serverLevel.getEntitiesOfClass(Entity.class, new AABB(sideOfPlatform.above()), entity -> true));
                                        blockSnapshots.add(BlockSnapshot.create(serverLevel.dimension(), serverLevel, sideOfPlatform));
                                    }
                                }
                            }
                        }
                    }
                });
                Long2ObjectMap<LevelChunkTicks<Block>> allContainers = blockLevelTicks.allContainers;
                HashSet<ScheduledTick<Block>> hashSet = new HashSet<>();
                blockSnapshots.forEach(blockSnapshot -> {
                    BlockPos blockSnapshotPos = blockSnapshot.getPos();
                    serverLevel.removeBlockEntity(blockSnapshotPos);
                    if (BlockMover.setBlockSilently(serverLevel, blockSnapshotPos, Blocks.AIR.defaultBlockState(), 2, 512)) {
                        BlockRelocation.CHANNEL.send(PacketDistributor.DIMENSION.with(serverLevel::dimension), new UpdateBlock(blockSnapshotPos, Blocks.AIR.defaultBlockState(), null));
                        LevelChunkTicks<Block> blockLevelChunkTicks = allContainers.get(ChunkPos.asLong(blockSnapshotPos));
                        ScheduledTick<Block> peek = blockLevelChunkTicks.peek();
                        if (peek != null)
                            hashSet.add(new ScheduledTick<>(blockSnapshot.getReplacedBlock().getBlock(), blockSnapshotPos.relative(moveTo), peek.triggerTick(), peek.priority(), peek.subTickOrder()));
                        blockLevelChunkTicks.removeIf(blockScheduledTick -> blockScheduledTick.pos().equals(blockSnapshotPos));
                    }
                });

                blockSnapshots.forEach(blockSnapshot -> {
                    BlockPos forward = blockSnapshot.getPos().relative(moveTo);
                    BlockState currentBlock = blockSnapshot.getReplacedBlock();
                    if (BlockMover.setBlockSilently(serverLevel, forward, currentBlock, 2, 512)) {
                        BlockEntity blockEntity2 = serverLevel.getBlockEntity(forward);
                        CompoundTag tag = blockSnapshot.getTag();
                        if (tag != null) {
                            tag.putInt("x", forward.getX());
                            tag.putInt("y", forward.getY());
                            tag.putInt("z", forward.getZ());
                        }
                        if (blockEntity2 != null) {
                            if (tag != null) {
                                blockEntity2.load(tag);
                            }
                        }
                        BlockRelocation.CHANNEL.send(PacketDistributor.DIMENSION.with(serverLevel::dimension), new UpdateBlock(forward, currentBlock, tag));
                        hashSet.forEach(blockLevelTicks::schedule);
                    }
                });
                entities.forEach(entity -> entity.moveTo(entity.getX() + moveTo.getStepX(), entity.getY() + moveTo.getStepY(), entity.getZ() + moveTo.getStepZ()));
            } else {
                blockers.forEach((blockPos, blockState) -> BlockRelocation.LOGGER.warn("Relocator's movement is blocked by {} at {}", blockState.getBlock().getName().getString(), blockPos.toString().replace("BlockPos", "")));
            }
        }

    }

    @Override
    public Predicate<BlockState> grabCondition() {
        return blockState -> !blockState.isAir() && !blockState.getMaterial().isLiquid() && blockState.getDestroySpeed(null, null) != -1 && !blockState.is(BlockTags.SMALL_FLOWERS) && !blockState.is(BlockTags.LEAVES);
    }

    @Override
    public void setFromDirection(Direction direction) {
        moveFrom = direction;
    }

    @Override
    public Direction getToFrom(Direction from) {
        return movementDirections.get(from);
    }

    @Override
    public HashMap<Direction, Direction> getMovementDirections() {
        return movementDirections;
    }

    @Override
    public void setFromTo(Direction from, Direction to) {
        movementDirections.put(from, to);
    }

    @Override
    public Direction getFromDirection() {
        return moveFrom;
    }

    @Override
    public void setMovementDirections(HashMap<Direction, Direction> movementDirections) {
        this.movementDirections = movementDirections;
    }
}
