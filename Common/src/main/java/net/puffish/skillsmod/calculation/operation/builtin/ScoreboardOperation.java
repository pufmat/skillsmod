package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.world.entity.Entity;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.calculation.LegacyBuiltinPrototypes;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class ScoreboardOperation implements Operation<Entity, Double> {
	private final String objectiveName;

	private ScoreboardOperation(String objectiveName) {
		this.objectiveName = objectiveName;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY.registerOperation(
				SkillsMod.createIdentifier("get_score"),
				BuiltinPrototypes.NUMBER,
				ScoreboardOperation::parse
		);

		LegacyBuiltinPrototypes.registerAlias(
				BuiltinPrototypes.ENTITY,
				SkillsMod.createIdentifier("scoreboard"),
				SkillsMod.createIdentifier("get_score")
		);
	}

	public static Result<ScoreboardOperation, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(ScoreboardOperation::parse, context));
	}

	public static Result<ScoreboardOperation, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optScoreboard = rootObject.getString("objective")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new ScoreboardOperation(
					optScoreboard.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Double> apply(Entity entity) {
		var scoreboard = entity.level().getScoreboard();
		return Optional.ofNullable(scoreboard.getObjective(objectiveName))
				.map(objective -> Optional.ofNullable(scoreboard.getPlayerScoreInfo(entity, objective))
						.map(score -> (double) score.value())
						.orElse(0.0));
	}
}
