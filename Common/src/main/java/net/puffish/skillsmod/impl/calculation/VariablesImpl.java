package net.puffish.skillsmod.impl.calculation;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeOperation;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.impl.calculation.operation.OperationConfigContextImpl;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariablesImpl<T, R> implements Variables<T, R> {
	private final Map<String, Function<T, R>> operations;

	private VariablesImpl(Map<String, Function<T, R>> operations) {
		this.operations = operations;
	}

	@Override
	public Stream<String> streamNames() {
		return operations.keySet().stream();
	}

	@Override
	public Map<String, R> evaluate(T t) {
		return operations.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().apply(t)));
	}

	public static <T, R> Variables<T, R> create(
			Map<String, Function<T, R>> operations
	) {
		return new VariablesImpl<>(Map.copyOf(operations));
	}

	public static <T, R> Variables<T, R> combine(
			Collection<Variables<T, R>> variables
	) {
		return new CombineVariables<>(List.copyOf(variables));
	}

	@SafeVarargs
	public static <T, R> Variables<T, R> combine(
			Variables<T, R>... variables
	) {
		return new CombineVariables<>(Arrays.asList(variables));
	}

	public static <T> Result<Variables<T, Double>, Problem> parse(
			JsonElement rootElement,
			Prototype<T> prototype,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, prototype, context));
	}

	public static <T> Result<Variables<T, Double>, Problem> parse(
			JsonObject rootObject,
			Prototype<T> prototype,
			ConfigContext context
	) {
		return rootObject.getAsMap((key, value) -> parseVariable(value, prototype, context))
				.mapFailure(problems -> Problem.combine(problems.values()))
				.mapSuccess(VariablesImpl::new);
	}

	public static <T> Result<Function<T, Double>, Problem> parseVariable(
			JsonElement rootElement,
			Prototype<T> prototype,
			ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parseVariable(rootObject, prototype, context), context)
		);
	}

	public static <T> Result<Function<T, Double>, Problem> parseVariable(
			JsonObject rootObject,
			Prototype<T> prototype,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var optOperation = rootObject.getArray("operations")
				.mapSuccess(array -> {
					Optional<PrototypeOperation<T, ?>> o = Optional.of(PrototypeOperation.createIdentity(prototype));
					for (var element : (Iterable<JsonElement>) array.stream()::iterator) {
						o = o.flatMap(
								v -> parseOperation(element, v, context)
										.ifFailure(problems::add)
										.getSuccess()
						);
					}
					return o;
				})
				.orElse(LegacyUtils.wrapDeprecated(
						() -> parseOperation(rootObject, PrototypeOperation.createIdentity(prototype), context, "legacy_")
								.mapSuccess(Optional::of),
						3,
						context
				))
				.ifFailure(problems::add)
				.getSuccess()
				.flatMap(Function.identity());

		var optFallback = rootObject.get("fallback")
				.getSuccess()
				.flatMap(fallbackElement -> fallbackElement.getAsDouble()
						.ifFailure(problems::add)
						.getSuccess()
				);

		var required = rootObject.get("required")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsBoolean()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(true);

		if (problems.isEmpty()) {
			return buildVariable(
					optOperation.orElseThrow(),
					optFallback,
					rootObject.getPath().getObject("operations")
			).orElse(problem -> {
				if (required || optFallback.isEmpty()) {
					return Result.failure(problem);
				} else {
					context.emitWarning(problem.toString());
					var fallback = optFallback.orElseThrow();
					return Result.success(t -> fallback);
				}
			});
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public static <T> Result<PrototypeOperation<T, ?>, Problem> parseOperation(
			JsonElement rootElement,
			PrototypeOperation<T, ?> operation,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parseOperation(rootObject, operation, context, ""), context)
		);
	}

	public static <T> Result<PrototypeOperation<T, ?>, Problem> parseOperation(
			JsonObject rootObject,
			PrototypeOperation<T, ?> operation,
			ConfigContext context,
			String prefix
	) {
		var problems = new ArrayList<Problem>();

		var optType = rootObject.get("type")
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		var maybeDataElement = rootObject.get("data");

		if (problems.isEmpty()) {
			return buildOperation(
					operation,
					optType.orElseThrow().withPrefixedPath(prefix),
					rootObject.getPath().getObject("type"),
					maybeDataElement,
					context
			);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static <T> Result<PrototypeOperation<T, ?>, Problem> buildOperation(
			PrototypeOperation<T, ?> operation,
			Identifier type,
			JsonPath typePath,
			Result<JsonElement, Problem> maybeDataElement,
			ConfigContext context
	) {
		if (type.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			type = Identifier.of(operation.getReturnPrototype().getId().getNamespace(), type.getPath());
		}
		var factory = operation.andThen(type, new OperationConfigContextImpl(context, maybeDataElement));
		if (factory.isEmpty()) {
			return Result.failure(typePath.createProblem("Expected a valid operation type"));
		}
		return factory.orElseThrow();
	}

	private static <T> Result<Function<T, Double>, Problem> buildVariable(
			PrototypeOperation<T, ?> operation,
			Optional<Double> fallback,
			JsonPath operationsPath
	) {
		var optOperation = operation.recoverReturnType(BuiltinPrototypes.NUMBER)
				.map(po -> (Operation<T, Double>) po)
				.or(() -> operation.recoverReturnType(BuiltinPrototypes.BOOLEAN)
						.map(c -> t -> c.apply(t).map(b -> b ? 1.0 : 0.0))
				);
		if (optOperation.isPresent()) {
			return Result.success(t -> optOperation.orElseThrow().apply(t).orElseGet(() -> {
				if (fallback.isEmpty()) {
					SkillsMod.getInstance().getLogger().warn(
							operationsPath.createProblem("Fallback is not specified but operations returned no value").toString()
					);
				}
				return fallback.orElse(Double.NaN);
			}));
		} else {
			return Result.failure(operationsPath.createProblem("Expected operations to provide a number"));
		}
	}

	private record CombineVariables<T, R>(List<Variables<T, R>> variablesList) implements Variables<T, R> {
		@Override
		public Stream<String> streamNames() {
			return variablesList.stream().flatMap(Variables::streamNames);
		}

		@Override
		public Map<String, R> evaluate(T t) {
			return variablesList.stream()
					.flatMap(variables -> variables.evaluate(t).entrySet().stream())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
	}
}