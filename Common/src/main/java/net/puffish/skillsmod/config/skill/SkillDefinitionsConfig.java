package net.puffish.skillsmod.config.skill;

import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.Result;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SkillDefinitionsConfig {
	private final Map<String, SkillDefinitionConfig> definitions;

	private SkillDefinitionsConfig(Map<String, SkillDefinitionConfig> definitions) {
		this.definitions = definitions;
	}

	public static Result<SkillDefinitionsConfig, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<SkillDefinitionsConfig, Error> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		return rootObject.getAsMap((id, element) -> SkillDefinitionConfig.parse(id, element, context))
				.mapFailure(errors -> (Error) ManyErrors.ofList(errors.values()))
				.mapSuccess(SkillDefinitionsConfig::new);
	}

	public Optional<SkillDefinitionConfig> getById(String id) {
		return Optional.ofNullable(definitions.get(id));
	}

	public Collection<SkillDefinitionConfig> getAll() {
		return definitions.values();
	}

	public void dispose(MinecraftServer server) {
		for (var definition : definitions.values()) {
			definition.dispose(server);
		}
	}
}
