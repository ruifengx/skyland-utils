package com.github.ruifengx.skylandutils.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.ruifengx.skylandutils.fluid.Interaction;

@Mixin(FlowingFluidBlock.class)
public abstract class MixinFlowingFluidBlock {
    @Shadow
    private void fizz(IWorld worldIn, BlockPos pos) {}

    @Shadow(remap = false)
    public abstract FlowingFluid getFluid();

    @Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable = true)
    private void onReactWithNeighbors(World worldIn, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        for (Interaction interact : Interaction.getAll(worldIn)) {
            final Fluid fluid = worldIn.getFluidState(pos).getType();
            boolean generated = interact.matchWorldAt(worldIn, pos, fluid, blockToGenerate -> {
                worldIn.setBlockAndUpdate(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(
                    worldIn, pos, pos, blockToGenerate.defaultBlockState()
                ));
                this.fizz(worldIn, pos);
            });
            if (generated) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
