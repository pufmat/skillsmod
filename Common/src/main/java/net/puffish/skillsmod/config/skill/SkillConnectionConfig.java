package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.json.JsonArrayWrapper;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;

public class SkillConnectionConfig {
	private final String skillAId;
	private final String skillBId;

	private SkillConnectionConfig(String skillAId, String skillBId) {
		this.skillAId = skillAId;
		this.skillBId = skillBId;
	}

	public static Result<SkillConnectionConfig, Failure> parse(JsonElementWrapper rootElement, SkillsConfig skills) {
		return rootElement.getAsArray()
				.andThen(rootArray -> SkillConnectionConfig.parse(rootArray, skills));
	}

	private static Result<SkillConnectionConfig, Failure> parse(JsonArrayWrapper rootArray, SkillsConfig skills) {
		if (rootArray.getSize() != 2) {
			return Result.failure(rootArray.getPath().createFailure("Expected an array of 2 elements"));
		}

		var failures = new ArrayList<Failure>();

		var optIds = rootArray.getAsList((i, element) -> element.getAsString().andThen(id -> {
					if (skills.getById(id).isEmpty()) {
						return Result.failure(
								element.getPath().createFailure("Expected a valid skill")
						);
					} else {
						return Result.success(id);
					}
				}))
				.ifFailure(failures::addAll)
				.getSuccess();

		if (failures.isEmpty()) {
			var ids = optIds.orElseThrow();
			return Result.success(new SkillConnectionConfig(
					ids.get(0),
					ids.get(1)
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	public String getSkillAId() {
		return skillAId;
	}

	public String getSkillBId() {
		return skillBId;
	}
}
