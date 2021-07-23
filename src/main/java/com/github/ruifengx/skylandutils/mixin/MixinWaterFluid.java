package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFluid.class)
public abstract class MixinWaterFluid {
    @Shadow public abstract boolean isSame(Fluid fluidIn);

    @Inject(method = "canBeReplacedWith", at = @At("RETURN"), cancellable = true)
    protected void canDisplace(FluidState toState, IBlockReader world, BlockPos toPos, Fluid fromFluid,
                               Direction direction, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(direction == Direction.DOWN && !isSame(fromFluid)
            || FluidUtil.canBeReplacedWith(toState, world, toPos, direction));
    }
}
