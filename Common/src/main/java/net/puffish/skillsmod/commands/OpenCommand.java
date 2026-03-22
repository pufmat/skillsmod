package net.puffish.skillsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.util.CommandUtils;

public class OpenCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> create() {
		return Commands.literal("open")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.argument("players", EntityArgument.players())
						.executes(OpenCommand::open)
				);
	}

	private static int open(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");

		for (var player : players) {
			SkillsAPI.openScreen(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"open"
		);
		return players.size();
	}
}
