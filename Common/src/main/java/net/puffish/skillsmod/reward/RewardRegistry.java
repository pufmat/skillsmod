package net.puffish.skillsmod.reward;

import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.api.reward.RewardFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RewardRegistry {
	private static final Map<Identifier, RewardFactory> factories = new ConcurrentHashMap<>();

	public static void register(Identifier id, RewardFactory factory) {
		factories.compute(id, (key, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "` to reward registry");
		});
	}

	public static Optional<RewardFactory> getFactory(Identifier key) {
		return Optional.ofNullable(factories.get(key));
	}
}
