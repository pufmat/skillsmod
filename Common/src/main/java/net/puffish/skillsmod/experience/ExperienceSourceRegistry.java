package net.puffish.skillsmod.experience;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Optional;

public class ExperienceSourceRegistry {
	private static final HashMap<Identifier, ExperienceSourceFactory> factories = new HashMap<>();

	public static void register(Identifier key, ExperienceSourceFactory factory) {
		factories.compute(key, (key2, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "`'` to registry");
		});
	}

	public static Optional<ExperienceSourceFactory> getFactory(Identifier key) {
		return Optional.ofNullable(factories.get(key));
	}
}
