package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SkillsConfig {
	private final Map<String, SkillConfig> skills;

	private SkillsConfig(Map<String, SkillConfig> skills) {
		this.skills = skills;
	}

	public static Result<SkillsConfig, Error> parse(JsonElementWrapper rootElement, SkillDefinitionsConfig definitions) {
		return rootElement.getAsObject().andThen(rootObject -> SkillsConfig.parse(rootObject, definitions));
	}

	public static Result<SkillsConfig, Error> parse(JsonObjectWrapper rootObject, SkillDefinitionsConfig definitions) {
		return rootObject.getAsMap((key, value) -> SkillConfig.parse(key, value, definitions))
				.mapFailure(errors -> (Error) ManyErrors.ofList(errors))
				.mapSuccess(SkillsConfig::new);
	}

	public Optional<SkillConfig> getById(String id) {
		return Optional.ofNullable(skills.get(id));
	}

	public Collection<SkillConfig> getAll() {
		return skills.values();
	}
}
