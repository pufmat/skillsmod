package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.puffish.skillsmod.impl.rewards.RewardUpdateContextImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

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

	public static CategoryData create(GeneralConfig general) {
		return new CategoryData(
				new HashSet<>(),
				general.isUnlockedByDefault(),
				general.getStartingPoints(),
				0
		);
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

		var unlockedNbt = new NbtList();
		for (var skill : unlockedSkills) {
			unlockedNbt.add(NbtString.of(skill));
		}
		nbt.put("unlocked_skills", unlockedNbt);

		return nbt;
	}

	public Skill.State getSkillState(CategoryConfig category, SkillConfig skill, SkillDefinitionConfig definition) {
		if (unlockedSkills.contains(skill.getId())) {
			return Skill.State.UNLOCKED;
		}

		if (category.getConnections()
				.getExclusive()
				.getNeighborsFor(skill.getId())
				.map(neighbors -> neighbors.stream().anyMatch(unlockedSkills::contains))
				.orElse(false)
		) {
			return Skill.State.EXCLUDED;
		}

		if (category.getConnections()
				.getNormal()
				.getNeighborsFor(skill.getId())
				.map(neighbors -> neighbors.stream().filter(unlockedSkills::contains).count())
				.orElse(0L) >= definition.getRequiredSkills()
		) {
			return canAfford(category, definition) ? Skill.State.AFFORDABLE : Skill.State.AVAILABLE;
		}

		if (skill.isRoot()) {
			if (category.getGeneral().isExclusiveRoot() && unlockedSkills.stream()
					.flatMap(skillId -> category.getSkills().getById(skillId).stream())
					.anyMatch(SkillConfig::isRoot)
			) {
				return Skill.State.LOCKED;
			}

			return canAfford(category, definition) ? Skill.State.AFFORDABLE : Skill.State.AVAILABLE;
		}

		return Skill.State.LOCKED;
	}

	private boolean canAfford(CategoryConfig category, SkillDefinitionConfig definition) {
		return getPointsLeft(category) >= Math.max(definition.getRequiredPoints(), definition.getCost())
				&& getSpentPoints(category) >= definition.getRequiredSpentPoints();
	}

	public boolean tryUnlockSkill(CategoryConfig category, ServerPlayerEntity player, String skillId, boolean force) {
		return category.getSkills().getById(skillId).flatMap(skill -> {
			var definitionId = skill.getDefinitionId();

			return category.getDefinitions().getById(definitionId).map(definition -> {
				if (force || getSkillState(category, skill, definition) == Skill.State.AFFORDABLE) {
					unlockSkill(skillId);

					var count = countUnlocked(category, definitionId);

					for (var reward : definition.getRewards()) {
						reward.getInstance().update(new RewardUpdateContextImpl(player, count, true));
					}

					return true;
				}

				return false;
			});
		}).orElse(false);
	}

	public int countUnlocked(CategoryConfig category, String definitionId) {
		return category.getDefinitions().getById(definitionId).map(
				definition -> category.getSkills()
						.getAll()
						.stream()
						.filter(skill -> skill.getDefinitionId().equals(definitionId))
						.filter(skill -> getSkillState(category, skill, definition) == Skill.State.UNLOCKED)
						.count()
		).orElse(0L).intValue();
	}

	public void updateRewards(CategoryConfig category, ServerPlayerEntity player, Predicate<SkillRewardConfig> predicate) {
		for (var definition : category.getDefinitions().getAll()) {
			var count = countUnlocked(category, definition.getId());

			for (var reward : definition.getRewards()) {
				if (predicate.test(reward)) {
					reward.getInstance().update(new RewardUpdateContextImpl(player, count, false));
				}
			}
		}
	}

	public void unlockSkill(String id) {
		unlockedSkills.add(id);
	}

	public void lockSkill(String id) {
		unlockedSkills.remove(id);
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

	public int getRequiredExperience(CategoryConfig category, int level) {
		return category.getExperience()
				.map(experience -> experience.getExperiencePerLevel().getFunction().apply(level))
				.orElse(0);
	}

	public int getRequiredTotalExperience(CategoryConfig category, int level) {
		return category.getExperience()
				.map(experience -> experience.getRequiredTotalExperience(level))
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
		return Math.min(getEarnedPoints(category), category.getGeneral().getSpentPointsLimit()) - getSpentPoints(category);
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
