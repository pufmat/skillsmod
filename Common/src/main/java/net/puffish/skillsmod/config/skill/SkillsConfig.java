package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SkillsConfig {
	private final Map<String, SkillConfig> skills;

	public SkillsConfig(Map<String, SkillConfig> skills) {
		this.skills = skills;
	}

	public static Result<SkillsConfig, Problem> parse(JsonElement rootElement, SkillDefinitionsConfig definitions, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> SkillsConfig.parse(rootObject, definitions, context));
	}

	public static Result<SkillsConfig, Problem> parse(JsonObject rootObject, SkillDefinitionsConfig definitions, ConfigContext context) {
		return rootObject.getAsMap((key, value) -> SkillConfig.parse(key, value, definitions, context))
				.mapFailure(problems -> Problem.combine(problems.values()))
				.mapSuccess(SkillsConfig::new);
	}

	public Optional<SkillConfig> getById(String id) {
		return Optional.ofNullable(skills.get(id));
	}

	public Collection<SkillConfig> getAll() {
		return skills.values();
	}

	public Map<String, SkillConfig> getMap() {
		return skills;
	}
}
