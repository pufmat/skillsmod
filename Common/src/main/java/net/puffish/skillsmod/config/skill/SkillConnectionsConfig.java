package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.json.JsonArrayWrapper;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public class SkillConnectionsConfig {
	private final SkillConnectionsGroupConfig normal;

	private SkillConnectionsConfig(SkillConnectionsGroupConfig normal) {
		this.normal = normal;
	}

	public static Result<SkillConnectionsConfig, Failure> parse(JsonElementWrapper rootElement, SkillsConfig skills) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, skills))
				.orElse(failure -> rootElement.getAsArray()
						.andThen(rootArray -> parseLegacy(rootArray, skills))
				);
	}

	private static Result<SkillConnectionsConfig, Failure> parse(JsonObjectWrapper rootObject, SkillsConfig skills) {
		var failures = new ArrayList<Failure>();

		var normal = rootObject.get("normal")
				.getSuccess()
				.flatMap(element -> SkillConnectionsGroupConfig.parse(element, skills)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(SkillConnectionsGroupConfig::empty);

		if (failures.isEmpty()) {
			return Result.success(new SkillConnectionsConfig(
					normal
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	private static Result<SkillConnectionsConfig, Failure> parseLegacy(JsonArrayWrapper rootArray, SkillsConfig skills) {
		return SkillConnectionsGroupConfig.parseLegacy(rootArray, skills)
				.mapSuccess(SkillConnectionsConfig::new);
	}

	public SkillConnectionsGroupConfig getNormal() {
		return normal;
	}
}