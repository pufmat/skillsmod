package net.puffish.skillsmod.experience.calculation;

import net.puffish.skillsmod.experience.calculation.condition.Condition;
import net.puffish.skillsmod.experience.calculation.condition.ConditionFactory;
import net.puffish.skillsmod.experience.calculation.parameter.Parameter;
import net.puffish.skillsmod.experience.calculation.parameter.ParameterFactory;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

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

	public static <T> Result<CalculationManager<T>, Error> create(
			JsonElementWrapper rootElement,
			Map<String, ConditionFactory<T>> conditionFactories,
			Map<String, ParameterFactory<T>> parameterFactories
	) {
		return rootElement.getAsObject().andThen(rootObject -> create(rootObject, conditionFactories, parameterFactories));
	}

	public static <T> Result<CalculationManager<T>, Error> create(
			JsonObjectWrapper rootObject,
			Map<String, ConditionFactory<T>> conditionFactories,
			Map<String, ParameterFactory<T>> parameterFactories
	) {
		var errors = new ArrayList<Error>();

		var conditions = new HashMap<String, Condition<T>>();
		var parameters = new HashMap<String, Parameter<T>>();

		rootObject.getObject("conditions")
				.getSuccess() // ignore failure because this property is optional
				.ifPresent(array -> array.stream()
						.forEach(entry -> Condition.parse(entry.getValue(), conditionFactories)
								.peek(
										condition -> conditions.put(entry.getKey(), condition),
										errors::add
								)
						)
				);

		rootObject.getObject("parameters")
				.getSuccess() // ignore failure because this property is optional
				.ifPresent(array -> array.stream()
						.forEach(entry -> Parameter.parse(entry.getValue(), parameterFactories)
								.peek(
										parameter -> parameters.put(entry.getKey(), parameter),
										errors::add
								)
						)
				);

		var calculations = rootObject.getArray("experience")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(array -> array.getAsList((i, element) -> Calculation.parse(element, conditions.keySet(), parameters.keySet()))
						.mapFailure(ManyErrors::ofList)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		if (errors.isEmpty()) {
			return Result.success(new CalculationManager<>(
					conditions,
					parameters,
					calculations
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

}
