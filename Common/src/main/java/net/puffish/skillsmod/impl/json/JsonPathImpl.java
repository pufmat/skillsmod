package net.puffish.skillsmod.impl.json;

import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonPathImpl implements JsonPath {
	private final List<String> path;

	public JsonPathImpl(List<String> path) {
		this.path = path;
	}

	@Override
	public JsonPath thenArray(long index) {
		return this.then("index " + index);
	}

	@Override
	public JsonPath thenObject(String key) {
		return this.then("`" + key + "`");
	}

	private JsonPath then(String str) {
		var path = new ArrayList<String>();
		path.add(str);
		path.addAll(this.path);
		return new JsonPathImpl(path);
	}

	@Override
	public Optional<JsonPath> getParent() {
		if (path.size() <= 1) {
			return Optional.empty();
		}
		return Optional.of(new JsonPathImpl(
				new ArrayList<>(this.path.subList(1, this.path.size()))
		));
	}

	private String getHead() {
		return this.path.get(0);
	}

	@Override
	public Failure expectedToExist() {
		return expectedTo("exist");
	}

	@Override
	public Failure expectedToExistAndBe(String str) {
		return expectedTo("exist and be " + str);
	}

	@Override
	public Failure expectedToBe(String str) {
		return expectedTo("be " + str);
	}

	@Override
	public Failure createFailure(String str) {
		return Failure.message(str + " at " + this);
	}

	private Failure expectedTo(String str) {
		var parent = getParent();
		if (parent.isPresent()) {
			return Failure.message("Expected " + getHead() + " to " + str + " at " + parent.orElseThrow() + ".");
		} else {
			return Failure.message("Expected " + getHead() + " to " + str + ".");
		}
	}

	@Override
	public String toString() {
		return String.join(" at ", path);
	}
}
