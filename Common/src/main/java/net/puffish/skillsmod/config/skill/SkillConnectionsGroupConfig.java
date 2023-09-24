package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.json.JsonArrayWrapper;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.skill.SkillConnection;
import net.puffish.skillsmod.skill.SkillPair;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SkillConnectionsGroupConfig {
	private final List<SkillConnection> connections;
	private final Map<String, Collection<String>> neighbors;

	private SkillConnectionsGroupConfig(List<SkillConnection> connections, Map<String, Collection<String>> neighbors) {
		this.connections = connections;
		this.neighbors = neighbors;
	}

	public static SkillConnectionsGroupConfig empty() {
		return new SkillConnectionsGroupConfig(List.of(), Map.of());
	}

	public static Result<SkillConnectionsGroupConfig, Failure> parse(JsonElementWrapper rootElement, SkillsConfig skills) {
		return rootElement.getAsObject()
				.andThen(rootObject -> SkillConnectionsGroupConfig.parse(rootObject, skills));
	}

	private static Result<SkillConnectionsGroupConfig, Failure> parse(JsonObjectWrapper rootObject, SkillsConfig skills) {
		var failures = new ArrayList<Failure>();

		var bidirectional = rootObject
				.getArray("bidirectional")
				.getSuccess()
				.flatMap(array -> array.getAsList((i, element) -> SkillConnectionConfig.parse(element, skills))
						.<Failure>mapFailure(ManyFailures::ofList)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		var unidirectional = rootObject
				.getArray("unidirectional")
				.getSuccess()
				.flatMap(array -> array.getAsList((i, element) -> SkillConnectionConfig.parse(element, skills))
						.<Failure>mapFailure(ManyFailures::ofList)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		if (failures.isEmpty()) {
			return Result.success(build(
					bidirectional,
					unidirectional
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public static Result<SkillConnectionsGroupConfig, Failure> parseLegacy(JsonArrayWrapper rootArray, SkillsConfig skills) {
		return rootArray.getAsList((i, element) -> SkillConnectionConfig.parse(element, skills))
				.<Failure>mapFailure(ManyFailures::ofList)
				.mapSuccess(bidirectional -> SkillConnectionsGroupConfig.build(bidirectional, List.of()));
	}

	private static SkillConnectionsGroupConfig build(
			List<SkillConnectionConfig> bidirectional,
			List<SkillConnectionConfig> unidirectional
	) {
		var neighbors = new HashMap<String, Collection<String>>();
		var directions = new HashMap<SkillPair, SkillPair.Direction>();

		for (var connection : unidirectional) {
			var a = connection.getSkillAId();
			var b = connection.getSkillBId();

			var order = a.compareTo(b);
			if (order == 0) {
				continue;
			}

			neighbors.computeIfAbsent(b, k -> new HashSet<>()).add(a);

			directions.compute(
					order > 0 ? new SkillPair(a, b) : new SkillPair(b, a),
					(k, v) -> {
						var dir = order > 0 ? SkillPair.Direction.A_TO_B : SkillPair.Direction.B_TO_A;
						if (v == null || v == dir) {
							return dir;
						}
						return SkillPair.Direction.BOTH;
					}
			);
		}

		for (var connection : bidirectional) {
			var a = connection.getSkillAId();
			var b = connection.getSkillBId();

			var order = a.compareTo(b);
			if (order == 0) {
				continue;
			}

			neighbors.computeIfAbsent(a, k -> new HashSet<>()).add(b);
			neighbors.computeIfAbsent(b, k -> new HashSet<>()).add(a);

			directions.compute(
					order > 0 ? new SkillPair(a, b) : new SkillPair(b, a),
					(k, v) -> SkillPair.Direction.BOTH
			);
		}

		return new SkillConnectionsGroupConfig(
				directions.entrySet()
						.stream()
						.map(entry -> {
							var a = entry.getKey().skillAId();
							var b = entry.getKey().skillBId();
							return switch (entry.getValue()) {
								case A_TO_B -> SkillConnection.createUnidirectional(a, b);
								case B_TO_A -> SkillConnection.createUnidirectional(b, a);
								case BOTH -> SkillConnection.createBidirectional(a, b);
							};
						})
						.toList(),
				neighbors
		);
	}

	public List<SkillConnection> getAll() {
		return connections;
	}

	public Optional<Collection<String>> getNeighborsFor(String skillId) {
		return Optional.ofNullable(neighbors.get(skillId));
	}
}