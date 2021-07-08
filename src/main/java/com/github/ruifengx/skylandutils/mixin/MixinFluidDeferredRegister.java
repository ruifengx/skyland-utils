package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.fluid.LavaLike;
import com.github.ruifengx.skylandutils.fluid.WaterLike;
import com.github.ruifengx.skylandutils.util.FluidUtil;
import com.github.ruifengx.skylandutils.util.IFluidAttributesExtra;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.registration.FluidBuilder;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;
import slimeknights.mantle.registration.object.FluidObject;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(FluidDeferredRegister.class)
public abstract class MixinFluidDeferredRegister {
    @Shadow(remap = false)
    public abstract <F extends ForgeFlowingFluid> FluidObject<F> register(
        String name, String tagName, FluidAttributes.Builder builder,
        Function<ForgeFlowingFluid.Properties, ? extends F> still,
        Function<ForgeFlowingFluid.Properties, ? extends F> flowing,
        Material material, int lightLevel);

    @Inject(method = "register(" +
        "Ljava/lang/String;" +
        "Ljava/lang/String;" +
        "Lnet/minecraftforge/fluids/FluidAttributes$Builder;" +
        "Lnet/minecraft/block/material/Material;I" +
        ")Lslimeknights/mantle/registration/object/FluidObject;",
        at = @At("HEAD"), remap = false, cancellable = true)
    public void onRegister(String name, String tagName, FluidAttributes.Builder builder, Material material,
                           int lightLevel, CallbackInfoReturnable<FluidObject<ForgeFlowingFluid>> cir) {
        if (((IFluidAttributesExtra) builder).isLavaLike()) {
            cir.setReturnValue(register(
                name, tagName, builder, LavaLike.Source::new,
                LavaLike.Flowing::new, material, lightLevel
            ));
        } else if (((IFluidAttributesExtra) builder).isWaterLike()) {
            cir.setReturnValue(register(
                name, tagName, builder, WaterLike.Source::new,
                WaterLike.Flowing::new, material, lightLevel
            ));
        }
    }

    @Inject(method = "register(" +
        "Ljava/lang/String;Ljava/lang/String;" +
        "Lslimeknights/mantle/registration/FluidBuilder;" +
        "Ljava/util/function/Function;" +
        "Ljava/util/function/Function;" +
        "Ljava/util/function/Function;" +
        ")Lslimeknights/mantle/registration/object/FluidObject;",
        at = @At("RETURN"), remap = false)
    public void onRegister(
        String name, String tagName, FluidBuilder builder,
        Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid> still,
        Function<ForgeFlowingFluid.Properties, ForgeFlowingFluid> flowing,
        Function<Supplier<FlowingFluid>, FlowingFluidBlock> block,
        CallbackInfoReturnable<FluidObject<ForgeFlowingFluid>> cir) {
        FluidUtil.registerBucketForDispenser(FluidUtil.getBucket(builder));
    }
}
