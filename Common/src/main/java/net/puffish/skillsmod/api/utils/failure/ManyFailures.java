package net.puffish.skillsmod.api.utils.failure;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ManyFailures implements Failure {
	private final List<String> failures;

	private ManyFailures(List<String> failures) {
		this.failures = failures;
	}

	public static ManyFailures ofList(Collection<Failure> failures) {
		return new ManyFailures(failures.stream().map(Failure::getMessages).flatMap(List::stream).toList());
	}

	public static ManyFailures ofMapValues(Map<?, Failure> failures) {
		return new ManyFailures(failures.values().stream().map(Failure::getMessages).flatMap(List::stream).toList());
	}

	@Override
	public List<String> getMessages() {
		return failures;
	}

	@Override
	public Failure map(Function<String, String> function) {
		return new ManyFailures(failures.stream().map(function).toList());
	}

	@Override
	public Failure flatMap(Function<String, Failure> function) {
		return ManyFailures.ofList(failures.stream().map(function).toList());
	}
}
