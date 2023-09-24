package net.puffish.skillsmod.api.utils.failure;

import java.util.List;
import java.util.function.Function;

public interface Failure {
	List<String> getMessages();

	Failure map(Function<String, String> function);

	Failure flatMap(Function<String, Failure> function);
}
