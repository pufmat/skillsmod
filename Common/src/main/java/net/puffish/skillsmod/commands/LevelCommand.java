package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.commands.arguments.CategoryArgumentType;
import net.puffish.skillsmod.util.CommandUtils;

import java.util.function.BiConsumer;

public class LevelCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("level")
				.requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithLevel())
										.then(CommandManager.argument("count", IntegerArgumentType.integer())
												.executes(LevelCommand::add)
										)
								)
						)
				)
				.then(CommandManager.literal("set")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithLevel())
										.then(CommandManager.argument("count", IntegerArgumentType.integer())
												.executes(LevelCommand::set)
										)
								)
						)
				)
				.then(CommandManager.literal("get")
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.then(CommandManager.argument("category", CategoryArgumentType.categoryOnlyWithLevel())
										.executes(LevelCommand::get)
								)
						)
				);
	}

	private static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategoryOnlyWithLevel(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");

		var addLevel = category.getExperience()
				.map(experience -> (BiConsumer<ServerPlayerEntity, Integer>) experience::addLevel)
				.orElseGet(() -> category.getExchange()
						.map(exchange -> (BiConsumer<ServerPlayerEntity, Integer>) exchange::addLevel)
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

	private static int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(context, "players");
		var category = CategoryArgumentType.getCategoryOnlyWithLevel(context, "category");
		var count = IntegerArgumentType.getInteger(context, "count");

		var setLevel = category.getExperience()
				.map(experience -> (BiConsumer<ServerPlayerEntity, Integer>) experience::setLevel)
				.orElseGet(() -> category.getExchange()
						.map(exchange -> (BiConsumer<ServerPlayerEntity, Integer>) exchange::setLevel)
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

	private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var player = EntityArgumentType.getPlayer(context, "player");
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
