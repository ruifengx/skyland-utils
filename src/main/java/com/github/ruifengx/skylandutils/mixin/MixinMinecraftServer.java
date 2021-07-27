package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.carpet.TickWarpStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends RecursiveEventLoop<TickDelayedTask> {
    public MixinMinecraftServer(String name) { super(name); }

    @Shadow protected long nextTickTime;
    @Shadow private long lastOverloadWarning;

    @Shadow protected abstract void tickServer(BooleanSupplier p_71217_1_);

    @Shadow protected abstract void waitUntilNextTick();
    @Inject(method = "runServer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/util/Util;getMillis()J", ordinal = 1))
    void resetNextTickTime(CallbackInfo ci) {
        if (TickWarpStatus.isWarppingSpeed() && TickWarpStatus.shouldWarppingContinue())
            this.nextTickTime = this.lastOverloadWarning = Util.getMillis();
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V"))
    void forceTicking(MinecraftServer self, BooleanSupplier shouldTick) {
        if (TickWarpStatus.isWarppingSpeed())
            shouldTick = () -> true;
        this.tickServer(shouldTick);
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/server/MinecraftServer;waitUntilNextTick()V"))
    void cancelWaitUntilNextTick(MinecraftServer self) {
        // CAUTION: check on update of game version whether the following invariant holds:
        //      waitUntilNextTick == runAllTasks + managedBlock
        if (TickWarpStatus.isWarppingSpeed())
            this.runAllTasks();
        else
            this.waitUntilNextTick();
    }
}
