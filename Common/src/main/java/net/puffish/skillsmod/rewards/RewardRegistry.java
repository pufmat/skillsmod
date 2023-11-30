package net.puffish.skillsmod.rewards;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.rewards.RewardWithDataFactory;
import net.puffish.skillsmod.api.rewards.RewardWithoutDataFactory;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.HashMap;
import java.util.Optional;

public class RewardRegistry {
	private static final HashMap<Identifier, RewardFactory> factories = new HashMap<>();

	public static void register(Identifier key, RewardWithDataFactory factory) {
		register(key, (Result<JsonElementWrapper, Failure> maybeData, ConfigContext context) -> maybeData.andThen(data -> factory.create(data, context)));
	}

	public static void register(Identifier key, RewardWithoutDataFactory factory) {
		register(key, (Result<JsonElementWrapper, Failure> maybeData, ConfigContext context) -> factory.create(context));
	}

	private static void register(Identifier key, RewardFactory factory) {
		factories.compute(key, (key2, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "`'` to registry");
		});
	}

	public static Optional<RewardFactory> getFactory(Identifier key) {
		return Optional.ofNullable(factories.get(key));
	}
}
