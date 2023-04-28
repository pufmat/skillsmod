package net.puffish.skillsmod.experience.calculation;

import net.puffish.skillsmod.expression.ArithmeticParser;
import net.puffish.skillsmod.expression.Expression;
import net.puffish.skillsmod.expression.LogicParser;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Calculation {
	private final Expression<Boolean> condition;
	private final Expression<Double> expression;

	private Calculation(Expression<Boolean> condition, Expression<Double> expression) {
		this.condition = condition;
		this.expression = expression;
	}

	public static Result<Calculation, Error> parse(JsonElementWrapper rootElement, Set<String> conditionVariables, Set<String> expressionVariables) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, conditionVariables, expressionVariables));
	}

	public static Result<Calculation, Error> parse(JsonObjectWrapper rootObject, Set<String> conditionVariables, Set<String> expressionVariables) {
		var errors = new ArrayList<Error>();

		var condition = rootObject.get("condition")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsString()
						.andThen(string -> LogicParser.parse(string, conditionVariables)
								.mapFailure(error -> error.flatMap(msg -> element.getPath().errorAt(msg)))
						)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElse(p -> true); // no condition, so always true

		var optExpression = rootObject.get("expression")
				.andThen(element -> element.getAsString()
						.andThen(string -> ArithmeticParser.parse(string, expressionVariables)
								.mapFailure(error -> error.flatMap(msg -> element.getPath().errorAt(msg))))
				)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new Calculation(
					condition,
					optExpression.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public boolean test(Map<String, Boolean> variables) {
		return condition.eval(variables);
	}

	public double eval(Map<String, Double> variables) {
		return expression.eval(variables);
	}

}
