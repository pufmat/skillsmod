package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.rewards.RewardContext;
import net.puffish.skillsmod.skill.SkillState;

import java.util.HashSet;
import java.util.Set;

public class CategoryData {
	private final Set<String> unlockedSkills;

	private boolean unlocked;
	private int extraPoints;
	private int earnedExperience;

	private CategoryData(Set<String> unlockedSkills, boolean unlocked, int extraPoints, int earnedExperience) {
		this.unlockedSkills = unlockedSkills;
		this.unlocked = unlocked;
		this.extraPoints = extraPoints;
		this.earnedExperience = earnedExperience;
	}

	public static CategoryData create(boolean unlocked) {
		return new CategoryData(new HashSet<>(), unlocked, 0, 0);
	}

	public static CategoryData read(NbtCompound nbt) {
		var unlocked = nbt.getBoolean("unlocked");
		var points = nbt.getInt("points");
		var experience = nbt.getInt("experience");

		var unlockedSkills = new HashSet<String>();
		var unlockedNbt = nbt.getList("unlocked_skills", NbtElement.STRING_TYPE);
		for (var elementNbt : unlockedNbt) {
			if (elementNbt instanceof NbtString stringNbt) {
				unlockedSkills.add(stringNbt.asString());
			}
		}

		return new CategoryData(unlockedSkills, unlocked, points, experience);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putBoolean("unlocked", unlocked);
		nbt.putInt("points", extraPoints);
		nbt.putInt("experience", earnedExperience);

		NbtList unlockedNbt = new NbtList();
		for (var skill : unlockedSkills) {
			unlockedNbt.add(NbtString.of(skill));
		}
		nbt.put("unlocked_skills", unlockedNbt);

		return nbt;
	}

	public SkillState getSkillState(CategoryConfig category, SkillConfig skill) {
		if (unlockedSkills.contains(skill.getId())) {
			return SkillState.UNLOCKED;
		}

		var neighborIds = category.getConnections().getNeighbors().get(skill.getId());
		if (neighborIds == null || neighborIds.stream().anyMatch(unlockedSkills::contains)) {
			return SkillState.AVAILABLE;
		}

		if (skill.isRoot()) {
			if (!category.getGeneral().isExclusiveRoot()) {
				return SkillState.AVAILABLE;
			}
			if (unlockedSkills.stream()
					.flatMap(skillId -> category.getSkills().getById(skillId).stream())
					.noneMatch(SkillConfig::isRoot)) {
				return SkillState.AVAILABLE;
			}
		}

		return SkillState.LOCKED;
	}

	public boolean tryUnlockSkill(CategoryConfig category, ServerPlayerEntity player, String skillId, boolean force) {
		return category.getSkills().getById(skillId).flatMap(skill -> {
			var definitionId = skill.getDefinitionId();

			return category.getDefinitions().getById(definitionId).map(definition -> {
				if (force) {
					addExtraPoints(definition.getCost());
				} else {
					if (getSkillState(category, skill) != SkillState.AVAILABLE) {
						return false;
					}

					if (getPointsLeft(category) < Math.max(definition.getRequiredPoints(), definition.getCost())) {
						return false;
					}

					if (getSpentPoints(category) < definition.getRequiredSpentPoints()) {
						return false;
					}
				}

				unlockSkill(skillId);

				int count = countUnlocked(category, definitionId);

				for (var reward : definition.getRewards()) {
					reward.getInstance().update(player, new RewardContext(count, true));
				}

				return true;
			});
		}).orElse(false);
	}

	public int countUnlocked(CategoryConfig category, String definitionId) {
		return (int) category.getSkills()
				.getAll()
				.stream()
				.filter(skill -> skill.getDefinitionId().equals(definitionId))
				.filter(skill -> getSkillState(category, skill) == SkillState.UNLOCKED)
				.count();
	}



	public void refreshReward(CategoryConfig category, ServerPlayerEntity player, Identifier type) {
		for (var definition : category.getDefinitions().getAll()) {
			int count = countUnlocked(category, definition.getId());

			for (var reward : definition.getRewards()) {
				if (reward.getType().equals(type)) {
					reward.getInstance().update(player, new RewardContext(count, false));
				}
			}
		}
	}

	public void applyRewards(CategoryConfig category, ServerPlayerEntity player) {
		for (var definition : category.getDefinitions().getAll()) {
			int count = countUnlocked(category, definition.getId());

			for (var reward : definition.getRewards()) {
				reward.getInstance().update(player, new RewardContext(count, false));
			}
		}
	}

	public void unlockSkill(String id) {
		unlockedSkills.add(id);
	}

	public void resetSkills() {
		unlockedSkills.clear();
	}

	public void addExperience(int experience) {
		this.earnedExperience += experience;
	}

	public Set<String> getUnlockedSkillIds() {
		return unlockedSkills;
	}

	public int getEarnedExperience() {
		return earnedExperience;
	}

	public void setEarnedExperience(int earnedExperience) {
		this.earnedExperience = earnedExperience;
	}

	public int getCurrentLevel(CategoryConfig category) {
		return category.getExperience()
				.map(experience -> experience.getCurrentLevel(earnedExperience))
				.orElse(0);
	}

	public int getCurrentExperience(CategoryConfig category) {
		return category.getExperience()
				.map(experience -> experience.getCurrentExperience(earnedExperience))
				.orElse(0);
	}

	public int getRequiredExperience(CategoryConfig category) {
		return category.getExperience()
				.map(experience -> experience.getRequiredExperience(getCurrentLevel(category)))
				.orElse(0);
	}

	public int getPointsForExperience(CategoryConfig category) {
		if (category.getExperience().isPresent()) {
			return getCurrentLevel(category);
		}
		return 0;
	}

	public int getSpentPoints(CategoryConfig category) {
		return unlockedSkills.stream()
				.flatMap(skillId -> category.getSkills()
						.getById(skillId)
						.flatMap(skill -> category.getDefinitions().getById(skill.getDefinitionId()))
						.stream()
				)
				.mapToInt(SkillDefinitionConfig::getCost)
				.sum();
	}

	public int getEarnedPoints(CategoryConfig category) {
		return getExtraPoints() + getPointsForExperience(category);
	}

	public int getPointsLeft(CategoryConfig category) {
		return Math.min(getEarnedPoints(category), getSpentPointsLimit(category)) - getSpentPoints(category);
	}

	public int getSpentPointsLimit(CategoryConfig category) {
		return category.getGeneral().getSpentPointsLimit();
	}

	public void addExtraPoints(int count) {
		extraPoints += count;
	}

	public int getExtraPoints() {
		return extraPoints;
	}

	public void setExtraPoints(int points) {
		this.extraPoints = points;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}
}
