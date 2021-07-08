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
        () -> new BlazeBlock(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.GOLD)
            .setRequiresTool().setLightLevel((state) -> 15)
            .hardnessAndResistance(0.5F)
            .setAllowsSpawn((state, reader, pos, entity) -> entity.isImmuneToFire())
            .setEmmisiveRendering((state, world, pos) -> true)));
}
