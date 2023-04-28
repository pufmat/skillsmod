package net.puffish.skillsmod.utils.error;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ManyErrors implements Error {
	private final List<String> errors;

	private ManyErrors(List<String> errors) {
		this.errors = errors;
	}

	public static ManyErrors ofList(Collection<Error> errors) {
		return new ManyErrors(errors.stream().map(Error::getMessages).flatMap(List::stream).toList());
	}

	@Override
	public List<String> getMessages() {
		return errors;
	}

	@Override
	public Error map(Function<String, String> function) {
		return new ManyErrors(errors.stream().map(function).toList());
	}

	@Override
	public Error flatMap(Function<String, Error> function) {
		return ManyErrors.ofList(errors.stream().map(function).toList());
	}
}
