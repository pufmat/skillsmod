package net.puffish.skillsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.util.CommandUtils;

public class OpenCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("open")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.argument("players", EntityArgumentType.players())
						.executes(context -> {
							var players = EntityArgumentType.getPlayers(context, "players");

							for (var player : players) {
								SkillsAPI.openScreen(player);
							}
							CommandUtils.sendSuccess(
									context,
									players,
									"open"
							);
							return players.size();
						})
				);
	}
}
