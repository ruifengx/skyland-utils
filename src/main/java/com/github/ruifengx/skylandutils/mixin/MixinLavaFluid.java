package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.fluid.WaterLike;
import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LavaFluid.class)
public abstract class MixinLavaFluid {
    @Inject(method = "canDisplace", at = @At("RETURN"), cancellable = true)
    protected void canDisplace(FluidState toState, IBlockReader world, BlockPos toPos, Fluid fromFluid,
                               Direction direction, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(toState.getActualHeight(world, toPos) >= 0.44444445F && WaterLike.isWaterLike(fromFluid)
            || FluidUtil.canDisplace(toState, world, toPos, direction));
    }
}
