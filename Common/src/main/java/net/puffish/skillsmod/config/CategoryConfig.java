package net.puffish.skillsmod.config;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.util.DisposeContext;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public record CategoryConfig(
		Identifier id,
		GeneralConfig general,
		SkillDefinitionsConfig definitions,
		SkillsConfig skills,
		SkillConnectionsConfig connections,
		Optional<ExperienceConfig> experience
) {

	public static Result<CategoryConfig, Problem> parse(
			Identifier id,
			JsonElement generalElement,
			JsonElement definitionsElement,
			JsonElement skillsElement,
			JsonElement connectionsElement,
			Optional<JsonElement> optExperienceElement,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var optGeneral = GeneralConfig.parse(generalElement, context)
				.ifFailure(problems::add)
				.getSuccess();

		var optExperience = optExperienceElement
				.flatMap(experience -> ExperienceConfig.parse(experience, context)
						.ifFailure(problems::add)
						.getSuccess()
						.flatMap(Function.identity())
				);

		var optDefinitions = SkillDefinitionsConfig.parse(definitionsElement, context)
				.ifFailure(problems::add)
				.getSuccess();

		var optSkills = optDefinitions.flatMap(
				definitions -> SkillsConfig.parse(skillsElement, definitions, context)
						.ifFailure(problems::add)
						.getSuccess()
		);

		var optConnections = optSkills.flatMap(
				skills -> SkillConnectionsConfig.parse(connectionsElement, skills, context)
						.ifFailure(problems::add)
						.getSuccess()
		);

		if (problems.isEmpty()) {
			return Result.success(new CategoryConfig(
					id,
					optGeneral.orElseThrow(),
					optDefinitions.orElseThrow(),
					optSkills.orElseThrow(),
					optConnections.orElseThrow(),
					optExperience
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public void dispose(DisposeContext context) {
		definitions.dispose(context);
		experience.ifPresent(experience -> experience.dispose(context));
	}

}
