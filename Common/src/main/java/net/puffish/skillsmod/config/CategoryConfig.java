package net.puffish.skillsmod.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.rewards.RewardContext;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.skill.SkillState;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public class CategoryConfig {
	private final Identifier id;
	private final GeneralConfig general;
	private final SkillDefinitionsConfig definitions;
	private final SkillsConfig skills;
	private final SkillConnectionsConfig connections;
	private final ExperienceConfig experience;

	private CategoryConfig(Identifier id, GeneralConfig general, SkillDefinitionsConfig definitions, SkillsConfig skills, SkillConnectionsConfig connections, ExperienceConfig experience) {
		this.id = id;
		this.general = general;
		this.definitions = definitions;
		this.skills = skills;
		this.connections = connections;
		this.experience = experience;
	}

	public static Result<CategoryConfig, Failure> parse(
			Identifier id,
			JsonElementWrapper generalElement,
			JsonElementWrapper definitionsElement,
			JsonElementWrapper skillsElement,
			JsonElementWrapper connectionsElement,
			JsonElementWrapper experienceElement,
			ConfigContext context
	) {
		var failures = new ArrayList<Failure>();

		var optGeneral = GeneralConfig.parse(generalElement)
				.ifFailure(failures::add)
				.getSuccess();

		var optExperience = ExperienceConfig.parse(experienceElement, context)
				.ifFailure(failures::add)
				.getSuccess();

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
					optExperience.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public void dispose(MinecraftServer server) {
		definitions.dispose(server);
		experience.dispose(server);
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

	public ExperienceConfig getExperience() {
		return experience;
	}
}
