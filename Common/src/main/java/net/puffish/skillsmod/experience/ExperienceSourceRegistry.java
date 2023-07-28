package net.puffish.skillsmod.experience;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

import java.util.HashMap;
import java.util.Optional;

public class ExperienceSourceRegistry {
	private static final HashMap<Identifier, ExperienceSourceFactory> factories = new HashMap<>();

	public static void register(Identifier key, ExperienceSourceWithDataFactory factory) {
		register(key, (Result<JsonElementWrapper, Failure> maybeData, ConfigContext context) -> maybeData.andThen(data -> factory.create(data, context)));
	}

	public static void register(Identifier key, ExperienceSourceWithoutDataFactory factory) {
		register(key, (Result<JsonElementWrapper, Failure> maybeData, ConfigContext context) -> factory.create(context));
	}

	private static void register(Identifier key, ExperienceSourceFactory factory) {
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
