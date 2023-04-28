package net.puffish.skillsmod.utils.error;

import java.util.List;
import java.util.function.Function;

public interface Error {
	List<String> getMessages();

	Error map(Function<String, String> function);
	Error flatMap(Function<String, Error> function);
}
