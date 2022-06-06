package dev.buildtool.blockrelocation;

import dev.buildtool.blockrelocation.api.BlockMover;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientHandling {

    public Supplier<Runnable> updateBlock(UpdateBlock updateBlock, Supplier<NetworkEvent.Context> contextSupplier) {
        return () -> () -> {
            Player player = Minecraft.getInstance().player;
            Level level = player.level;
            BlockMover.setBlock(level, updateBlock.pos, updateBlock.blockState, 0, 512);
            if (updateBlock.entityData != null) {
                BlockEntity blockEntity = level.getBlockEntity(updateBlock.pos);
                blockEntity.load(updateBlock.entityData);
            }
            contextSupplier.get().setPacketHandled(true);
        };
    }

}
