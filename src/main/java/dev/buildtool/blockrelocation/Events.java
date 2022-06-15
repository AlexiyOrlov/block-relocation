package dev.buildtool.blockrelocation;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Events {

    @SubscribeEvent
    public static void stopSignalThrough(BlockEvent.NeighborNotifyEvent neighborNotifyEvent) {
        BlockState blockState = neighborNotifyEvent.getState();
        if (blockState.getBlock() == BlockRelocation.RELOCATOR.get()) {
            neighborNotifyEvent.setCanceled(true);
        }
    }
}
