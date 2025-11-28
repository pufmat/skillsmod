package net.puffish.skillsmod.experience.source.builtin.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public record AntiFarmingPerEntity(float limitPerEntity, int resetAfterSeconds) {
	public static Result<AntiFarmingPerEntity, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(AntiFarmingPerEntity::parse, context)
		);
	}

	private static Result<AntiFarmingPerEntity, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var limitPerEntity = rootObject.getFloat("limit_per_entity")
				.ifFailure(problems::add)
				.getSuccess();

		var resetAfterSeconds = rootObject.getInt("reset_after_seconds")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new AntiFarmingPerEntity(
					limitPerEntity.orElseThrow(),
					resetAfterSeconds.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public static class State {
		private final Reference2ObjectMap<AntiFarmingPerEntity, Instance> antiFarmingData = new Reference2ObjectOpenHashMap<>();

		public float addAndLimit(AntiFarmingPerEntity antiFarming, float damage) {
			return antiFarmingData
					.computeIfAbsent(antiFarming, key -> new Instance())
					.addAndLimit(damage, antiFarming.limitPerEntity(), antiFarming.resetAfterSeconds());
		}

		public void removeOutdated() {
			antiFarmingData.values().removeIf(Instance::cleanupOutdated);
		}

		private static class Instance {
			private final Queue<TimeDamage> queue = new LinkedList<>();
			private float totalDamage = 0;

			public float addAndLimit(float damage, float limitPerEntity, long resetAfterSeconds) {
				if (totalDamage < limitPerEntity) {
					damage = Math.min(damage, limitPerEntity - totalDamage);
					queue.add(new TimeDamage(
							System.currentTimeMillis() + resetAfterSeconds * 1000L,
							damage
					));
					totalDamage += damage;
					return damage;
				} else {
					return 0;
				}
			}

			public boolean cleanupOutdated() {
				var currentTime = System.currentTimeMillis();

				var it = queue.iterator();
				while (it.hasNext()) {
					var el = it.next();
					if (el.time >= currentTime) {
						break;
					}
					totalDamage -= el.damage;
					it.remove();
				}

				return queue.isEmpty();
			}

			private record TimeDamage(long time, float damage) { }
		}
	}
}
