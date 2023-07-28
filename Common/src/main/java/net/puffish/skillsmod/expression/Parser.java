package net.puffish.skillsmod.expression;

import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;
import net.puffish.skillsmod.utils.failure.SingleFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Parser<T> {
	private Lexer lexer;
	private final List<UnaryOperator<T>> unaryOperators;
	private final List<BinaryOperator<T>> binaryOperators;
	private final List<GroupOperator> groupOperators;
	private final List<FunctionOperator<T>> functionOperators;
	private final Function<String, Result<Expression<T>, Failure>> otherHandler;

	private final List<Failure> failures = new ArrayList<>();

	private Parser(Lexer lexer, List<UnaryOperator<T>> unaryOperators, List<BinaryOperator<T>> binaryOperators, List<GroupOperator> groupOperators, List<FunctionOperator<T>> functionOperators, Function<String, Result<Expression<T>, Failure>> otherHandler) {
		this.lexer = lexer;
		this.unaryOperators = unaryOperators;
		this.binaryOperators = binaryOperators;
		this.groupOperators = groupOperators;
		this.functionOperators = functionOperators;
		this.otherHandler = otherHandler;
	}

	public static <T> Result<Expression<T>, Failure> parse(String expression, List<UnaryOperator<T>> unaryOperators, List<BinaryOperator<T>> binaryOperators, List<GroupOperator> groupOperators, List<FunctionOperator<T>> functionOperators, Function<String, Result<Expression<T>, Failure>> otherHandler) {
		return new Parser<>(Lexer.create(expression), unaryOperators, binaryOperators, groupOperators, functionOperators, otherHandler).parse();
	}

	private Result<Expression<T>, Failure> parse() {
		var expression = tryParse();
		if (failures.isEmpty() && !lexer.isEnd()) {
			failures.add(SingleFailure.of("Invalid expression"));
		}
		if (failures.isEmpty()) {
			return Result.success(expression.orElseThrow());
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	private Optional<Expression<T>> tryParse() {
		return tryParse(0);
	}

	private Optional<Expression<T>> tryParse(int precedence) {
		var optLeft = consumePrefix();
		if (optLeft.isEmpty()) {
			return Optional.empty();
		}
		var left = optLeft.orElseThrow();

		while (true) {
			if (lexer.isEnd()) {
				break;
			}

			var optBinary = testBinary(precedence);
			if (optBinary.isEmpty()) {
				break;
			}
			var binary = optBinary.orElseThrow();

			var optRight = tryParse(binary.precedence() + (binary.right() ? 0 : 1));
			if (optRight.isEmpty()) {
				return Optional.empty();
			}
			var right = optRight.orElseThrow();

			left = binary.function().apply(left, right);
		}

		return Optional.of(left);
	}

	private Optional<BinaryOperator<T>> testBinary(int precedence) {
		for (var binary : binaryOperators) {
			if (binary.precedence() < precedence) {
				continue;
			}
			if (lexer.consume(binary.token())) {
				return Optional.of(binary);
			}
		}

		return Optional.empty();
	}

	private Optional<Expression<T>> consumePrefix() {
		for (var unary : unaryOperators) {
			if (lexer.consume(unary.token())) {
				return tryParse(unary.precedence()).map(expr -> unary.function().apply(expr));
			}
		}

		for (var group : groupOperators) {
			if (lexer.consume(group.openToken())) {
				var optExpression = tryParse();
				if (lexer.consume(group.closeToken())) {
					return optExpression;
				}

				return invalid();
			}
		}

		for (var function : functionOperators) {
			var testLexer = Lexer.copy(lexer);
			if (testLexer.consume(function.name()) && testLexer.consume(function.openToken())) {
				lexer = testLexer;

				if (lexer.consume(function.closeToken())) {
					if (function.args() != -1 && function.args() != 0) {
						return invalid();
					}
					return Optional.of(function.function().apply(List.of()));
				}

				var args = new ArrayList<Expression<T>>();
				while (true) {
					var optExpression = tryParse();
					if (optExpression.isEmpty()) {
						return Optional.empty();
					}
					args.add(optExpression.orElseThrow());

					if (lexer.consume(function.closeToken())) {
						if (function.args() != -1 && function.args() != args.size()) {
							return invalid();
						}
						return Optional.of(function.function().apply(args));
					}
					if (!lexer.consume(function.separatorToken())) {
						return invalid();
					}
				}
			}
		}

		var optToken = lexer.consumeOther();
		if (optToken.isPresent()) {
			var token = optToken.orElseThrow();

			return Optional.of(
					otherHandler.apply(token)
							.ifFailure(failures::add)
							.getSuccessOrElse(e -> v -> null)
			);
		}

		return invalid();
	}

	private <R> Optional<R> invalid() {
		failures.add(SingleFailure.of("Invalid expression"));
		return Optional.empty();
	}
}
