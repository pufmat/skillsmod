package net.puffish.skillsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

public class CategoryCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> create() {
		return Commands.literal("category")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("lock")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.executes(CategoryCommand::lock)
								)
						)
				)
				.then(Commands.literal("unlock")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.executes(CategoryCommand::unlock)
								)
						)
				)
				.then(Commands.literal("erase")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.executes(CategoryCommand::erase)
								)
						)
				)
				.then(Commands.literal("open")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.executes(CategoryCommand::open)
								)
						)
				);
	}

	private static int lock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");

		for (var player : players) {
			category.lock(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"category.lock",
				category.getId()
		);
		return players.size();
	}

	private static int unlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");

		for (var player : players) {
			category.unlock(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"category.unlock",
				category.getId()
		);
		return players.size();
	}

	private static int erase(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");

		for (var player : players) {
			category.erase(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"category.erase",
				category.getId()
		);
		return players.size();
	}

	private static int open(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");

		for (var player : players) {
			category.openScreen(player);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"category.open",
				category.getId()
		);
		return players.size();
	}
}
