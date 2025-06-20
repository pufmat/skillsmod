package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.NbtCompound;
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
	private int experience;

	private CategoryData(Set<String> unlockedSkills, Map<Identifier, Integer> points, boolean unlocked, int experience) {
		this.unlockedSkills = unlockedSkills;
		this.points = points;
		this.unlocked = unlocked;
		this.experience = experience;
	}

	public static CategoryData create(GeneralConfig general) {
		var points = new HashMap<Identifier, Integer>();
		points.put(PointSources.STARTING, general.startingPoints());

		return new CategoryData(
				new HashSet<>(),
				points,
				general.unlockedByDefault(),
				0
		);
	}

	public static CategoryData read(NbtCompound nbt) {
		var unlocked = nbt.getBoolean("unlocked", false);
		var experience = nbt.getInt("experience", 0);

		var unlockedSkills = new HashSet<String>();
		var unlockedNbt = nbt.getListOrEmpty("unlocked_skills");
		for (var elementNbt : unlockedNbt) {
			if (elementNbt instanceof NbtString stringNbt) {
				unlockedSkills.add(stringNbt.value());
			}
		}

		var points = new HashMap<Identifier, Integer>();
		var pointsNbt = nbt.get("points");
		if (pointsNbt instanceof NbtInt pointsNbtInt) {
			points.put(PointSources.LEGACY, pointsNbtInt.intValue());
		} else if (pointsNbt instanceof NbtCompound pointsNbtCompound) {
			for (var key : pointsNbtCompound.getKeys()) {
				points.put(Identifier.of(key), pointsNbtCompound.getInt(key, 0));
			}
		}

		return new CategoryData(unlockedSkills, points, unlocked, experience);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putBoolean("unlocked", unlocked);
		nbt.putInt("experience", experience);

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
		if (unlockedSkills.contains(skill.id())) {
			return Skill.State.UNLOCKED;
		}

		if (category.connections()
				.exclusive()
				.getNeighborsFor(skill.id())
				.map(neighbors -> neighbors.stream().filter(unlockedSkills::contains).count())
				.orElse(0L) >= definition.requiredExclusions()
		) {
			return Skill.State.EXCLUDED;
		}

		if (category.connections()
				.normal()
				.getNeighborsFor(skill.id())
				.map(neighbors -> neighbors.stream().filter(unlockedSkills::contains).count())
				.orElse(0L) >= definition.requiredSkills()
		) {
			return canAfford(category, definition) ? Skill.State.AFFORDABLE : Skill.State.AVAILABLE;
		}

		if (skill.isRoot()) {
			if (category.general().exclusiveRoot() && unlockedSkills.stream()
					.flatMap(skillId -> category.skills().getById(skillId).stream())
					.anyMatch(SkillConfig::isRoot)
			) {
				return Skill.State.LOCKED;
			}

			return canAfford(category, definition) ? Skill.State.AFFORDABLE : Skill.State.AVAILABLE;
		}

		return Skill.State.LOCKED;
	}

	private boolean canAfford(CategoryConfig category, SkillDefinitionConfig definition) {
		return getPointsLeft(category) >= Math.max(definition.requiredPoints(), definition.cost())
				&& getSpentPoints(category) >= definition.requiredSpentPoints();
	}

	public boolean canUnlockSkill(CategoryConfig category, SkillConfig skill, boolean force) {
		var definitionId = skill.definitionId();

		return category.definitions()
				.getById(definitionId)
				.map(definition -> {
					var state = getSkillState(category, skill, definition);
					return force ? state != Skill.State.UNLOCKED : state == Skill.State.AFFORDABLE;
				})
				.orElse(false);
	}

	public int countUnlocked(CategoryConfig category, String definitionId) {
		return category.definitions().getById(definitionId).map(
				definition -> category.skills()
						.getAll()
						.stream()
						.filter(skill -> skill.definitionId().equals(definitionId))
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

	public Set<String> getUnlockedSkillIds() {
		return unlockedSkills;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int earnedExperience) {
		this.experience = earnedExperience;
	}

	public int getSpentPoints(CategoryConfig category) {
		return unlockedSkills.stream()
				.flatMap(skillId -> category.skills()
						.getById(skillId)
						.flatMap(skill -> category.definitions().getById(skill.definitionId()))
						.stream()
				)
				.mapToInt(SkillDefinitionConfig::cost)
				.sum();
	}

	public int getPointsTotal() {
		var total = 0;
		for (var count : points.values()) {
			total += count;
		}
		return total;
	}

	public int getPointsLeft(CategoryConfig category) {
		return Math.min(getPointsTotal(), category.general().spentPointsLimit()) - getSpentPoints(category);
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
