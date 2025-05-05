package net.puffish.skillsmod.experience;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ExperienceCurve {
	private final Function<Integer, Integer> function;
	private final List<Integer> requiredCache = new ArrayList<>();
	private final List<Integer> requiredTotalCache = new ArrayList<>();

	private ExperienceCurve(Function<Integer, Integer> function) {
		this.function = function;
	}

	public static ExperienceCurve create(Function<Integer, Integer> function) {
		var curve = new ExperienceCurve(function);
		curve.requiredTotalCache.add(curve.getRequired(0));
		return curve;
	}

	// Returns the experience required at the specified level.
	public int getRequired(int level) {
		while (level >= requiredCache.size()) {
			requiredCache.add(function.apply(requiredCache.size()));
		}
		return requiredCache.get(level);
	}

	// Returns the total experience required at the specified level.
	public int getRequiredTotal(int level) {
		while (level >= requiredTotalCache.size()) {
			requiredTotalCache.add(
					requiredTotalCache.get(requiredTotalCache.size() - 1)
							+ getRequired(requiredTotalCache.size())
			);
		}
		return requiredTotalCache.get(level);
	}

	// Returns progress information about the current level, current experience and require experience based on the earned experience.
	public Progress getProgress(int earned) {
		var low = 0;
		var high = requiredTotalCache.size();
		while (low < high) {
			int mid = low + high >>> 1;
			if (earned < requiredTotalCache.get(mid)) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		var level = low;

		if (level == 0) {
			var next = requiredTotalCache.get(level);
			return new Progress(level, earned, next);
		}

		if (level < requiredTotalCache.size()) {
			var prev = requiredTotalCache.get(level - 1);
			var next = requiredTotalCache.get(level);
			return new Progress(level, earned - prev, next - prev);
		}

		while (true) {
			var prev = requiredTotalCache.get(level - 1);
			var next = prev + getRequired(level);
			requiredTotalCache.add(next);
			if (earned < next) {
				return new Progress(level, earned - prev, next - prev);
			}
			level++;
		}
	}

	public record Progress(int currentLevel, int currentExperience, int requiredExperience) { }
}
