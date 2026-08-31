package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

import java.util.function.BiConsumer;

public class LevelCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> create() {
		return Commands.literal("level")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("add")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.categoryOnlyWithLevel())
										.then(Commands.argument("count", IntegerArgumentType.integer())
												.executes(LevelCommand::add)
										)
								)
						)
				)
				.then(Commands.literal("set")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("category", CategoryArgumentType.categoryOnlyWithLevel())
										.then(Commands.argument("count", IntegerArgumentType.integer())
												.executes(LevelCommand::set)
										)
								)
						)
				)
				.then(Commands.literal("get")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("category", CategoryArgumentType.categoryOnlyWithLevel())
										.executes(LevelCommand::get)
								)
						)
				);
	}

	private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategoryOnlyWithLevel(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");

		var addLevel = category.getExperience()
				.map(experience -> (BiConsumer<ServerPlayer, Integer>) experience::addLevel)
				.orElseGet(() -> category.getExchange()
						.map(exchange -> (BiConsumer<ServerPlayer, Integer>) exchange::addLevel)
						.orElseThrow());

		for (var player : players) {
			addLevel.accept(player, count);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"level.add",
				count,
				category.getId()
		);
		return players.size();
	}

	private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategoryOnlyWithLevel(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");

		var setLevel = category.getExperience()
				.map(experience -> (BiConsumer<ServerPlayer, Integer>) experience::setLevel)
				.orElseGet(() -> category.getExchange()
						.map(exchange -> (BiConsumer<ServerPlayer, Integer>) exchange::setLevel)
						.orElseThrow());

		for (var player : players) {
			setLevel.accept(player, count);
		}
		CommandUtils.sendSuccess(
				context,
				players,
				"level.set",
				count,
				category.getId()
		);
		return players.size();
	}

	private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var player = EntityArgument.getPlayer(context, "player");
		var category = CategoryArgumentType.getCategoryOnlyWithLevel(context, "category");

		var level = category.getExperience()
				.map(experience -> experience.getLevel(player))
				.orElseGet(() -> category.getExchange()
						.map(exchange -> exchange.getLevel(player))
						.orElseThrow());

		CommandUtils.sendSuccess(
				context,
				player,
				"level.get",
				level,
				category.getId()
		);
		return level;
	}
}
