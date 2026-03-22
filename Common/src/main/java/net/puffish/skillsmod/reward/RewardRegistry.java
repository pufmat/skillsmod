package net.puffish.skillsmod.reward;

import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.api.reward.RewardFactory;

import java.util.HashMap;
import java.util.Optional;

public class RewardRegistry {
	private static final HashMap<Identifier, RewardFactory> factories = new HashMap<>();

	public static void register(Identifier key, RewardFactory factory) {
		factories.compute(key, (key2, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "` to registry");
		});
	}

	public static Optional<RewardFactory> getFactory(Identifier key) {
		return Optional.ofNullable(factories.get(key));
	}
}
