package net.puffish.skillsmod.expression;

import java.util.function.Function;

public record UnaryOperator<T>(String token, int precedence, Function<Expression<T>, Expression<T>> function) {

	public static <T> UnaryOperator<T> create(String token, int precedence, Function<Expression<T>, Expression<T>> function) {
		return new UnaryOperator<>(token, precedence, function);
	}

}
