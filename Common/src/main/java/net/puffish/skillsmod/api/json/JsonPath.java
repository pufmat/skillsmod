package net.puffish.skillsmod.api.json;

import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.SingleFailure;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonPath {
	private final List<String> path;

	private JsonPath(List<String> path) {
		this.path = path;
	}

	public static JsonPath createNamed(String name) {
		return new JsonPath(List.of("`" + name + "`"));
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

	public Failure expectedToExist() {
		return expectedTo("exist");
	}

	public Failure expectedToExistAndBe(String str) {
		return expectedTo("exist and be " + str);
	}

	public Failure expectedToBe(String str) {
		return expectedTo("be " + str);
	}

	public Failure failureAt(String str) {
		return SingleFailure.of(str + " at " + this);
	}

	private Failure expectedTo(String str) {
		var parent = getParent();
		if (parent.isPresent()) {
			return SingleFailure.of("Expected " + getHead() + " to " + str + " at " + parent.orElseThrow() + ".");
		} else {
			return SingleFailure.of("Expected " + getHead() + " to " + str + ".");
		}
	}

	@Override
	public String toString() {
		return String.join(" at ", path);
	}
}
