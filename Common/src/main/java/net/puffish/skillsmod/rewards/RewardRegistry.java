package net.puffish.skillsmod.rewards;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Optional;

public class RewardRegistry {
	private static final HashMap<Identifier, RewardFactory> handlers = new HashMap<>();

	public static void register(Identifier key, RewardFactory handler) {
		handlers.compute(key, (key2, old) -> {
			if (old == null) {
				return handler;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "`'` to registry");
		});
	}

	public static Optional<RewardFactory> getFactory(Identifier key) {
		return Optional.ofNullable(handlers.get(key));
	}
}
