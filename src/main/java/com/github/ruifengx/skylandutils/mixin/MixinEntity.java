package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.github.ruifengx.skylandutils.fluid.LavaLike;
import com.github.ruifengx.skylandutils.fluid.WaterLike;
import com.github.ruifengx.skylandutils.util.PlayerUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.fluids.fluids.SlimeFluid;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow protected boolean firstUpdate;
    @Shadow public World world;
    @Shadow public float fallDistance;
    @Shadow protected Object2DoubleMap<ITag<Fluid>> eyesFluidLevel;
    @Shadow
    @Nullable
    protected ITag<Fluid> field_241335_O_;

    @Nullable
    private LavaLike currentLavaLike = null;

    @Shadow public abstract AxisAlignedBB getBoundingBox();
    @Shadow public abstract Vector3d getMotion();
    @Shadow public abstract void setMotion(Vector3d motionIn);
    @Shadow public abstract boolean isPushedByWater();
    @Shadow public abstract boolean isImmuneToFire();
    @Shadow public abstract void setFire(int seconds);
    @Shadow public abstract boolean attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract double getPosYEye();
    @Shadow public abstract double getPosX();
    @Shadow public abstract double getPosZ();

    @Inject(method = "updateEyesInWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;" +
        "getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), cancellable = true)
    private void onUpdateEyesInWater(CallbackInfo ci) {
        double heightEyes = this.getPosYEye() - (double) 0.11111111F;
        BlockPos eyePos = new BlockPos(this.getPosX(), heightEyes, this.getPosZ());
        FluidState fluidState = this.world.getFluidState(eyePos);
        for (ITag<Fluid> itag : FluidTags.getAllTags()) {
            if (fluidState.isTagged(itag) ||
                itag == FluidTags.WATER && WaterLike.isWaterLike(fluidState.getFluid()) ||
                itag == FluidTags.LAVA && LavaLike.isLavaLike(fluidState.getFluid())) {
                double h = (float) eyePos.getY() + fluidState.getActualHeight(this.world, eyePos);
                if (h > heightEyes) this.field_241335_O_ = itag;
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "handleFluidAcceleration", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("deprecation")
    private void updateCurrentFluidType(ITag<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir) {
        final AxisAlignedBB bb = this.getBoundingBox().shrink(0.001D);
        final int minX = MathHelper.floor(bb.minX), maxX = MathHelper.ceil(bb.maxX);
        final int minY = MathHelper.floor(bb.minY), maxY = MathHelper.ceil(bb.maxY);
        final int minZ = MathHelper.floor(bb.minZ), maxZ = MathHelper.ceil(bb.maxZ);
        if (!this.world.isAreaLoaded(minX, minY, minZ, maxX, maxY, maxZ)) cir.setReturnValue(false);

        if (fluidTag == FluidTags.LAVA) this.currentLavaLike = null;
        boolean fluidFound = false;

        double depth = 0;
        int velocityCount = 0;
        Vector3d sumVelocity = Vector3d.ZERO;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    pos.setPos(x, y, z);
                    FluidState fluidState = this.world.getFluidState(pos);
                    Fluid thisFluid = fluidState.getFluid();
                    if (thisFluid.isEquivalentTo(Fluids.EMPTY)) continue;
                    if (fluidTag == FluidTags.LAVA && thisFluid instanceof LavaLike)
                        this.currentLavaLike = (LavaLike) thisFluid;
                    if (fluidTag == FluidTags.LAVA && LavaLike.isLavaLike(thisFluid) ||
                        fluidTag == FluidTags.WATER && WaterLike.isWaterLike(thisFluid) ||
                        fluidTag == FluidTags.WATER && thisFluid instanceof SlimeFluid) {
                        fluidFound = true;
                        double fluidSurfaceHeight = (float) y + fluidState.getActualHeight(this.world, pos);
                        if (fluidSurfaceHeight > bb.minY) {
                            depth = Math.max(fluidSurfaceHeight - bb.minY, depth);
                            if (this.isPushedByWater()) {
                                Vector3d v = fluidState.getFlow(this.world, pos);
                                if (depth < 0.4D) v.scale(depth);
                                sumVelocity = sumVelocity.add(v);
                                ++velocityCount;
                            }
                        }
                    }
                }
            }
        }

        if (sumVelocity.length() > 0) {
            if (velocityCount > 0) sumVelocity = sumVelocity.scale(1.0D / (double) velocityCount);
            if (!PlayerUtil.isPlayerEntity((Entity) (Object) this)) sumVelocity = sumVelocity.normalize();
            Vector3d vDelta = sumVelocity.scale(motionScale);
            Vector3d vCurrent = this.getMotion();
            if (Math.abs(vCurrent.x) < 0.003D && Math.abs(vCurrent.z) < 0.003D && vDelta.length() < 0.0045000000000000005D) {
                vDelta = vDelta.normalize().scale(0.0045000000000000005D);
            }
            this.setMotion(vCurrent.add(vDelta));
        }

        this.eyesFluidLevel.put(fluidTag, depth);
        cir.setReturnValue(fluidFound);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInLava()Z", ordinal = 1))
    void onCheckLavaDamage(CallbackInfo ci) {
        if (!this.firstUpdate && this.currentLavaLike != null) {
            if (!this.isImmuneToFire()) {
                double scaleTemperature = this.currentLavaLike.getAttributes().getTemperature() / 1000.0D;
                this.setFire(MathHelper.floor(15 * scaleTemperature));
                this.attackEntityFrom(this.currentLavaLike.asDamageSource, 4.0F);
            }
            this.fallDistance *= 0.5F;
        }
    }
}
