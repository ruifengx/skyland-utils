package com.github.ruifengx.skylandutils.block;

import com.github.ruifengx.skylandutils.SkylandUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class SkylandBlocks {
    public static final DeferredRegister<Block> BLOCKS
        = DeferredRegister.create(ForgeRegistries.BLOCKS, SkylandUtils.MODID);

    public static final RegistryObject<Block> BLAZE_BLOCK = BLOCKS.register("blaze_block",
        () -> new BlazeBlock(AbstractBlock.Properties.of(Material.STONE, MaterialColor.GOLD)
            .requiresCorrectToolForDrops().lightLevel((state) -> 15)
            .strength(0.5F)
            .isValidSpawn((state, reader, pos, entity) -> entity.fireImmune())
            .emissiveRendering((state, world, pos) -> true)));
}
