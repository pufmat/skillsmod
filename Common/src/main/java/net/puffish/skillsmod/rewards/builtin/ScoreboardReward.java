package net.puffish.skillsmod.rewards.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.rewards.Reward;
import net.puffish.skillsmod.rewards.RewardContext;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public class ScoreboardReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("scoreboard");

	private final String objectiveName;

	private ScoreboardReward(String objectiveName) {
		this.objectiveName = objectiveName;
	}

	public static void register() {
		SkillsAPI.registerRewardWithData(
				ID,
				json -> json.getAsObject().andThen(ScoreboardReward::create)
		);
	}

	private static Result<ScoreboardReward, Error> create(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optScoreboard = rootObject.getString("scoreboard")
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new ScoreboardReward(
					optScoreboard.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public void update(ServerPlayerEntity player, RewardContext context) {
		var scoreboard = player.getScoreboard();
		var objective = scoreboard.getObjective(objectiveName);
		if (objective != null) {
			scoreboard.getPlayerScore(player.getEntityName(), objective).setScore(context.count());
		}
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
