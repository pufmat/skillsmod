package net.puffish.skillsmod.experience.calculation;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.expression.ArithmeticParser;
import net.puffish.skillsmod.expression.Expression;
import net.puffish.skillsmod.expression.LogicParser;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Calculation {
	private final Expression<Boolean> condition;
	private final Expression<Double> expression;
	private final JsonPath expressionElementPath;

	private Calculation(Expression<Boolean> condition, Expression<Double> expression, JsonPath expressionElementPath) {
		this.condition = condition;
		this.expression = expression;
		this.expressionElementPath = expressionElementPath;
	}

	public static Result<Calculation, Failure> parse(JsonElementWrapper rootElement, Set<String> conditionVariables, Set<String> expressionVariables) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, conditionVariables, expressionVariables));
	}

	public static Result<Calculation, Failure> parse(JsonObjectWrapper rootObject, Set<String> conditionVariables, Set<String> expressionVariables) {
		var failures = new ArrayList<Failure>();

		var condition = rootObject.get("condition")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsString()
						.andThen(string -> LogicParser.parse(string, conditionVariables)
								.mapFailure(failure -> failure.flatMap(msg -> element.getPath().failureAt(msg)))
						)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElse(p -> true); // no condition, so always true

		var optExpressionElement = rootObject.get("expression")
				.ifFailure(failures::add)
				.getSuccess();

		var optExpression = optExpressionElement
				.flatMap(element -> element.getAsString()
						.andThen(string -> ArithmeticParser.parse(string, expressionVariables)
								.mapFailure(failure -> failure.flatMap(msg -> element.getPath().failureAt(msg))))
						.ifFailure(failures::add)
						.getSuccess()
				);

		if (failures.isEmpty()) {
			return Result.success(new Calculation(
					condition,
					optExpression.orElseThrow(),
					optExpressionElement.orElseThrow().getPath()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public boolean test(Map<String, Boolean> variables) {
		return condition.eval(variables);
	}

	public double eval(Map<String, Double> variables) {
		return expression.eval(variables);
	}

	public Optional<Integer> getValue(Map<String, Boolean> conditionVariables, Map<String, Double> expressionVariables) {
		if (test(conditionVariables)) {
			var value = eval(expressionVariables);
			if (Double.isFinite(value)) {
				return Optional.of((int) Math.round(value));
			} else {
				for (var message : expressionElementPath.failureAt("Expression returned a value that is not finite").getMessages()) {
					SkillsMod.getInstance().getLogger().warn(message);
				}
				return Optional.of(0);
			}
		}
		return Optional.empty();
	}

}
