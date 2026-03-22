package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;
import net.puffish.skillsmod.util.PointSources;

public class PointsCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> create() {
		return Commands.literal("points")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("add")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.then(Commands.argument("count", IntegerArgumentType.integer())
												.executes(PointsCommand::addTotal)
												.then(Commands.argument("source", IdentifierArgument.id())
														.executes(PointsCommand::add)
												)
										)
								)
						)
				)
				.then(Commands.literal("set")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.then(Commands.argument("count", IntegerArgumentType.integer())
												.executes(PointsCommand::setTotal)
												.then(Commands.argument("source", IdentifierArgument.id())
														.executes(PointsCommand::set)
												)
										)
								)
						)
				)
				.then(Commands.literal("get")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("category", CategoryArgumentType.category())
										.executes(PointsCommand::getTotal)
										.then(Commands.argument("source", IdentifierArgument.id())
												.executes(PointsCommand::get)
										)
								)
						)
				);
	}

	private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");
		var source = IdentifierArgument.getId(context, "source");

		for (var player : players) {
			category.addPoints(player, source, count);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"points.add",
				count,
				category.getId()
		);
		return players.size();
	}

	private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");
		var source = IdentifierArgument.getId(context, "source");

		for (var player : players) {
			category.setPoints(player, source, count);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"points.set",
				count,
				category.getId()
		);
		return players.size();
	}

	private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var player = EntityArgument.getPlayer(context, "player");
		var category = CategoryArgumentType.getCategory(context, "category");
		var source = IdentifierArgument.getId(context, "source");

		var count = category.getPoints(player, source);
		CommandUtils.sendSuccess(
				context,
				player,
				"points.get",
				count,
				category.getId()
		);
		return count;
	}

	private static int addTotal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");

		for (var player : players) {
			category.addPoints(player, PointSources.COMMANDS, count);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"points.add",
				count,
				category.getId()
		);
		return players.size();
	}

	private static int setTotal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategory(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");

		for (var player : players) {
			category.addPoints(player, PointSources.COMMANDS, count - category.getPointsTotal(player));
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"points.set",
				count,
				category.getId()
		);
		return players.size();
	}

	private static int getTotal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var player = EntityArgument.getPlayer(context, "player");
		var category = CategoryArgumentType.getCategory(context, "category");

		var count = category.getPointsTotal(player);
		CommandUtils.sendSuccess(
				context,
				player,
				"points.get",
				count,
				category.getId()
		);
		return count;
	}
}
