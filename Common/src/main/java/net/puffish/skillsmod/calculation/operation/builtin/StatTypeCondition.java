package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.StatType;
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

public class StatTypeCondition implements Operation<StatType<?>, Boolean> {

	private final HolderSet<StatType<?>> statTypeEntries;

	private StatTypeCondition(HolderSet<StatType<?>> statTypeEntries) {
		this.statTypeEntries = statTypeEntries;
	}

	public static void register() {
		BuiltinPrototypes.STAT_TYPE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				StatTypeCondition::parse
		);
	}

	public static Result<StatTypeCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(StatTypeCondition::parse, context));
	}

	public static Result<StatTypeCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optStatType = rootObject.get("stat")
				.andThen(BuiltinJson::parseStatTypeOrStatTypeTag)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new StatTypeCondition(
					optStatType.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(StatType<?> statType) {
		return Optional.of(statTypeEntries.contains(BuiltInRegistries.STAT_TYPE.wrapAsHolder(statType)));
	}
}
