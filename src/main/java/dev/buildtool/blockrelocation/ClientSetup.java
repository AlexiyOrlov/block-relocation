package dev.buildtool.blockrelocation;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers clientSetupEvent) {
        clientSetupEvent.registerBlockEntityRenderer(BlockRelocation.PLATFORM_ENTITY.get(), p_173571_ -> new PlatformRenderer());
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent clientSetupEvent) {
        ItemBlockRenderTypes.setRenderLayer(BlockRelocation.PLATFORM.get(), RenderType.cutout());
    }

}
