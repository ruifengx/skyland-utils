package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.fluid.AllFluidTags;
import com.github.ruifengx.skylandutils.fluid.ScaldingDamage;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract boolean handleFluidAcceleration(ITag<Fluid> fluidTag, double motionScale);
    @Shadow public abstract void extinguish();

    @Shadow protected Object2DoubleMap<ITag<Fluid>> eyesFluidLevel;
    @Shadow public abstract void setFire(int seconds);

    private Fluid eyesFluid = Fluids.EMPTY;
    private final Object2ObjectMap<ITag<Fluid>, Fluid> fluidsDetected
        = new Object2ObjectArrayMap<>(4);

    // override entity behaviour related tags:
    // - vanilla LAVA => SCALDING
    // - vanilla WATER => ALLOW_SWIMMING
    private static ITag<Fluid> replaceTag(ITag<Fluid> tag) {
        if (tag == FluidTags.WATER) return AllFluidTags.ALLOW_SWIMMING;
        if (tag == FluidTags.LAVA) return AllFluidTags.SCALDING;
        return tag;
    }

    @Inject(method = "func_233571_b_", at = @At("HEAD"), cancellable = true)
    void getEyesFluidLevel(ITag<Fluid> tag, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(this.eyesFluidLevel.getDouble(replaceTag(tag)));
    }

    // handle SWIMMING fluids

    @Inject(method = "updateEyesInWater", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/tags/FluidTags;getAllTags()Ljava/util/List;"),
        locals = LocalCapture.CAPTURE_FAILHARD)
    void onSetEyesFluid(CallbackInfo ci, double d0, Entity entity, BlockPos pos, FluidState state) {
        this.eyesFluid = state.getFluid();
    }

    @Inject(method = "areEyesInFluid", at = @At("HEAD"), cancellable = true)
    void onAreEyesInFluid(ITag<Fluid> tagIn, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.eyesFluid.isIn(replaceTag(tagIn)));
    }

    @Redirect(method = "updateWaterState", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
        target = "Lnet/minecraft/tags/FluidTags;WATER:Lnet/minecraft/tags/ITag$INamedTag;"))
    ITag.INamedTag<Fluid> onUpdateSwimmingState() {
        return AllFluidTags.ALLOW_SWIMMING;
    }

    // handle EXTINGUISHING fluids

    @Redirect(method = "updateWaterState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/entity/Entity;extinguish()V"))
    void cancelExtinguish(Entity self) { }

    @Inject(method = "updateWaterState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/entity/Entity;handleFluidAcceleration(Lnet/minecraft/tags/ITag;D)Z"))
    void tryExtinguish(CallbackInfo ci) {
        if (handleFluidAcceleration(AllFluidTags.EXTINGUISHING, 0))
            this.extinguish();
    }

    // handle SCALDING & IGNITING fluids

    @Inject(method = "handleFluidAcceleration", locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D"))
    void onFluidDetected(ITag<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir,
                         AxisAlignedBB bb, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax,
                         double maxHeight, boolean isPushedByWater, boolean fluidFound,
                         Vector3d velSum, int velCount, BlockPos.Mutable pos,
                         int x, int y, int z, FluidState state, double height) {
        if (height - bb.minY > maxHeight)
            this.fluidsDetected.put(fluidTag, state.getFluid());
    }

    @Redirect(method = "func_233566_aG_", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/entity/Entity;handleFluidAcceleration(Lnet/minecraft/tags/ITag;D)Z"))
    boolean onHandleLava(Entity entity, ITag<Fluid> fluidTag, double motionScale) {
        return entity.handleFluidAcceleration(AllFluidTags.SCALDING, motionScale)
            | entity.handleFluidAcceleration(AllFluidTags.IGNITING, 0);
    }

    @Redirect(method = "isInLava", at = @At(value = "INVOKE", remap = false, target =
        "Lit/unimi/dsi/fastutil/objects/Object2DoubleMap;getDouble(Ljava/lang/Object;)D"))
    double onCheckForLava(Object2DoubleMap<ITag<Fluid>> map, Object key) {
        return Math.max(map.getDouble(AllFluidTags.SCALDING), map.getDouble(AllFluidTags.IGNITING));
    }

    boolean isInIgniting() { return this.eyesFluidLevel.getDouble(AllFluidTags.IGNITING) > 0; }

    @Redirect(method = "setOnFireFromLava", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/entity/Entity;setFire(I)V"))
    void trySetOnFire(Entity entity, int seconds) {
        if (this.isInIgniting()) this.setFire(seconds);
    }

    @Redirect(method = "setOnFireFromLava", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
        target = "Lnet/minecraft/util/DamageSource;LAVA:Lnet/minecraft/util/DamageSource;"))
    DamageSource getDamageSource() {
        return new ScaldingDamage(this.fluidsDetected.get(AllFluidTags.SCALDING));
    }
}
