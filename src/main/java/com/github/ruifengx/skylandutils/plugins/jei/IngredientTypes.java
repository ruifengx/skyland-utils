package com.github.ruifengx.skylandutils.plugins.jei;

import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.block.Block;

public class IngredientTypes {
    public static final IIngredientType<Block> BLOCK = () -> Block.class;
}
