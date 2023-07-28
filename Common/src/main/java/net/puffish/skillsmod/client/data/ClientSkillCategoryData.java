package net.puffish.skillsmod.client.data;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.skill.SkillState;
import net.puffish.skillsmod.utils.Bounds2i;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClientSkillCategoryData {
	private final Identifier id;

	private final Text title;
	private final ClientIconData icon;
	private final Identifier background;

	private final Map<String, ClientSkillDefinitionData> definitions;
	private final Map<String, ClientSkillData> skills;
	private final Collection<ClientSkillConnectionData> connections;
	private final Map<String, Collection<String>> neighbors;

	private int pointsLeft;
	private float experienceProgress;

	public ClientSkillCategoryData(Identifier id, Text title, ClientIconData icon, Identifier background, Map<String, ClientSkillDefinitionData> definitions, Map<String, ClientSkillData> skills, Collection<ClientSkillConnectionData> connections, int pointsLeft, float experienceProgress) {
		this.id = id;
		this.title = title;
		this.icon = icon;
		this.background = background;
		this.definitions = definitions;
		this.skills = skills;
		this.connections = connections;
		this.neighbors = new HashMap<>();
		this.pointsLeft = pointsLeft;
		this.experienceProgress = experienceProgress;

		for (var connection : connections) {
			neighbors.compute(connection.getSkillAId(), (key, value) -> {
				if (value == null) {
					value = new ArrayList<>();
				}
				value.add(connection.getSkillBId());
				return value;
			});
			neighbors.compute(connection.getSkillBId(), (key, value) -> {
				if (value == null) {
					value = new ArrayList<>();
				}
				value.add(connection.getSkillAId());
				return value;
			});
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
		if (skill.isRoot()) {
			for (var otherSkill : skills.values()) {
				if (otherSkill.isRoot() && otherSkill.getState() == SkillState.AVAILABLE) {
					otherSkill.setState(SkillState.LOCKED);
				}
			}
		}
		for (var neighborId : neighbors.get(skillId)) {
			var neighbor = skills.get(neighborId);
			if (neighbor != null && neighbor.getState() == SkillState.LOCKED) {
				neighbor.setState(SkillState.AVAILABLE);
			}
		}
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

	public Collection<ClientSkillConnectionData> getConnections() {
		return connections;
	}

	public Map<String, Collection<String>> getNeighbors() {
		return neighbors;
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
		return pointsLeft;
	}

	public void setPointsLeft(int pointsLeft) {
		this.pointsLeft = pointsLeft;
	}

	public float getExperienceProgress() {
		return experienceProgress;
	}

	public void setExperienceProgress(float experienceProgress) {
		this.experienceProgress = experienceProgress;
	}
}
