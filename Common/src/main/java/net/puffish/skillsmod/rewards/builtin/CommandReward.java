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
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.Objects;

public class CommandReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("command");

	private final String command;

	private CommandReward(String command) {
		this.command = command;
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
		var failures = new ArrayList<Failure>();

		var optCommand = rootObject.getString("command")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new CommandReward(
					optCommand.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public void update(ServerPlayerEntity player, RewardContext context) {
		if (context.recent()) {
			var server = Objects.requireNonNull(player.getServer());

			server.getCommandManager().executeWithPrefix(
					player.getCommandSource()
							.withSilent()
							.withLevel(server.getFunctionPermissionLevel()),
					command
			);
		}
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
