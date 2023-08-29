package net.puffish.skillsmod.rewards.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.rewards.Reward;
import net.puffish.skillsmod.rewards.RewardContext;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CommandReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("command");

	private final Map<UUID, Integer> counts = new HashMap<>();

	private final String command;
	private final String unlockCommand;
	private final String lockCommand;

	private CommandReward(String command, String unlockCommand, String lockCommand) {
		this.command = command;
		this.unlockCommand = unlockCommand;
		this.lockCommand = lockCommand;
	}

	public static void register() {
		SkillsAPI.registerRewardWithData(
				ID,
				CommandReward::create
		);
	}

	private static Result<CommandReward, Failure> create(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(CommandReward::create);
	}

	private static Result<CommandReward, Failure> create(JsonObjectWrapper rootObject) {
		var command = rootObject.getString("command")
				.getSuccess()
				.orElse("");

		var unlockCommand = rootObject.getString("unlock_command")
				.getSuccess()
				.orElse("");

		var lockCommand = rootObject.getString("lock_command")
				.getSuccess()
				.orElse("");

		return Result.success(new CommandReward(
				command,
				unlockCommand,
				lockCommand
		));
	}

	private void executeCommand(ServerPlayerEntity player, String command) {
		if (command.isBlank()) {
			return;
		}

		var server = Objects.requireNonNull(player.getServer());

		server.getCommandManager().executeWithPrefix(
				player.getCommandSource()
						.withSilent()
						.withLevel(server.getFunctionPermissionLevel()),
				command
		);
	}

	@Override
	public void update(ServerPlayerEntity player, RewardContext context) {
		if (context.recent()) {
			executeCommand(player, command);
		}

		counts.compute(player.getUuid(), (uuid, count) -> {
			if (count == null) {
				count = 0;
			}

			while (context.count() > count) {
				executeCommand(player, unlockCommand);
				count++;
			}
			while (context.count() < count) {
				executeCommand(player, lockCommand);
				count--;
			}

			return count;
		});
	}

	@Override
	public void dispose(MinecraftServer server) {
		for (var entry : counts.entrySet()) {
			var player = server.getPlayerManager().getPlayer(entry.getKey());
			if (player == null) {
				continue;
			}
			for (var i = 0; i < entry.getValue(); i++) {
				executeCommand(player, lockCommand);
			}
		}
		counts.clear();
	}
}
