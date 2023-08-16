package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.stat.Stat;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public class StatCondition implements Condition<Stat<?>> {
	private final Stat<?> stat;

	private StatCondition(Stat<?> stat) {
		this.stat = stat;
	}

	public static ConditionFactory<Stat<?>> factory() {
		return ConditionFactory.withData(StatCondition::parse);
	}

	public static Result<StatCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(StatCondition::parse);
	}

	public static Result<StatCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optStat = rootObject.get("stat")
				.andThen(JsonParseUtils::parseStat)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new StatCondition(
					optStat.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(Stat<?> stat) {
		return this.stat.equals(stat);
	}
}
