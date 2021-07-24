package com.github.ruifengx.skylandutils.plugins.jei;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.github.ruifengx.skylandutils.fluid.Interaction;
import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InteractionCategory implements IRecipeCategory<Interaction> {
    public static final ResourceLocation ID = SkylandUtils.getResource("interaction");
    private static final ResourceLocation LOC = SkylandUtils.getResource("textures/gui/jei/interaction.png");

    private static InteractionCategory INSTANCE;
    public static InteractionCategory initialize(IGuiHelper gui) {
        if (INSTANCE == null) INSTANCE = new InteractionCategory(gui);
        return INSTANCE;
    }

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable smallFluidEnv;
    private final IDrawable middleFluidEnv;
    private final IDrawable largeFluidEnv;

    public InteractionCategory(IGuiHelper gui) {
        this.background = gui.createDrawable(LOC, 0, 0, 116, 64);
        this.icon = new Icon(gui);
        this.arrow = gui.drawableBuilder(LOC, 0, 64, 50, 17)
            .buildAnimated(80, IDrawableAnimated.StartDirection.LEFT, false);
        this.smallFluidEnv = gui.createDrawable(LOC, 50, 81, 14, 14);
        this.middleFluidEnv = gui.createDrawable(LOC, 32, 81, 18, 14);
        this.largeFluidEnv = gui.createDrawable(LOC, 0, 81, 32, 14);
    }

    @Override public ResourceLocation getUid() { return ID; }
    @Override public Class<? extends Interaction> getRecipeClass() { return Interaction.class; }
    @Override public String getTitle() {
        return ForgeI18n.getPattern(Util.makeDescriptionId("jei",
            new ResourceLocation(SkylandUtils.MODID, "interaction.title")));
    }
    @Override public IDrawable getBackground() { return this.background; }
    @Override public IDrawable getIcon() { return this.icon; }

    private static final class EnvLayout {
        @Nullable public final IDrawable fluidEnv;
        @Nullable public final Rectangle2d fluidPos;
        public final List<Rectangle2d> blockPos;
        private EnvLayout(@Nullable Rectangle2d fluidPos, List<Rectangle2d> blockPos, @Nullable IDrawable fluidEnv) {
            this.fluidPos = fluidPos;
            this.blockPos = blockPos;
            this.fluidEnv = fluidEnv;
        }

        private static final int CENTER_X = 56;
        private static final int WIDTH_BLOCK = 18;
        private static IDrawable selectFluidEnv(int m) {
            if (m >= 4) return InteractionCategory.INSTANCE.largeFluidEnv;
            if (m >= 3) return InteractionCategory.INSTANCE.middleFluidEnv;
            return InteractionCategory.INSTANCE.smallFluidEnv;
        }
        public static EnvLayout fluidOnly(int m) {
            final IDrawable fluidEnv = EnvLayout.selectFluidEnv(m);
            final int left = CENTER_X - fluidEnv.getWidth() / 2;
            return new EnvLayout(new Rectangle2d(left, 5, fluidEnv.getWidth(), 14),
                new ArrayList<>(), EnvLayout.selectFluidEnv(m));
        }
        public static EnvLayout blockOnly(int n) {
            final int total_width = WIDTH_BLOCK * n;
            final int left = CENTER_X - total_width / 2;
            final List<Rectangle2d> result = new ArrayList<>();
            for (int i = 0; i < n; ++i)
                result.add(new Rectangle2d(left + i * WIDTH_BLOCK, 3, WIDTH_BLOCK, WIDTH_BLOCK));
            return new EnvLayout(null, result, null);
        }
        public static EnvLayout both(int m, int n) {
            final IDrawable fluidEnv = EnvLayout.selectFluidEnv(m);
            final int FLUID_WIDTH = fluidEnv.getWidth();
            final int total_width = FLUID_WIDTH + 2 + WIDTH_BLOCK * n;
            final int left = CENTER_X - total_width / 2;
            final Rectangle2d fluidPos = new Rectangle2d(left, 5, FLUID_WIDTH, FLUID_WIDTH);
            final int blockLeft = left + FLUID_WIDTH + 2;
            final List<Rectangle2d> result = new ArrayList<>();
            for (int i = 0; i < n; ++i)
                result.add(new Rectangle2d(blockLeft + i * WIDTH_BLOCK, 3, WIDTH_BLOCK, WIDTH_BLOCK));
            return new EnvLayout(fluidPos, result, fluidEnv);
        }
        public static EnvLayout from(Interaction recipe) {
            if (recipe.getDisplayEnvBlocks().isEmpty())
                return fluidOnly(recipe.getDisplayEnvFluids().size());
            else if (recipe.getDisplayEnvFluids().isEmpty())
                return blockOnly(recipe.getDisplayEnvBlocks().size());
            else
                return both(recipe.getDisplayEnvFluids().size(), recipe.getDisplayEnvBlocks().size());
        }
    }

    @Override public void draw(Interaction recipe, MatrixStack matrices, double mouseX, double mouseY) {
        this.arrow.draw(matrices, 34, 24);
        EnvLayout layout = EnvLayout.from(recipe);
        if (layout.fluidPos != null) {
            assert layout.fluidEnv != null;
            layout.fluidEnv.draw(matrices, layout.fluidPos.getX(), layout.fluidPos.getY());
        }
    }
    @Override public void setIngredients(Interaction recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, recipe.getDisplayFluidInputs());
        ingredients.setInputLists(VanillaTypes.ITEM, recipe.getDisplayBlockInputs());
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getDisplayOutput());
    }
    @Override public void setRecipe(IRecipeLayout recipeLayout, Interaction recipe, IIngredients ingredients) {
        final IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();
        fluids.init(0, true, 12, 24, 16, 16, 1000, false, null);
        fluids.set(0, recipe.getDisplaySourceFluid());
        EnvLayout layout = EnvLayout.from(recipe);
        List<List<FluidStack>> inputs = recipe.getDisplayEnvFluids();
        final int n = inputs.size();
        if (n > 0) {
            assert layout.fluidPos != null;
            final int WIDTH = layout.fluidPos.getWidth() - 2;
            final int width = WIDTH / n;
            for (int i = 0; i < n - 1; ++i) {
                final int xPos = layout.fluidPos.getX() + 1 + i * width;
                fluids.init(1 + i, true, xPos, 6, width, 12, 1000, false, null);
                fluids.set(1 + i, inputs.get(i));
            }
            {
                final int xPos = layout.fluidPos.getX() + 1 + (n - 1) * width;
                fluids.init(n, true, xPos, 6, WIDTH - width * (n - 1), 12, 1000, false, null);
                fluids.set(n, inputs.get(n - 1));
            }
        }
        final IGuiItemStackGroup blocks = recipeLayout.getItemStacks();
        blocks.init(0, true, 47, 42);
        blocks.set(0, recipe.getDisplayBlockBelow());
        blocks.init(1, false, 90, 23);
        blocks.set(1, recipe.getDisplayOutput());
        for (int i = 0; i < layout.blockPos.size(); ++i) {
            Rectangle2d pos = layout.blockPos.get(i);
            blocks.init(2 + i, true, pos.getX(), pos.getY());
            blocks.set(2 + i, recipe.getDisplayEnvBlocks().get(i));
        }
    }
    @Override public List<ITextComponent> getTooltipStrings(Interaction recipe, double mouseX, double mouseY) {
        return IRecipeCategory.super.getTooltipStrings(recipe, mouseX, mouseY);
    }
}
