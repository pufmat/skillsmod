package net.puffish.skillsmod.expression;

import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.SingleFailure;

import java.util.List;
import java.util.Set;

public class LogicParser {

	private static final List<BinaryOperator<Boolean>> LOGIC_BINARY = List.of(
			BinaryOperator.createLeft("|", 1, (l, r) -> v -> l.eval(v) || r.eval(v)),
			BinaryOperator.createLeft("&", 2, (l, r) -> v -> l.eval(v) && r.eval(v))
	);

	private static final List<UnaryOperator<Boolean>> LOGIC_UNARY = List.of(
			UnaryOperator.create("!", 3, e -> v -> !e.eval(v))
	);

	private static final List<GroupOperator> LOGIC_GROUP = List.of(
			GroupOperator.create("(", ")")
	);

	private static final List<FunctionOperator<Boolean>> LOGIC_FUNCTION = List.of(

	);

	public static Result<Expression<Boolean>, Failure> parse(String expression, Set<String> variables) {
		return Parser.parse(expression, LOGIC_UNARY, LOGIC_BINARY, LOGIC_GROUP, LOGIC_FUNCTION, token -> {
			if (variables.contains(token)) {
				return Result.success(v -> v.get(token));
			} else {
				return Result.failure(SingleFailure.of("Unknown variable `" + token + "`"));
			}
		});
	}
}
