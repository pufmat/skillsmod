package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public class StatValueOperation implements Operation<ServerPlayerEntity, Double> {
	private final Stat<?> stat;

	private StatValueOperation(Stat<?> stat) {
		this.stat = stat;
	}

	public static void register() {
		BuiltinPrototypes.PLAYER.registerOperation(
				SkillsMod.createIdentifier("get_stat_value"),
				BuiltinPrototypes.NUMBER,
				StatValueOperation::parse
		);
	}

	public static Result<StatValueOperation, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(StatValueOperation::parse, context));
	}

	public static Result<StatValueOperation, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optStat = rootObject.get("stat")
				.andThen(BuiltinJson::parseStat)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new StatValueOperation(
					optStat.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Double> apply(ServerPlayerEntity player) {
		return Optional.of((double) player.getStatHandler().getStat(stat));
	}
}
