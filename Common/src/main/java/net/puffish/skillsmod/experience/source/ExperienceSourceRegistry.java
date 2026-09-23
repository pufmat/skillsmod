package net.puffish.skillsmod.experience.source;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ExperienceSourceRegistry {
	private static final Map<Identifier, ExperienceSourceFactory> factories = new ConcurrentHashMap<>();

	public static void register(Identifier id, ExperienceSourceFactory factory) {
		factories.compute(id, (key, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "` to experience source registry");
		});
	}

	public static Optional<ExperienceSourceFactory> getFactory(Identifier key) {
		return Optional.ofNullable(factories.get(key));
	}
}
