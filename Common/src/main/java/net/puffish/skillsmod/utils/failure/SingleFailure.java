package net.puffish.skillsmod.utils.failure;

import java.util.List;
import java.util.function.Function;

public class SingleFailure implements Failure {
	private final String message;

	private SingleFailure(String message) {
		this.message = message;
	}

	public static SingleFailure of(String message) {
		return new SingleFailure(message);
	}

	@Override
	public List<String> getMessages() {
		return List.of(message);
	}

	@Override
	public Failure map(Function<String, String> function) {
		return SingleFailure.of(function.apply(message));
	}

	@Override
	public Failure flatMap(Function<String, Failure> function) {
		return function.apply(message);
	}
}
