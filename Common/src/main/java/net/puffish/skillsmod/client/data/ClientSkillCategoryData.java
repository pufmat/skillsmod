package net.puffish.skillsmod.client.data;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.skill.SkillState;
import net.puffish.skillsmod.utils.Bounds2i;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClientSkillCategoryData {
	private final Identifier id;

	private final Text title;
	private final ClientIconData icon;
	private final Identifier background;
	private final boolean exclusiveRoot;
	private final int spentPointsLimit;

	private final Map<String, ClientSkillDefinitionData> definitions;
	private final Map<String, ClientSkillData> skills;
	private final Collection<ClientSkillConnectionData> normalConnections;
	private final Map<String, Collection<String>> normalNeighbors;
	private final Map<String, Collection<String>> exclusiveNeighbors;
	private final Map<String, Collection<ClientSkillConnectionData>> exclusiveConnections;

	private int spentPoints;
	private int earnedPoints;

	private int currentLevel;
	private int currentExperience;
	private int requiredExperience;

	private final Identifier playSound;

	public ClientSkillCategoryData(
			Identifier id,
			Text title,
			ClientIconData icon,
			Identifier background,
			Identifier playSound,
			boolean exclusiveRoot,
			int spentPointsLimit,
			Map<String, ClientSkillDefinitionData> definitions,
			Map<String, ClientSkillData> skills,
			Collection<ClientSkillConnectionData> normalConnections,
			Collection<ClientSkillConnectionData> exclusiveConnections,
			int spentPoints,
			int earnedPoints,
			int currentLevel,
			int currentExperience,
			int requiredExperience
	) {
		this.id = id;
		this.title = title;
		this.icon = icon;
		this.background = background;
		this.exclusiveRoot = exclusiveRoot;
		this.spentPointsLimit = spentPointsLimit;
		this.definitions = definitions;
		this.skills = skills;
		this.normalConnections = normalConnections;
		this.spentPoints = spentPoints;
		this.earnedPoints = earnedPoints;
		this.currentLevel = currentLevel;
		this.currentExperience = currentExperience;
		this.requiredExperience = requiredExperience;
		this.normalNeighbors = new HashMap<>();
		this.exclusiveNeighbors = new HashMap<>();
		this.exclusiveConnections = new HashMap<>();
		this.playSound = playSound;

		for (var connection : normalConnections) {
			var a = connection.getSkillAId();
			var b = connection.getSkillBId();

			normalNeighbors.computeIfAbsent(a, key -> new HashSet<>()).add(b);
			if (connection.isBidirectional()) {
				normalNeighbors.computeIfAbsent(b, key -> new HashSet<>()).add(a);
			}
		}

		for (var connection : exclusiveConnections) {
			var a = connection.getSkillAId();
			var b = connection.getSkillBId();

			exclusiveNeighbors.computeIfAbsent(a, key -> new HashSet<>()).add(b);
			if (connection.isBidirectional()) {
				exclusiveNeighbors.computeIfAbsent(b, key -> new HashSet<>()).add(a);
			}

			this.exclusiveConnections.computeIfAbsent(a, key -> new HashSet<>()).add(connection);
			this.exclusiveConnections.computeIfAbsent(b, key -> new HashSet<>()).add(connection);
		}
	}

	public Bounds2i getBounds() {
		Bounds2i bounds = Bounds2i.zero();
		for (var skill : skills.values()) {
			bounds.extend(new Vector2i(skill.getX(), skill.getY()));
		}
		return bounds;
	}

	public void unlock(String skillId) {
		var skill = skills.get(skillId);
		if (skill == null) {
			return;
		}
		skill.setState(SkillState.UNLOCKED);
		if (skill.isRoot() && exclusiveRoot) {
			for (var otherSkill : skills.values()) {
				if (otherSkill.isRoot() && otherSkill.getState() == SkillState.AVAILABLE) {
					otherSkill.setState(SkillState.LOCKED);
				}
			}
		}
		var normalNeighborsIds = normalNeighbors.get(skillId);
		if (normalNeighborsIds != null) {
			for (var neighborId : normalNeighborsIds) {
				var neighbor = skills.get(neighborId);
				if (neighbor != null && neighbor.getState() == SkillState.LOCKED) {
					neighbor.setState(SkillState.AVAILABLE);
				}
			}
		}
		var exclusiveNeighborsIds = exclusiveNeighbors.get(skillId);
		if (exclusiveNeighborsIds != null) {
			for (var neighborId : exclusiveNeighborsIds) {
				var neighbor = skills.get(neighborId);
				if (neighbor != null && neighbor.getState() != SkillState.UNLOCKED) {
					neighbor.setState(SkillState.EXCLUDED);
				}
			}
		}
	}

	public boolean hasAvailableSkill() {
		return skills.values()
				.stream()
				.anyMatch(skill -> skill.getState() == SkillState.AVAILABLE);
	}

	public Identifier getId() {
		return id;
	}

	public Map<String, ClientSkillDefinitionData> getDefinitions() {
		return definitions;
	}

	public Map<String, ClientSkillData> getSkills() {
		return skills;
	}

	public Collection<ClientSkillConnectionData> getNormalConnections() {
		return normalConnections;
	}

	public Map<String, Collection<ClientSkillConnectionData>> getExclusiveConnections() {
		return exclusiveConnections;
	}

	public Text getTitle() {
		return title;
	}

	public ClientIconData getIcon() {
		return icon;
	}

	public Identifier getBackground() {
		return background;
	}

	public int getPointsLeft() {
		return Math.max(Math.min(earnedPoints, spentPointsLimit) - spentPoints, 0);
	}

	public int getSpentPoints() {
		return spentPoints;
	}

	public void setSpentPoints(int spentPoints) {
		this.spentPoints = spentPoints;
	}

	public int getEarnedPoints() {
		return earnedPoints;
	}

	public void setEarnedPoints(int earnedPoints) {
		this.earnedPoints = earnedPoints;
	}

	public int getSpentPointsLeft() {
		return Math.max(spentPointsLimit - spentPoints, 0);
	}

	public int getSpentPointsLimit() {
		return spentPointsLimit;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public int getCurrentExperience() {
		return currentExperience;
	}

	public void setCurrentExperience(int currentExperience) {
		this.currentExperience = currentExperience;
	}

	public int getRequiredExperience() {
		return requiredExperience;
	}

	public void setRequiredExperience(int requiredExperience) {
		this.requiredExperience = requiredExperience;
	}

	public float getExperienceProgress() {
		return ((float) currentExperience) / ((float) requiredExperience);
	}

	public int getExperienceToNextLevel() {
		return requiredExperience - currentExperience;
	}

	public Identifier getPlaySound() {
		return playSound;
	}
}
