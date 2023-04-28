package net.puffish.skillsmod.expression;

import java.util.function.BiFunction;

public record BinaryOperator<T>(String token, int precedence, boolean right, BiFunction<Expression<T>, Expression<T>, Expression<T>> function) {

	public static <T> BinaryOperator<T> createLeft(String token, int precedence, BiFunction<Expression<T>, Expression<T>, Expression<T>> function) {
		return new BinaryOperator<>(token, precedence, false, function);
	}

	public static <T> BinaryOperator<T> createRight(String token, int precedence, BiFunction<Expression<T>, Expression<T>, Expression<T>> function) {
		return new BinaryOperator<>(token, precedence, true, function);
	}

}
