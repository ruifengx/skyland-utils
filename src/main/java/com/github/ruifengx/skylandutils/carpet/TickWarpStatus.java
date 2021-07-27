package com.github.ruifengx.skylandutils.carpet;

import com.github.ruifengx.skylandutils.SkylandUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.Nullable;

public final class TickWarpStatus {
    private static long totalWarpTicks = 0;
    private static long remainingWarpTicks = 0;
    private static long warpStartNanoTime = 0;
    @Nullable
    private static ServerPlayerEntity warppingPlayer = null;

    public static boolean isWarppingSpeed() { return totalWarpTicks > 0; }
    public static void startWarpping(ServerPlayerEntity player, long ticks, CommandSource source) {
        if (ticks == 0) {
            if (isWarppingSpeed()) {
                stopWarpping();
                source.sendSuccess(SkylandUtils.getTranslation("tick_warp.interrupted"), false);
            } else {
                source.sendSuccess(SkylandUtils.getTranslation("tick_warp.not_in_progress"), false);
            }
        } else if (isWarppingSpeed()) {
            ITextComponent playerName = warppingPlayer != null ? warppingPlayer.getDisplayName()
                : SkylandUtils.getTranslation("tick_warp.another_player");
            source.sendSuccess(SkylandUtils.getTranslation("tick_warp.in_progress", playerName), false);
        } else {
            totalWarpTicks = ticks;
            remainingWarpTicks = ticks;
            warpStartNanoTime = System.nanoTime();
            warppingPlayer = player;
            source.sendSuccess(SkylandUtils.getTranslation("tick_warp.start"), false);
        }
    }
    public static boolean shouldWarppingContinue() {
        if (remainingWarpTicks > 0) {
            if (remainingWarpTicks == totalWarpTicks)
                warpStartNanoTime = System.nanoTime();
            --remainingWarpTicks;
            return true;
        } else {
            stopWarpping();
            return false;
        }
    }
    private static void stopWarpping() {
        long completedWarpTicks = totalWarpTicks - remainingWarpTicks;
        double totalTime = System.nanoTime() - warpStartNanoTime;
        if (totalTime == 0) totalTime = 1;
        int tps = (int) (1e9 * completedWarpTicks / totalTime);
        double mspt = totalTime / completedWarpTicks / 1e6;
        if (warppingPlayer != null)
            warppingPlayer.sendMessage(SkylandUtils.getTranslation(
                "tick_warp.success_message", tps, mspt), Util.NIL_UUID);
        remainingWarpTicks = 0;
        warpStartNanoTime = 0;
        totalWarpTicks = 0;
    }
}
