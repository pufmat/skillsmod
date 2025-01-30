package net.puffish.skillsmod.impl.calculation.prototype;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeOperation;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class PrototypeImpl<T> implements Prototype<T> {

	private final Map<Identifier, Function<OperationConfigContext, Result<PrototypeOperation<T, ?>, Problem>>> factories = new HashMap<>();

	private final Identifier id;

	public PrototypeImpl(Identifier id) {
		this.id = id;
	}

	private <R> Function<OperationConfigContext, Result<PrototypeOperation<T, ?>, Problem>> createFunction(Prototype<R> prototype, OperationFactory<T, R> factory) {
		return context -> factory.apply(context).mapSuccess(o -> new PrototypeOperationImpl<>(prototype, o));
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public <R> void registerOperation(Identifier id, Prototype<R> prototype, OperationFactory<T, R> factory) {
		register(id, createFunction(prototype, factory));
	}

	@Override
	public Optional<Result<PrototypeOperation<T, ?>, Problem>> getOperation(Identifier id, OperationConfigContext context) {
		var function = factories.get(id);
		if (function == null || function instanceof PrototypeImpl.LegacyFunction<T> && LegacyUtils.isRemoved(3, context)) {
			return Optional.empty();
		}
		return Optional.of(function.apply(context));
	}

	public <R> void registerLegacyOperation(Identifier id, Prototype<R>  prototype, OperationFactory<T, R> factory) {
		register(id, new LegacyFunction<>(createFunction(prototype, factory)));
	}

	public void registerAlias(Identifier id, Identifier existingId) {
		register(id, new LegacyFunction<>(Objects.requireNonNull(factories.get(existingId))));
	}

	private void register(Identifier id, Function<OperationConfigContext, Result<PrototypeOperation<T, ?>, Problem>> factory) {
		factories.compute(id, (key, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "` to registry");
		});
	}

	private record LegacyFunction<T>(
			Function<OperationConfigContext, Result<PrototypeOperation<T, ?>, Problem>> parent
	) implements Function<OperationConfigContext, Result<PrototypeOperation<T, ?>, Problem>> {

		@Override
		public Result<PrototypeOperation<T, ?>, Problem> apply(OperationConfigContext operationConfigContext) {
			return parent.apply(operationConfigContext);
		}

	}

}
