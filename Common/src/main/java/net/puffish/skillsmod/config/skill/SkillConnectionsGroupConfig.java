package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonArray;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.common.SkillConnection;
import net.puffish.skillsmod.common.SkillPair;
import net.puffish.skillsmod.util.LegacyUtils;

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

	public static Result<SkillConnectionsGroupConfig, Problem> parse(JsonElement rootElement, SkillsConfig skills, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, skills), context)
		);
	}

	private static Result<SkillConnectionsGroupConfig, Problem> parse(JsonObject rootObject, SkillsConfig skills) {
		var problems = new ArrayList<Problem>();

		var bidirectional = rootObject
				.getArray("bidirectional")
				.getSuccess()
				.flatMap(array -> array.getAsList((i, element) -> SkillConnectionConfig.parse(element, skills))
						.mapFailure(Problem::combine)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		var unidirectional = rootObject
				.getArray("unidirectional")
				.getSuccess()
				.flatMap(array -> array.getAsList((i, element) -> SkillConnectionConfig.parse(element, skills))
						.mapFailure(Problem::combine)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		if (problems.isEmpty()) {
			return Result.success(build(
					bidirectional,
					unidirectional
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public static Result<SkillConnectionsGroupConfig, Problem> parseLegacy(JsonArray rootArray, SkillsConfig skills) {
		return rootArray.getAsList((i, element) -> SkillConnectionConfig.parse(element, skills))
				.mapFailure(Problem::combine)
				.mapSuccess(bidirectional -> SkillConnectionsGroupConfig.build(bidirectional, List.of()));
	}

	private static SkillConnectionsGroupConfig build(
			List<Optional<SkillConnectionConfig>> bidirectional,
			List<Optional<SkillConnectionConfig>> unidirectional
	) {
		var neighbors = new HashMap<String, Collection<String>>();
		var directions = new HashMap<SkillPair, SkillPair.Direction>();

		for (var optConnection : unidirectional) {
			optConnection.ifPresent(connection -> {
				var a = connection.skillAId();
				var b = connection.skillBId();

				var order = a.compareTo(b);
				if (order == 0) {
					return;
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
			});
		}

		for (var optConnection : bidirectional) {
			optConnection.ifPresent(connection -> {
				var a = connection.skillAId();
				var b = connection.skillBId();

				var order = a.compareTo(b);
				if (order == 0) {
					return;
				}

				neighbors.computeIfAbsent(a, k -> new HashSet<>()).add(b);
				neighbors.computeIfAbsent(b, k -> new HashSet<>()).add(a);

				directions.compute(
						order > 0 ? new SkillPair(a, b) : new SkillPair(b, a),
						(k, v) -> SkillPair.Direction.BOTH
				);
			});
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