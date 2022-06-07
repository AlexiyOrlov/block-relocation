package dev.buildtool.blockrelocation;

import dev.buildtool.blockrelocation.api.BlockMover;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BlockRelocation.ID)
public class BlockRelocation {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String ID = "block_relocation";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);

    static final RegistryObject<Block> RELOCATOR = BLOCKS.register("relocator", () -> new RelocatorBlock(BlockBehaviour.Properties.of(Material.METAL)));
    static final RegistryObject<Block> PLATFORM = BLOCKS.register("platform", () -> new PlatformBlock(BlockBehaviour.Properties.of(Material.METAL)));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);

    static {
        ITEMS.register("relocator", () -> new BlockItem(RELOCATOR.get(), new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION)));
        ITEMS.register("platform", () -> new BlockItem(PLATFORM.get(), new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION)));
    }

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ID);

    static final RegistryObject<BlockEntityType<RelocatorEntity>> RELOCATOR_ENTITY = BLOCK_ENTITIES.register("relocator", () -> new BlockEntityType<>(RelocatorEntity::new, Collections.singleton(RELOCATOR.get()), null));
    static final RegistryObject<BlockEntityType<PlatformEntity>> PLATFORM_ENTITY = BLOCK_ENTITIES.register("platform", () -> new BlockEntityType<>(PlatformEntity::new, Collections.singleton(PLATFORM.get()), null));
    static final String PROTOCOL = "1.0";
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(ID, "network"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    public static ForgeConfigSpec.IntValue GRABBER_BATCH_LIMIT;
    public BlockRelocation() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, new ForgeConfigSpec.Builder().configure(builder -> {
            GRABBER_BATCH_LIMIT = builder.defineInRange("Max number of connected platofrms", () -> 900, 2, 5712);
            return builder.build();
        }).getRight());
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CHANNEL.registerMessage(0, UpdateBlock.class, (updateBlock, friendlyByteBuf) -> {
            friendlyByteBuf.writeBlockPos(updateBlock.pos);
            friendlyByteBuf.writeInt(Block.getId(updateBlock.blockState));
            if (updateBlock.entityData != null)
                friendlyByteBuf.writeNbt(updateBlock.entityData);
        }, friendlyByteBuf -> {
            BlockPos pos = friendlyByteBuf.readBlockPos();
            int blockState = friendlyByteBuf.readInt();
            return new UpdateBlock(pos, Block.stateById(blockState), friendlyByteBuf.isReadable() ? friendlyByteBuf.readNbt() : null);
        }, (updateBlock, contextSupplier) -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, new ClientHandling().updateBlock(updateBlock, contextSupplier));
        });
        CHANNEL.registerMessage(1, SetDirections.class, (setDirections, friendlyByteBuf) -> {
                    setDirections.directionHashMap.forEach((direction, direction2) -> {
                        friendlyByteBuf.writeEnum(direction);
                        friendlyByteBuf.writeEnum(direction2);
                    });
                    friendlyByteBuf.writeBlockPos(setDirections.platformPosition);
                }, friendlyByteBuf -> {
                    HashMap<Direction, Direction> directionHashMap = new HashMap<>(6, 1);
                    for (int i = 0; i < 6; i++) {
                        Direction from = friendlyByteBuf.readEnum(Direction.class);
                        Direction to = friendlyByteBuf.readEnum(Direction.class);
                        directionHashMap.put(from, to);
                    }
                    return new SetDirections(directionHashMap, friendlyByteBuf.readBlockPos());
                },
                (setDirections, contextSupplier) -> {
                    ServerLevel serverLevel = contextSupplier.get().getSender().getLevel();
                    BlockEntity blockEntity = serverLevel.getBlockEntity(setDirections.platformPosition);
                    if (blockEntity instanceof BlockMover blockMover) {
                        blockMover.setMovementDirections(setDirections.directionHashMap);
                        blockEntity.setChanged();
                        contextSupplier.get().setPacketHandled(true);
                    }
                });
    }
}
