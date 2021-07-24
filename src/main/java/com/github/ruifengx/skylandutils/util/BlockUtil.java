package com.github.ruifengx.skylandutils.util;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class BlockUtil {
    public static Item forceAsItem(Block block) {
        Item item = block.asItem();
        if (item != Items.AIR) return item;
        return new BlockItem(block, new Item.Properties());
    }
}
