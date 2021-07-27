package com.github.ruifengx.skylandutils.carpet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.ISuggestionProvider.suggest;

public final class TickCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> command = literal("tick")
            .then(literal("warp")
                .executes((ctxt) -> startWarp(ctxt.getSource(), 0))
                .then(argument("ticks", integer(0))
                    .suggests((ctxt, b) -> suggest(new String[]{"3600", "72000"}, b))
                    .executes((ctxt) -> startWarp(ctxt.getSource(), getInteger(ctxt, "ticks")))));
        dispatcher.register(command);
    }

    private static int startWarp(CommandSource source, long ticks) {
        ServerPlayerEntity player = null;
        try { player = source.getPlayerOrException(); } catch (CommandSyntaxException ignored) { }
        TickWarpStatus.startWarpping(player, ticks, source);
        return 1;
    }
}
