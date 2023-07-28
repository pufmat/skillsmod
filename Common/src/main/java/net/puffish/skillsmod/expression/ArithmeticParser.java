package net.puffish.skillsmod.expression;

import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.SingleFailure;

import java.util.List;
import java.util.Set;

public class ArithmeticParser {
	private static final List<BinaryOperator<Double>> ARITHMETIC_BINARY = List.of(
			BinaryOperator.createLeft("+", 1, (l, r) -> v -> l.eval(v) + r.eval(v)),
			BinaryOperator.createLeft("-", 1, (l, r) -> v -> l.eval(v) - r.eval(v)),
			BinaryOperator.createLeft("*", 2, (l, r) -> v -> l.eval(v) * r.eval(v)),
			BinaryOperator.createLeft("/", 2, (l, r) -> v -> l.eval(v) / r.eval(v)),
			BinaryOperator.createRight("^", 4, (l, r) -> v -> Math.pow(l.eval(v), r.eval(v)))
	);

	private static final List<UnaryOperator<Double>> ARITHMETIC_UNARY = List.of(
			UnaryOperator.create("+", 3, e -> v -> +e.eval(v)),
			UnaryOperator.create("-", 3, e -> v -> -e.eval(v))
	);

	private static final List<GroupOperator> ARITHMETIC_GROUP = List.of(
			GroupOperator.create("(", ")")
	);

	private static final List<FunctionOperator<Double>> ARITHMETIC_FUNCTION = List.of(
			FunctionOperator.create("abs", "(", ",", ")", 1, l -> v -> Math.abs(l.get(0).eval(v))),
			FunctionOperator.createVariadic("min", "(", ",", ")", l -> v -> l.stream().mapToDouble(e -> e.eval(v)).min().orElse(+Double.MAX_VALUE)),
			FunctionOperator.createVariadic("max", "(", ",", ")", l -> v -> l.stream().mapToDouble(e -> e.eval(v)).max().orElse(-Double.MAX_VALUE))
	);

	public static Result<Expression<Double>, Failure> parse(String expression, Set<String> variables) {
		return Parser.parse(expression, ARITHMETIC_UNARY, ARITHMETIC_BINARY, ARITHMETIC_GROUP, ARITHMETIC_FUNCTION, token -> {
			if (variables.contains(token)) {
				return Result.success(v -> v.get(token));
			} else {
				try {
					var value = Double.parseDouble(token);
					return Result.success(v -> value);
				} catch (Exception e) {
					return Result.failure(SingleFailure.of("Unknown variable `" + token + "`"));
				}
			}
		});
	}
}
