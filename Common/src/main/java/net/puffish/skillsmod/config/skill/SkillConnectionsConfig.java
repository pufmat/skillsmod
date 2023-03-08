package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.json.JsonArrayWrapper;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillConnectionsConfig {
	private final List<SkillConnectionConfig> connections;
	private final Map<String, Collection<String>> neighbors;

	private SkillConnectionsConfig(List<SkillConnectionConfig> connections, Map<String, Collection<String>> neighbors) {
		this.connections = connections;
		this.neighbors = neighbors;
	}

	public static Result<SkillConnectionsConfig, Error> parse(JsonElementWrapper rootElement, SkillsConfig skills) {
		return rootElement.getAsArray().andThen(rootArray -> SkillConnectionsConfig.parse(rootArray, skills));
	}

	public static Result<SkillConnectionsConfig, Error> parse(JsonArrayWrapper rootArray, SkillsConfig skills) {
		return rootArray.getAsList((i, element) -> SkillConnectionConfig.parse(element, skills))
				.<Error>mapFailure(ManyErrors::ofList)
				.mapSuccess(SkillConnectionsConfig::build);
	}

	private static SkillConnectionsConfig build(List<SkillConnectionConfig> connections) {
		var neighbors = new HashMap<String, Collection<String>>();
		for (var connection : connections) {
			if (connection.getSkillAId().equals(connection.getSkillBId())) {
				continue;
			}

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

		return new SkillConnectionsConfig(connections, neighbors);
	}

	public List<SkillConnectionConfig> getAll() {
		return connections;
	}

	public Map<String, Collection<String>> getNeighbors() {
		return neighbors;
	}
}