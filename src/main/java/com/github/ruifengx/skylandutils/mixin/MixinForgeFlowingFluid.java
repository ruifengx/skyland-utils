package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.fluid.AllFluidTags;
import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeFlowingFluid.class)
public abstract class MixinForgeFlowingFluid {
    @Inject(method = "canBeReplacedWith", at = @At("RETURN"), cancellable = true)
    protected void canBeReplacedWith(FluidState toState, IBlockReader world, BlockPos toPos, Fluid fromFluid,
                                     Direction direction, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || FluidUtil.canBeReplacedWith(toState, world, toPos, direction));
    }

    @Inject(method = "getSlopeFindDistance", at = @At("HEAD"), cancellable = true)
    public void onGetSlopeFindDistance(IWorldReader worldIn, CallbackInfoReturnable<Integer> cir) {
        if (((ForgeFlowingFluid) (Object) this).is(AllFluidTags.ULTRA_WARM))
            cir.setReturnValue(worldIn.dimensionType().ultraWarm() ? 4 : 2);
    }

    @Inject(method = "getDropOff", at = @At("HEAD"), cancellable = true)
    protected void onGetDropOff(IWorldReader worldIn, CallbackInfoReturnable<Integer> cir) {
        if (((ForgeFlowingFluid) (Object) this).is(AllFluidTags.ULTRA_WARM))
            cir.setReturnValue(worldIn.dimensionType().ultraWarm() ? 1 : 2);
    }

    @Inject(method = "getTickDelay", at = @At("HEAD"), cancellable = true)
    public void getTickDelay(IWorldReader world, CallbackInfoReturnable<Integer> cir) {
        if (((ForgeFlowingFluid) (Object) this).is(AllFluidTags.ULTRA_WARM))
            cir.setReturnValue(world.dimensionType().ultraWarm() ? 10 : 30);
    }
}
