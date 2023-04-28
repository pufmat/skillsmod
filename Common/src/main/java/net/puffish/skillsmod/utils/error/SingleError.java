package net.puffish.skillsmod.utils.error;

import java.util.List;
import java.util.function.Function;

public class SingleError implements Error {
	private final String message;

	private SingleError(String message) {
		this.message = message;
	}

	public static SingleError of(String message) {
		return new SingleError(message);
	}

	@Override
	public List<String> getMessages() {
		return List.of(message);
	}

	@Override
	public Error map(Function<String, String> function) {
		return SingleError.of(function.apply(message));
	}

	@Override
	public Error flatMap(Function<String, Error> function) {
		return function.apply(message);
	}
}
