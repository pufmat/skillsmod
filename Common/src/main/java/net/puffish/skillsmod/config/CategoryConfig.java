package net.puffish.skillsmod.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.rewards.RewardContext;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.skill.SkillState;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.Result;

import java.util.ArrayList;

public class CategoryConfig {
	private final String id;
	private final int index;
	private final GeneralConfig general;
	private final SkillDefinitionsConfig definitions;
	private final SkillsConfig skills;
	private final SkillConnectionsConfig connections;
	private final ExperienceConfig experience;

	private CategoryConfig(String id, int index, GeneralConfig general, SkillDefinitionsConfig definitions, SkillsConfig skills, SkillConnectionsConfig connections, ExperienceConfig experience) {
		this.id = id;
		this.index = index;
		this.general = general;
		this.definitions = definitions;
		this.skills = skills;
		this.connections = connections;
		this.experience = experience;
	}

	public static Result<CategoryConfig, Error> parse(String id, int index, JsonElementWrapper generalElement, JsonElementWrapper definitionsElement, JsonElementWrapper skillsElement, JsonElementWrapper connectionsElement, JsonElementWrapper experienceElement) {
		var errors = new ArrayList<Error>();

		var optGeneral = GeneralConfig.parse(generalElement)
				.ifFailure(errors::add)
				.getSuccess();

		var optExperience = ExperienceConfig.parse(experienceElement)
				.ifFailure(errors::add)
				.getSuccess();

		var optDefinitions = SkillDefinitionsConfig.parse(definitionsElement)
				.ifFailure(errors::add)
				.getSuccess();

		var optSkills = optDefinitions.flatMap(
				definitions -> SkillsConfig.parse(skillsElement, definitions)
						.ifFailure(errors::add)
						.getSuccess()
		);

		var optConnections = optSkills.flatMap(
				skills -> SkillConnectionsConfig.parse(connectionsElement, skills)
						.ifFailure(errors::add)
						.getSuccess()
		);

		if (errors.isEmpty()) {
			return Result.success(new CategoryConfig(
					id,
					index,
					optGeneral.orElseThrow(),
					optDefinitions.orElseThrow(),
					optSkills.orElseThrow(),
					optConnections.orElseThrow(),
					optExperience.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public boolean tryUnlockSkill(ServerPlayerEntity player, CategoryData categoryData, String skillId, boolean force) {
		return skills.getById(skillId).flatMap(skill -> {
			var definitionId = skill.getDefinitionId();

			return definitions.getById(definitionId).map(definition -> {
				if (force) {
					categoryData.addExtraPoints(definition.getCost());
				} else {
					if (skill.getStateFor(this, categoryData) != SkillState.AVAILABLE) {
						return false;
					}

					if (categoryData.getPointsLeft(this) < definition.getCost()) {
						return false;
					}
				}

				categoryData.unlockSkill(skillId);

				int count = countUnlocked(categoryData, definitionId);

				for (var reward : definition.getRewards()) {
					reward.getInstance().update(player, new RewardContext(count, true));
				}

				return true;
			});
		}).orElse(false);
	}

	public void refreshReward(ServerPlayerEntity player, CategoryData categoryData, Identifier type) {
		for (var definition : definitions.getAll()) {
			int count = countUnlocked(categoryData, definition.getId());

			for (var reward : definition.getRewards()) {
				if (reward.getType().equals(type)) {
					reward.getInstance().update(player, new RewardContext(count, false));
				}
			}
		}
	}

	public void applyRewards(ServerPlayerEntity player, CategoryData categoryData) {
		for (var definition : definitions.getAll()) {
			int count = countUnlocked(categoryData, definition.getId());

			for (var reward : definition.getRewards()) {
				reward.getInstance().update(player, new RewardContext(count, false));
			}
		}
	}

	public void resetRewards(ServerPlayerEntity player) {
		for (var definition : definitions.getAll()) {
			for (var reward : definition.getRewards()) {
				reward.getInstance().update(player, new RewardContext(0, false));
			}
		}
	}

	public int countUnlocked(CategoryData categoryData, String definitionId) {
		return (int) skills.getAll()
				.stream()
				.filter(skill ->
						skill.getDefinitionId().equals(definitionId)
						&&
						skill.getStateFor(this, categoryData) == SkillState.UNLOCKED)
				.count();
	}

	public void dispose(MinecraftServer server) {
		definitions.dispose(server);
		experience.dispose(server);
	}

	public String getId() {
		return id;
	}

	public int getIndex() {
		return index;
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
