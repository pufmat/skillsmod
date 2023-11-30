package net.puffish.skillsmod.api.json;

import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.impl.json.JsonPathImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface JsonPath {
	static JsonPath named(String name) {
		return new JsonPathImpl(List.of("`" + name + "`"));
	}

	static JsonPath fromPath(Path path) {
		return new JsonPathImpl(List.of("`" + path + "`"));
	}

	JsonPath thenArray(long index);

	JsonPath thenObject(String key);

	Optional<JsonPath> getParent();

	Failure expectedToExist();

	Failure expectedToExistAndBe(String str);

	Failure expectedToBe(String str);

	Failure createFailure(String str);

	@Override
	String toString();
}
