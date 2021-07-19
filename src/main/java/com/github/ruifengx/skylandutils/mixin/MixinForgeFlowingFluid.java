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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.fluids.fluids.SlimeFluid;

@Mixin(ForgeFlowingFluid.class)
public abstract class MixinForgeFlowingFluid {
    @Inject(method = "canDisplace", at = @At("RETURN"), cancellable = true)
    protected void canDisplace(FluidState toState, IBlockReader world, BlockPos toPos, Fluid fromFluid,
                               Direction direction, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || FluidUtil.canDisplace(toState, world, toPos, direction));
    }

    @Inject(method = "getLevelDecreasePerBlock", at = @At("HEAD"), cancellable = true)
    protected void onGetLevelDecreasePerBlock(IWorldReader worldIn, CallbackInfoReturnable<Integer> cir) {
        if (((ForgeFlowingFluid) (Object) this).isIn(AllFluidTags.ULTRAWARM))
            cir.setReturnValue(worldIn.getDimensionType().isUltrawarm() ? 1 : 2);
    }

    @Inject(method = "getTickRate", at = @At("HEAD"), cancellable = true)
    public void getTickRate(IWorldReader world, CallbackInfoReturnable<Integer> cir) {
        if (((ForgeFlowingFluid) (Object) this).isIn(AllFluidTags.ULTRAWARM))
            cir.setReturnValue(world.getDimensionType().isUltrawarm() ? 10 : 30);
    }
}
