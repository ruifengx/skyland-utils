package com.github.ruifengx.skylandutils.plugins.jei;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.github.ruifengx.skylandutils.fluid.Interaction;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SkylandUtilsJEI implements IModPlugin {
    public static final ResourceLocation ID = SkylandUtils.getResource("jei_plugin");

    @Override public ResourceLocation getPluginUid() { return ID; }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        final IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(InteractionCategory.initialize(guiHelper));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        registration.addRecipes(Interaction.getAll(Minecraft.getInstance().level), InteractionCategory.ID);
    }
}
