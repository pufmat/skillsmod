package net.puffish.skillsmod.experience.source;

import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceFactory;

import java.util.HashMap;
import java.util.Optional;

public class ExperienceSourceRegistry {
	private static final HashMap<Identifier, ExperienceSourceFactory> factories = new HashMap<>();

	public static void register(Identifier id, ExperienceSourceFactory factory) {
		factories.compute(id, (key, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "` to registry");
		});
	}

	public static Optional<ExperienceSourceFactory> getFactory(Identifier key) {
		return Optional.ofNullable(factories.get(key));
	}
}
