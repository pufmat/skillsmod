package net.puffish.skillsmod.client.data;

import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.colors.ClientFillStrokeColorsConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ClientCategoryData {
	private final ClientCategoryConfig config;
	private final Map<String, Skill.State> skillStates;
	private final Map<ClientSkillConnectionConfig, ClientSkillConnectionData> connectionStates;

	private int spentPoints;
	private int earnedPoints;

	private int currentLevel;
	private int currentExperience;
	private int requiredExperience;

	private float scale = 1;

	private int x = 0;
	private int y = 0;

	private long lastOpen;

	private boolean unseenPoints;

	public ClientCategoryData(
			ClientCategoryConfig config,
			Map<String, Skill.State> skillStates,
			int spentPoints,
			int earnedPoints,
			int currentLevel,
			int currentExperience,
			int requiredExperience
	) {
		this.config = config;
		this.skillStates = skillStates;
		this.spentPoints = spentPoints;
		this.earnedPoints = earnedPoints;
		this.currentLevel = currentLevel;
		this.currentExperience = currentExperience;
		this.requiredExperience = requiredExperience;
		this.connectionStates = new HashMap<>();

		for (var connection : config.normalConnections()) {
			var skillA = config.skills().get(connection.skillAId());
			if (skillA == null) {
				continue;
			}
			var skillB = config.skills().get(connection.skillBId());
			if (skillB == null) {
				continue;
			}

			connectionStates.put(connection, new ClientSkillConnectionData(
					skillA,
					skillB,
					getConnectionColor(skillA, skillB, connection.bidirectional())
			));
		}
		for (var connection : config.exclusiveConnections()) {
			var skillA = config.skills().get(connection.skillAId());
			if (skillA == null) {
				continue;
			}
			var skillB = config.skills().get(connection.skillBId());
			if (skillB == null) {
				continue;
			}

			connectionStates.put(connection, new ClientSkillConnectionData(
					skillA,
					skillB,
					this.config.colors().connections().excluded()
			));
		}

		this.unseenPoints = getPointsLeft() > 0;
	}

	private void updateSkillState(ClientSkillConfig skill, Skill.State state) {
		skillStates.put(skill.id(), state);

		var normalConnections = this.config.skillNormalConnections().get(skill.id());
		if (normalConnections != null) {
			for (var connection : normalConnections) {
				updateConnection(connection);
			}
		}
	}

	public Skill.State getSkillState(ClientSkillConfig skill) {
		return skillStates.get(skill.id());
	}

	public void unlock(String skillId) {
		var skill = config.skills().get(skillId);
		if (skill == null) {
			return;
		}
		updateSkillState(skill, Skill.State.UNLOCKED);
		if (skill.isRoot() && config.exclusiveRoot()) {
			config.skills()
					.values()
					.stream()
					.filter(ClientSkillConfig::isRoot)
					.filter(other -> switch (getSkillState(other)) {
						case AVAILABLE, AFFORDABLE -> true;
						default -> false;
					})
					.forEach(other -> {
						if (!isAvailable(other)) {
							updateSkillState(other, Skill.State.LOCKED);
						}
					});
		}
		var normalNeighborsIds = config.skillNormalNeighbors().get(skillId);
		if (normalNeighborsIds != null) {
			normalNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == Skill.State.LOCKED)
					.forEach(neighbor -> {
						if (isAvailable(neighbor)) {
							if (isAffordable(neighbor)) {
								updateSkillState(neighbor, Skill.State.AFFORDABLE);
							} else {
								updateSkillState(neighbor, Skill.State.AVAILABLE);
							}
						}
					});
		}
		var exclusiveNeighborsIds = config.skillExclusiveNeighbors().get(skillId);
		if (exclusiveNeighborsIds != null) {
			exclusiveNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) != Skill.State.UNLOCKED)
					.forEach(neighbor -> {
						if (isExcluded(neighbor)) {
							updateSkillState(neighbor, Skill.State.EXCLUDED);
						}
					});
		}
	}

	public void lock(String skillId) {
		var skill = config.skills().get(skillId);
		if (skill == null) {
			return;
		}
		if (isExcluded(skill)) {
			updateSkillState(skill, Skill.State.EXCLUDED);
		} else if (isAvailable(skill)) {
			if (isAffordable(skill)) {
				updateSkillState(skill, Skill.State.AFFORDABLE);
			} else {
				updateSkillState(skill, Skill.State.AVAILABLE);
			}
		} else {
			updateSkillState(skill, Skill.State.LOCKED);
		}
		if (skill.isRoot()) {
			if (config.exclusiveRoot()) {
				if (config.skills()
						.values()
						.stream()
						.filter(ClientSkillConfig::isRoot)
						.allMatch(other -> getSkillState(other) != Skill.State.UNLOCKED)) {
					config.skills()
							.values()
							.stream()
							.filter(ClientSkillConfig::isRoot)
							.filter(other -> getSkillState(other) == Skill.State.LOCKED)
							.forEach(other -> {
								if (isAffordable(other)) {
									updateSkillState(other, Skill.State.AFFORDABLE);
								} else {
									updateSkillState(other, Skill.State.AVAILABLE);
								}
							});
				}
			}
		}
		var normalNeighborsIds = config.skillNormalNeighbors().get(skillId);
		if (normalNeighborsIds != null) {
			normalNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> switch (getSkillState(neighbor)) {
						case AVAILABLE, AFFORDABLE -> true;
						default -> false;
					})
					.forEach(neighbor -> {
						if (!isAvailable(neighbor)) {
							updateSkillState(neighbor, Skill.State.LOCKED);
						}
					});
		}
		var exclusiveNeighborsIds = config.skillExclusiveNeighbors().get(skillId);
		if (exclusiveNeighborsIds != null) {
			exclusiveNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == Skill.State.EXCLUDED)
					.forEach(neighbor -> {
						if (!isExcluded(neighbor)) {
							if (isAvailable(neighbor)) {
								if (isAffordable(neighbor)) {
									updateSkillState(neighbor, Skill.State.AFFORDABLE);
								} else {
									updateSkillState(neighbor, Skill.State.AVAILABLE);
								}
							} else {
								updateSkillState(neighbor, Skill.State.LOCKED);
							}
						}
					});
		}
	}

	private boolean isExcluded(ClientSkillConfig skill) {
		var exclusiveNeighborsReversedIds = config.skillExclusiveNeighborsReversed().get(skill.id());
		if (exclusiveNeighborsReversedIds == null) {
			return false;
		}
		return config.getDefinitionById(skill.definitionId())
				.map(definition -> exclusiveNeighborsReversedIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == Skill.State.UNLOCKED)
					.count() >= definition.requiredExclusions()
				)
				.orElse(false);
	}

	private boolean isAvailable(ClientSkillConfig skill) {
		var normalNeighborsReversedIds = config.skillNormalNeighborsReversed().get(skill.id());
		if (normalNeighborsReversedIds != null) {
			if (config.getDefinitionById(skill.definitionId())
					.map(definition -> normalNeighborsReversedIds.stream()
							.map(id -> config.skills().get(id))
							.filter(Objects::nonNull)
							.filter(neighbor -> getSkillState(neighbor) == Skill.State.UNLOCKED)
							.count() >= definition.requiredSkills()
					)
					.orElse(false)) {
				return true;
			}
		}
		if (skill.isRoot()) {
			return !config.exclusiveRoot() || config.skills()
					.values()
					.stream()
					.filter(ClientSkillConfig::isRoot)
					.allMatch(other -> getSkillState(other) != Skill.State.UNLOCKED);
		}
		return false;
	}

	private boolean isAffordable(ClientSkillConfig skill) {
		return config.getDefinitionById(skill.definitionId()).map(this::isAffordable).orElse(false);
	}

	private boolean isAffordable(ClientSkillDefinitionConfig definition) {
		return getPointsLeft() >= Math.max(definition.requiredPoints(), definition.cost())
				&& spentPoints >= definition.requiredSpentPoints();
	}

	public boolean hasAnySkillLeft() {
		return config.skills()
				.values()
				.stream()
				.map(this::getSkillState)
				.anyMatch(state -> state == Skill.State.AVAILABLE || state == Skill.State.AFFORDABLE);
	}

	public void updatePoints(int spentPoints, int earnedPoints) {
		var prevPointsLeft = getPointsLeft();

		this.spentPoints = spentPoints;
		this.earnedPoints = earnedPoints;

		if (getPointsLeft() > prevPointsLeft) {
			unseenPoints = true;
		}

		config.skills()
				.values()
				.stream()
				.filter(skill -> switch (getSkillState(skill)) {
					case AVAILABLE, AFFORDABLE -> true;
					default -> false;
				})
				.forEach(skill -> {
					if (isAffordable(skill)) {
						updateSkillState(skill, Skill.State.AFFORDABLE);
					} else {
						updateSkillState(skill, Skill.State.AVAILABLE);
					}
				});
	}

	private void updateConnection(ClientSkillConnectionConfig connection) {
		var data = connectionStates.get(connection);
		if (data == null) {
			return;
		}

		data.setColor(getConnectionColor(data.getSkillA(), data.getSkillB(), connection.bidirectional()));
	}

	private static final int[][][] ORDER = {
			{
					{0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0},
					{0, 1, 2, 3, 0},
					{0, 0, 0, 0, 0}
			},
			{
					{0, 0, 0, 0, 0},
					{0, 0, 0, 1, 0},
					{0, 0, 0, 2, 0},
					{0, 1, 2, 3, 0},
					{0, 0, 0, 0, 0}
			}
	};

	private ClientFillStrokeColorsConfig getConnectionColor(ClientSkillConfig skillA, ClientSkillConfig skillB, boolean bidirectional) {
		return switch (ORDER
				[bidirectional ? 1 : 0]
				[getSkillState(skillA).ordinal()]
				[getSkillState(skillB).ordinal()]
		) {
			case 3 -> config.colors().connections().unlocked();
			case 2 -> config.colors().connections().affordable();
			case 1 -> config.colors().connections().available();
			default -> config.colors().connections().locked();
		};
	}

	public Optional<ClientSkillConnectionData> getConnection(ClientSkillConnectionConfig connection) {
		return Optional.ofNullable(connectionStates.get(connection));
	}

	public ClientCategoryConfig getConfig() {
		return config;
	}

	public int getPointsLeft() {
		return Math.max(Math.min(earnedPoints, config.spentPointsLimit()) - spentPoints, 0);
	}

	public int getSpentPoints() {
		return spentPoints;
	}

	public int getEarnedPoints() {
		return earnedPoints;
	}

	public int getSpentPointsLeft() {
		return Math.max(config.spentPointsLimit() - spentPoints, 0);
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public boolean hasExperience() {
		return currentLevel >= 0;
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

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean hasUnseenPoints() {
		return unseenPoints;
	}

	public void updateUnseenPoints() {
		this.unseenPoints = false;
	}

	public long getLastOpen() {
		return lastOpen;
	}

	public void updateLastOpen() {
		this.lastOpen = System.currentTimeMillis();
	}
}
