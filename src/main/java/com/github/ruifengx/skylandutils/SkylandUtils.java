package com.github.ruifengx.skylandutils;

import com.github.ruifengx.skylandutils.block.SkylandBlocks;
import com.github.ruifengx.skylandutils.fluid.AllFluidTags;
import com.github.ruifengx.skylandutils.fluid.Interaction;
import com.github.ruifengx.skylandutils.item.AllItemTags;
import com.github.ruifengx.skylandutils.item.SkylandItemGroups;
import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SkylandUtils.MODID)
public class SkylandUtils {
    public static final String MODID = "skyland-utils";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public SkylandUtils() {
        AllItemTags.register();
        AllFluidTags.register();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        SkylandBlocks.BLOCKS.register(modEventBus);
    }

    @SubscribeEvent
    public void onTagsUpdated(final TagsUpdatedEvent event) {
        FluidUtil.registerAllBuckets();
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class
    // (this is subscribing to the MOD Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) { }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            final IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();
            SkylandBlocks.BLOCKS.getEntries().stream()
                .map(RegistryObject::get)
                .forEach(block -> {
                    final Item.Properties properties = new Item.Properties().group(SkylandItemGroups.SKYLAND_GROUP);
                    final BlockItem blockItem = new BlockItem(block, properties);
                    blockItem.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
                    registry.register(blockItem);
                });
        }

        @SubscribeEvent
        public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(Interaction.Serializer.INSTANCE);
        }
    }
}
