package net.puffish.skillsmod.utils.error;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManyErrors implements Error {
	private final List<Error> errors;

	private ManyErrors(List<Error> errors) {
		this.errors = errors;
	}

	public static ManyErrors ofList(List<Error> errors) {
		var tmp = new ArrayList<Error>();
		for (var error : errors) {
			if (error instanceof ManyErrors manyErrors) {
				tmp.addAll(manyErrors.errors);
			} else {
				tmp.add(error);
			}
		}
		return new ManyErrors(tmp);
	}

	public List<Error> get() {
		return errors;
	}

	@Override
	public String getMessage() {
		return errors.stream().map(Error::getMessage).collect(Collectors.joining(System.lineSeparator()));
	}
}
