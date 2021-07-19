package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.fluid.AllFluidTags;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.tags.ITag;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem {
    @Shadow(remap = false)
    public abstract Fluid getFluid();

    @Redirect(method = "tryPlaceContainedLiquid", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
        target = "Lnet/minecraft/tags/FluidTags;WATER:Lnet/minecraft/tags/ITag$INamedTag;"))
    ITag.INamedTag<Fluid> getVaporizingFluids() {
        return AllFluidTags.VAPORIZING;
    }
}
