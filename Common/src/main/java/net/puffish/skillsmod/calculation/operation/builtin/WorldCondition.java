package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class WorldCondition implements Operation<ServerLevel, Boolean> {
	private final Identifier dimension;

	private WorldCondition(Identifier dimension) {
		this.dimension = dimension;
	}

	public static void register() {
		BuiltinPrototypes.LEVEL.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				WorldCondition::parse
		);
	}

	public static Result<WorldCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.noUnused(WorldCondition::parse));
	}

	public static Result<WorldCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optDimension = rootObject.get("dimension")
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new WorldCondition(
					optDimension.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(ServerLevel world) {
		return Optional.of(world.dimension().identifier().equals(dimension));
	}
}
