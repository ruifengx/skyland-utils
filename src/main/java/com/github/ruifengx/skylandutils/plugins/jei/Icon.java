package com.github.ruifengx.skylandutils.plugins.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Icon implements IDrawable {
    private final IDrawable waterBucket;
    private final IDrawable lavaBucket;

    public Icon(IGuiHelper gui) {
        this.waterBucket = gui.createDrawableIngredient(new ItemStack(Items.WATER_BUCKET, 1));
        this.lavaBucket = gui.createDrawableIngredient(new ItemStack(Items.LAVA_BUCKET, 1));
    }

    @Override public int getWidth() { return 18; }
    @Override public int getHeight() { return 18; }
    @Override public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 0);
        matrixStack.scale(0.75f, 0.75f, 1);
        matrixStack.translate(1, 2, 0);
        this.waterBucket.draw(matrixStack);
        this.lavaBucket.draw(matrixStack, 6, 3);
        matrixStack.popPose();
    }
}
