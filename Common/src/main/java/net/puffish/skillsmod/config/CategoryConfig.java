package net.puffish.skillsmod.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public class CategoryConfig {
	private final Identifier id;
	private final GeneralConfig general;
	private final SkillDefinitionsConfig definitions;
	private final SkillsConfig skills;
	private final SkillConnectionsConfig connections;
	private final Optional<ExperienceConfig> optExperience;

	private CategoryConfig(
			Identifier id,
			GeneralConfig general,
			SkillDefinitionsConfig definitions,
			SkillsConfig skills,
			SkillConnectionsConfig connections,
			Optional<ExperienceConfig> optExperience
	) {
		this.id = id;
		this.general = general;
		this.definitions = definitions;
		this.skills = skills;
		this.connections = connections;
		this.optExperience = optExperience;
	}

	public static Result<CategoryConfig, Failure> parse(
			Identifier id,
			JsonElementWrapper generalElement,
			JsonElementWrapper definitionsElement,
			JsonElementWrapper skillsElement,
			JsonElementWrapper connectionsElement,
			Optional<JsonElementWrapper> optExperienceElement,
			ConfigContext context
	) {
		var failures = new ArrayList<Failure>();

		var optGeneral = GeneralConfig.parse(generalElement)
				.ifFailure(failures::add)
				.getSuccess();

		var optExperience = optExperienceElement
				.flatMap(experience -> ExperienceConfig.parse(experience, context)
						.ifFailure(failures::add)
						.getSuccess()
						.flatMap(Function.identity())
				);

		var optDefinitions = SkillDefinitionsConfig.parse(definitionsElement, context)
				.ifFailure(failures::add)
				.getSuccess();

		var optSkills = optDefinitions.flatMap(
				definitions -> SkillsConfig.parse(skillsElement, definitions)
						.ifFailure(failures::add)
						.getSuccess()
		);

		var optConnections = optSkills.flatMap(
				skills -> SkillConnectionsConfig.parse(connectionsElement, skills)
						.ifFailure(failures::add)
						.getSuccess()
		);

		if (failures.isEmpty()) {
			return Result.success(new CategoryConfig(
					id,
					optGeneral.orElseThrow(),
					optDefinitions.orElseThrow(),
					optSkills.orElseThrow(),
					optConnections.orElseThrow(),
					optExperience
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	public void dispose(MinecraftServer server) {
		definitions.dispose(server);
		optExperience.ifPresent(experience -> experience.dispose(server));
	}

	public Identifier getId() {
		return id;
	}

	public GeneralConfig getGeneral() {
		return general;
	}

	public SkillDefinitionsConfig getDefinitions() {
		return definitions;
	}

	public SkillsConfig getSkills() {
		return skills;
	}

	public SkillConnectionsConfig getConnections() {
		return connections;
	}

	public Optional<ExperienceConfig> getExperience() {
		return optExperience;
	}
}
