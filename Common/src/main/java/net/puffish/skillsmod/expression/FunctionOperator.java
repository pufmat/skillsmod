package net.puffish.skillsmod.expression;

import java.util.List;
import java.util.function.Function;

public record FunctionOperator<T>(String name, String openToken, String separatorToken, String closeToken, int args, Function<List<Expression<T>>, Expression<T>> function) {

	public static <T> FunctionOperator<T> create(String name, String openToken, String separatorToken, String closeToken, int args, Function<List<Expression<T>>, Expression<T>> function) {
		return new FunctionOperator<>(name, openToken, separatorToken, closeToken, args, function);
	}

	public static <T> FunctionOperator<T> createVariadic(String name, String openToken, String separatorToken, String closeToken, Function<List<Expression<T>>, Expression<T>> function) {
		return create(name, openToken, separatorToken, closeToken, -1, function);
	}

}
