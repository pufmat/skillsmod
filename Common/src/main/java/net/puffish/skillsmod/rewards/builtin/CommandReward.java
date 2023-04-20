package net.puffish.skillsmod.rewards.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.rewards.Reward;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.Objects;

public class CommandReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("command");

	private final String command;

	private CommandReward(String command) {
		this.command = command;
	}

	public static void register() {
		SkillsAPI.registerReward(ID, CommandReward::create);
	}

	private static Result<CommandReward, Error> create(Result<JsonElementWrapper, Error> maybeDataElement) {
		return maybeDataElement.andThen(CommandReward::create);
	}

	private static Result<CommandReward, Error> create(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(CommandReward::create);
	}

	private static Result<CommandReward, Error> create(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optCommand = rootObject.getString("command")
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new CommandReward(
					optCommand.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public void update(ServerPlayerEntity player, int count, boolean recent) {
		if (recent) {
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
