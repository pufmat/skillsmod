package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SkillsConfig {
	private final Set<String> valid;
	private final Map<String, SkillConfig> skills;

	public SkillsConfig(Set<String> valid, Map<String, SkillConfig> skills) {
		this.valid = valid;
		this.skills = skills;
	}

	public static Result<SkillsConfig, Problem> parse(JsonElement rootElement, SkillDefinitionsConfig definitions, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> SkillsConfig.parse(rootObject, definitions, context));
	}

	public static Result<SkillsConfig, Problem> parse(JsonObject rootObject, SkillDefinitionsConfig definitions, ConfigContext context) {
		return rootObject.getAsMap((key, value) -> SkillConfig.parse(key, value, definitions, context))
				.mapFailure(problems -> Problem.combine(problems.values()))
				.mapSuccess(map -> new SkillsConfig(
						map.keySet(),
						map.entrySet()
								.stream()
								.flatMap(entry -> entry.getValue()
										.map(value -> Map.entry(entry.getKey(), value))
										.stream()
								)
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
				));
	}

	public boolean isValid(String id) {
		return valid.contains(id);
	}

	public boolean isLoaded(String id) {
		return skills.containsKey(id);
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
