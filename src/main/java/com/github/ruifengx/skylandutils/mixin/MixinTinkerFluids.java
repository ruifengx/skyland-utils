package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.util.IFluidAttributesExtra;
import net.minecraftforge.fluids.FluidAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.fluids.TinkerFluids;

@Mixin(TinkerFluids.class)
public class MixinTinkerFluids {
    @Inject(method = "coolBuilder", at = @At("RETURN"), remap = false, cancellable = true)
    private static void coolBuilder(CallbackInfoReturnable<FluidAttributes.Builder> cir) {
        cir.setReturnValue(((IFluidAttributesExtra) cir.getReturnValue()).waterLike());
    }

    @Inject(method = "hotBuilder", at = @At("RETURN"), remap = false, cancellable = true)
    private static void hotBuilder(CallbackInfoReturnable<FluidAttributes.Builder> cir) {
        cir.setReturnValue(((IFluidAttributesExtra) cir.getReturnValue()).lavaLike());
    }
}
