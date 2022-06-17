package dev.buildtool.blockrelocation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
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

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent registerCommandsEvent) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = registerCommandsEvent.getDispatcher();
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(Commands.literal(BlockRelocation.ID).then(Commands.literal("toggleNotification").executes(context -> {
            RelocatorEntity.notifyAboutBlockers = !RelocatorEntity.notifyAboutBlockers;
            context.getSource().getPlayerOrException().sendMessage(new TranslatableComponent("block.relocation.command"), Util.NIL_UUID);
            return 0;
        })));
        commandDispatcher.getRoot().addChild(literalCommandNode.createBuilder().build());
        ;
    }
}
