package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SkillsConfig {
	private final Map<String, SkillConfig> skills;

	private SkillsConfig(Map<String, SkillConfig> skills) {
		this.skills = skills;
	}

	public static Result<SkillsConfig, Failure> parse(JsonElementWrapper rootElement, SkillDefinitionsConfig definitions) {
		return rootElement.getAsObject().andThen(rootObject -> SkillsConfig.parse(rootObject, definitions));
	}

	public static Result<SkillsConfig, Failure> parse(JsonObjectWrapper rootObject, SkillDefinitionsConfig definitions) {
		return rootObject.getAsMap((key, value) -> SkillConfig.parse(key, value, definitions))
				.mapFailure(failures -> (Failure) ManyFailures.ofList(failures.values()))
				.mapSuccess(SkillsConfig::new);
	}

	public Optional<SkillConfig> getById(String id) {
		return Optional.ofNullable(skills.get(id));
	}

	public Collection<SkillConfig> getAll() {
		return skills.values();
	}
}
