package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.json.JsonArrayWrapper;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.List;

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

	public static Result<SkillConnectionConfig, Failure> parse(JsonArrayWrapper rootArray, SkillsConfig skills) {
		if (rootArray.getSize() != 2) {
			return Result.failure(rootArray.getPath().failureAt("Expected an array of 2 elements"));
		}

		var failures = new ArrayList<Failure>();

		var optIds = rootArray.getAsList((i, element) -> element.getAsString().andThen(id -> {
					if (skills.getById(id).isEmpty()) {
						return Result.failure(
								element.getPath().failureAt("Expected a valid skill")
						);
					} else {
						return Result.success(id);
					}
				}))
				.ifFailure(failures::addAll)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(build(
					optIds.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	private static SkillConnectionConfig build(List<String> ids) {
		return new SkillConnectionConfig(
				ids.get(0),
				ids.get(1)
		);
	}

	public String getSkillAId() {
		return skillAId;
	}

	public String getSkillBId() {
		return skillBId;
	}
}
