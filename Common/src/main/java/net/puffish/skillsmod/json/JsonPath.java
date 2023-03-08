package net.puffish.skillsmod.json;

import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.SingleError;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonPath {
	private final List<String> path;

	private JsonPath(List<String> path) {
		this.path = path;
	}

	public static JsonPath createAnonymous() {
		return new JsonPath(List.of());
	}

	public static JsonPath fromPath(Path path) {
		return new JsonPath(List.of("`" + path + "`"));
	}

	public JsonPath thenArray(long index) {
		return this.then("index " + index);
	}

	public JsonPath thenObject(String key) {
		return this.then("`" + key + "`");
	}

	private JsonPath then(String str) {
		var path = new ArrayList<String>();
		path.add(str);
		path.addAll(this.path);
		return new JsonPath(path);
	}

	public Optional<JsonPath> getParent() {
		if (path.size() <= 1) {
			return Optional.empty();
		}
		return Optional.of(new JsonPath(
				new ArrayList<>(this.path.subList(1, this.path.size()))
		));
	}

	public String getHead() {
		return this.path.get(0);
	}

	public Error expectedToExist() {
		return expectedTo("exist");
	}

	public Error expectedToExistAndBe(String str) {
		return expectedTo("exist and be " + str);
	}

	public Error expectedToBe(String str) {
		return expectedTo("be " + str);
	}

	public Error errorAt(String str) {
		return SingleError.of(str + " at " + this);
	}

	private Error expectedTo(String str) {
		var parent = getParent();
		if (parent.isPresent()) {
			return SingleError.of("Expected " + getHead() + " to " + str + " at " + parent.orElseThrow());
		} else {
			return SingleError.of("Expected " + getHead() + " to " + str);
		}
	}

	@Override
	public String toString() {
		return String.join(" at ", path);
	}
}
