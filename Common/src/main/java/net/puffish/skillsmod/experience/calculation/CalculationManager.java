package net.puffish.skillsmod.experience.calculation;

import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.experience.calculation.condition.Condition;
import net.puffish.skillsmod.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.experience.calculation.parameter.Parameter;
import net.puffish.skillsmod.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculationManager<T> {
	private final Map<String, Condition<T>> conditions;
	private final Map<String, Parameter<T>> parameters;
	private final List<Calculation> calculations;

	private CalculationManager(Map<String, Condition<T>> conditions, Map<String, Parameter<T>> parameters, List<Calculation> calculations) {
		this.conditions = conditions;
		this.parameters = parameters;
		this.calculations = calculations;
	}

	public int getValue(T t) {
		var conditionsVariables = conditions.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().test(t)));

		var expressionVariables = parameters.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().apply(t)));

		return calculations.stream()
				.flatMap(calc -> calc.getValue(conditionsVariables, expressionVariables).stream())
				.findFirst()
				.orElse(0);
	}

	public static <T> Result<CalculationManager<T>, Failure> create(
			JsonElementWrapper rootElement,
			Map<String, ConditionFactory<T>> conditionFactories,
			Map<String, ParameterFactory<T>> parameterFactories,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(rootObject -> create(rootObject, conditionFactories, parameterFactories, context));
	}

	public static <T> Result<CalculationManager<T>, Failure> create(
			JsonObjectWrapper rootObject,
			Map<String, ConditionFactory<T>> conditionFactories,
			Map<String, ParameterFactory<T>> parameterFactories,
			ConfigContext context
	) {
		var failures = new ArrayList<Failure>();

		var conditions = new HashMap<String, Condition<T>>();
		var parameters = new HashMap<String, Parameter<T>>();

		rootObject.getObject("conditions")
				.getSuccess() // ignore failure because this property is optional
				.ifPresent(array -> array.stream()
						.forEach(entry -> Condition.parse(entry.getValue(), conditionFactories, context)
								.peek(
										condition -> conditions.put(entry.getKey(), condition),
										failures::add
								)
						)
				);

		rootObject.getObject("parameters")
				.getSuccess() // ignore failure because this property is optional
				.ifPresent(array -> array.stream()
						.forEach(entry -> Parameter.parse(entry.getValue(), parameterFactories, context)
								.peek(
										parameter -> parameters.put(entry.getKey(), parameter),
										failures::add
								)
						)
				);

		var calculations = rootObject.getArray("experience")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(array -> array.getAsList((i, element) -> Calculation.parse(element, conditions.keySet(), parameters.keySet()))
						.mapFailure(ManyFailures::ofList)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		if (failures.isEmpty()) {
			return Result.success(new CalculationManager<>(
					conditions,
					parameters,
					calculations
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

}
