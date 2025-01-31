package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.util.PointSources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class CategoryData {
	private final Set<String> unlockedSkills;
	private final Map<Identifier, Integer> points;
	private boolean unlocked;
	private int earnedExperience;

	private CategoryData(Set<String> unlockedSkills, Map<Identifier, Integer> points, boolean unlocked, int earnedExperience) {
		this.unlockedSkills = unlockedSkills;
		this.points = points;
		this.unlocked = unlocked;
		this.earnedExperience = earnedExperience;
	}

	public static CategoryData create(GeneralConfig general) {
		var points = new HashMap<Identifier, Integer>();
		points.put(PointSources.STARTING, general.getStartingPoints());

		return new CategoryData(
				new HashSet<>(),
				points,
				general.isUnlockedByDefault(),
				0
		);
	}

	public static CategoryData read(NbtCompound nbt) {
		var unlocked = nbt.getBoolean("unlocked");
		var experience = nbt.getInt("experience");

		var unlockedSkills = new HashSet<String>();
		var unlockedNbt = nbt.getList("unlocked_skills", NbtElement.STRING_TYPE);
		for (var elementNbt : unlockedNbt) {
			if (elementNbt instanceof NbtString stringNbt) {
				unlockedSkills.add(stringNbt.asString());
			}
		}

		var points = new HashMap<Identifier, Integer>();
		var pointsNbt = nbt.get("points");
		if (pointsNbt instanceof NbtInt pointsNbtInt) {
			points.put(PointSources.LEGACY, pointsNbtInt.intValue());
		} else if (pointsNbt instanceof NbtCompound pointsNbtCompound) {
			for (var key : pointsNbtCompound.getKeys()) {
				points.put(new Identifier(key), pointsNbtCompound.getInt(key));
			}
		}

		return new CategoryData(unlockedSkills, points, unlocked, experience);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putBoolean("unlocked", unlocked);
		nbt.putInt("experience", earnedExperience);

		var unlockedNbt = new NbtList();
		for (var skill : unlockedSkills) {
			unlockedNbt.add(NbtString.of(skill));
		}
		nbt.put("unlocked_skills", unlockedNbt);

		var pointsNbt = new NbtCompound();
		for (var entry : points.entrySet()) {
			if (entry.getValue() != 0) {
				pointsNbt.putInt(entry.getKey().toString(), entry.getValue());
			}
		}
		nbt.put("points", pointsNbt);

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

	public boolean canUnlockSkill(CategoryConfig category, SkillConfig skill) {
		var definitionId = skill.getDefinitionId();

		return category.getDefinitions()
				.getById(definitionId)
				.map(definition -> getSkillState(category, skill, definition) == Skill.State.AFFORDABLE)
				.orElse(false);
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
		return getPointsForExperience(category) + getPointsTotal();
	}

	public int getPointsTotal() {
		var total = 0;
		for (var count : points.values()) {
			total += count;
		}
		return total;
	}

	public int getPointsLeft(CategoryConfig category) {
		return Math.min(getEarnedPoints(category), category.getGeneral().getSpentPointsLimit()) - getSpentPoints(category);
	}

	public void addPoints(Identifier source, int count) {
		points.compute(source, (key, value) -> (value == null ? 0 : value) + count);
	}

	public int getPoints(Identifier source) {
		return points.getOrDefault(source, 0);
	}

	public void setPoints(Identifier source, int points) {
		this.points.put(source, points);
	}

	public Stream<Identifier> getPointsSources() {
		return this.points.entrySet().stream().filter(e -> e.getValue() != 0).map(Map.Entry::getKey);
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	public void unlock() {
		unlocked = true;
	}

	public void lock() {
		unlocked = false;
	}
}
