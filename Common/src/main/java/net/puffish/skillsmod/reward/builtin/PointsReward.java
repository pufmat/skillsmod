package net.puffish.skillsmod.reward.builtin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardConfigContext;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;

public class PointsReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("points");
	private static final String PREFIX = "points_reward.";

	private final Identifier categoryId;
	private final int points;
	private final Identifier source;

	private PointsReward(Identifier categoryId, int points, Identifier source) {
		this.categoryId = categoryId;
		this.points = points;
		this.source = source;
	}

	public static void register() {
		SkillsAPI.registerReward(ID, PointsReward::parse);
	}

	private static Result<PointsReward, Problem> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.noUnused(PointsReward::parse));
	}

	private static Result<PointsReward, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optCategory = rootObject.get("category")
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		var optPoints = rootObject.getInt("points")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new PointsReward(
					optCategory.orElseThrow(),
					optPoints.orElseThrow(),
					SkillsMod.createIdentifier(PREFIX + RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789"))
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public static void cleanup(ServerPlayerEntity player) {
		SkillsAPI.streamCategories().forEach(category -> {
			var sources = category.streamPointsSources(player)
					.filter(source -> source.getNamespace().equals(SkillsAPI.MOD_ID))
					.filter(source -> source.getPath().startsWith(PREFIX))
					.toList();
			// Modify points after the stream is completed.
			for (var source : sources) {
				category.setPoints(player, source, 0);
			};
		});
	}

	@Override
	public void update(RewardUpdateContext context) {
		SkillsAPI.getCategory(categoryId).ifPresent(category -> {
			if (context.isAction()) {
				category.setPoints(context.getPlayer(), source, points * context.getCount());
			} else {
				category.setPointsSilently(context.getPlayer(), source, points * context.getCount());
			}
		});
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		SkillsAPI.getCategory(categoryId).ifPresent(category -> {
			context.getServer()
					.getPlayerManager()
					.getPlayerList()
					.forEach(player -> category.setPoints(player, source, 0));
		});
	}

}
