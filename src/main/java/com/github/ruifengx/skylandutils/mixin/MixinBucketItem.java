package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.github.ruifengx.skylandutils.fluid.WaterLike;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem {
    @Shadow(remap = false)
    public abstract Fluid getFluid();

    @Inject(method = "tryPlaceContainedLiquid", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/DimensionType;isUltrawarm()Z"), cancellable = true)
    void onCheckVaporize(PlayerEntity player, World worldIn, BlockPos posIn,
                         BlockRayTraceResult rayTrace, CallbackInfoReturnable<Boolean> cir) {
        if (worldIn.getDimensionType().isUltrawarm() && WaterLike.isWaterLike(this.getFluid())) {
            worldIn.playSound(player, posIn, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                worldIn.addParticle(ParticleTypes.LARGE_SMOKE, (double) posIn.getX() + Math.random(),
                    (double) posIn.getY() + Math.random(), (double) posIn.getZ() + Math.random(),
                    0.0D, 0.0D, 0.0D);
            }
            cir.setReturnValue(true);
        }
    }
}
