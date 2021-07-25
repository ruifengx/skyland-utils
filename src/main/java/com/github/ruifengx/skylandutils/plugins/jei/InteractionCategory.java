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
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

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
    private final IDrawable crossSign;
    private final IDrawable exclamation;
    private final IDrawable smallFluidEnv;
    private final IDrawable middleFluidEnv;
    private final IDrawable largeFluidEnv;

    public InteractionCategory(IGuiHelper gui) {
        this.background = gui.createDrawable(LOC, 0, 0, 116, 64);
        this.icon = new Icon(gui);
        this.arrow = gui.drawableBuilder(LOC, 0, 64, 50, 17)
            .buildAnimated(80, IDrawableAnimated.StartDirection.LEFT, false);
        this.crossSign = gui.createDrawable(LOC, 0, 95, 14, 13);
        this.exclamation = gui.createDrawable(LOC, 14, 95, 14, 13);
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
        if (recipe.consumesSource()) this.crossSign.draw(matrices, 13, 44);
        else if (recipe.mightConsumeSource()) this.exclamation.draw(matrices, 13, 44);
    }
    @Override public void setIngredients(Interaction recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, recipe.getDisplayFluidInputs());
        ingredients.setInputLists(VanillaTypes.ITEM, recipe.getDisplayBlockInputs());
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getDisplayOutput());
    }

    public static class SlotIndexedTooltip<T> implements ITooltipCallback<T> {
        private final Map<Integer, List<ITextComponent>> mapTips = new HashMap<>();
        protected ITextComponent preprocessTip(ITextComponent tip) { return tip; }
        private void addTipImpl(int slotIndex, ITextComponent tip) {
            this.mapTips.putIfAbsent(slotIndex, new ArrayList<>());
            this.mapTips.get(slotIndex).add(tip);
        }
        final public void addTipRaw(int slotIndex, String tipId) {
            this.addTipImpl(slotIndex, new TranslationTextComponent(
                Util.makeDescriptionId("jei", SkylandUtils.getResource(tipId))));
        }
        final public void addTip(int slotIndex, ITextComponent tip) {
            this.addTipImpl(slotIndex, this.preprocessTip(tip));
        }
        final public void addNullableTip(int slotIndex, @Nullable ITextComponent tip) {
            if (tip != null) this.addTip(slotIndex, tip);
        }
        @Override
        public void onTooltip(int slotIndex, boolean input, T ingredient, List<ITextComponent> tooltip) {
            if (this.mapTips.containsKey(slotIndex))
                tooltip.addAll(this.mapTips.get(slotIndex));
        }
    }

    public static class FluidTagTooltip extends SlotIndexedTooltip<FluidStack> {
        @Override
        protected ITextComponent preprocessTip(ITextComponent tip) {
            return new TranslationTextComponent(Util.makeDescriptionId("jei",
                SkylandUtils.getResource("gui.tooltip.fluid_tag")), tip);
        }
    }

    public static class BlockTagTooltip extends SlotIndexedTooltip<ItemStack> {
        @Override
        protected ITextComponent preprocessTip(ITextComponent tip) {
            return new TranslationTextComponent(Util.makeDescriptionId("jei",
                SkylandUtils.getResource("gui.tooltip.block_tag")), tip);
        }
    }

    @Override public void setRecipe(IRecipeLayout recipeLayout, Interaction recipe, IIngredients ingredients) {
        final IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();
        final FluidTagTooltip fluidTips = new FluidTagTooltip();
        fluids.init(0, true, 12, 24, 16, 16, 1000, false, null);
        fluids.set(0, recipe.getDisplaySourceFluid());
        fluidTips.addNullableTip(0, recipe.getSourceFluid().getDisplayName());
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
                fluidTips.addNullableTip(1 + i, recipe.getTargetFluid(i).getDisplayName());
                fluidTips.addTipRaw(1 + i, "gui.tooltip.env_fluid");
            }
            {
                final int xPos = layout.fluidPos.getX() + 1 + (n - 1) * width;
                fluids.init(n, true, xPos, 6, WIDTH - width * (n - 1), 12, 1000, false, null);
                fluids.set(n, inputs.get(n - 1));
                fluidTips.addNullableTip(n, recipe.getTargetFluid(n - 1).getDisplayName());
                fluidTips.addTipRaw(n, "gui.tooltip.env_fluid");
            }
        }
        fluids.addTooltipCallback(fluidTips);

        final IGuiItemStackGroup blocks = recipeLayout.getItemStacks();
        final BlockTagTooltip blockTips = new BlockTagTooltip();
        blocks.init(0, true, 47, 42);
        blocks.set(0, recipe.getDisplayBlockBelow());
        blockTips.addNullableTip(0, recipe.getBlockExpectedBelow().getDisplayName());
        blockTips.addTipRaw(0, "gui.tooltip.below_block");
        blocks.init(1, false, 90, 23);
        blocks.set(1, recipe.getDisplayOutput());
        for (int i = 0; i < layout.blockPos.size(); ++i) {
            Rectangle2d pos = layout.blockPos.get(i);
            blocks.init(2 + i, true, pos.getX(), pos.getY());
            blocks.set(2 + i, recipe.getDisplayEnvBlocks().get(i));
            blockTips.addNullableTip(2 + i, recipe.getTargetBlock(i).getDisplayName());
            blockTips.addTipRaw(2 + i, "gui.tooltip.env_block");
        }
        blocks.addTooltipCallback(blockTips);
    }
    @Override public List<ITextComponent> getTooltipStrings(Interaction recipe, double mouseX, double mouseY) {
        final boolean consumesSource = recipe.consumesSource();
        final boolean mightConsumeSource = recipe.mightConsumeSource();
        if (consumesSource || mightConsumeSource) {
            final String id = consumesSource ? "gui.tooltip.consume_fluid_source"
                : "gui.tooltip.might_consume_fluid_source";
            final Rectangle2d crossPos = new Rectangle2d(13, 44, 14, 13);
            if (crossPos.contains(MathHelper.floor(mouseX), MathHelper.floor(mouseY))) {
                List<ITextComponent> result = new ArrayList<>();
                result.add(new TranslationTextComponent(Util.makeDescriptionId(
                    "jei", SkylandUtils.getResource(id))));
                return result;
            }
        }
        return IRecipeCategory.super.getTooltipStrings(recipe, mouseX, mouseY);
    }
}
