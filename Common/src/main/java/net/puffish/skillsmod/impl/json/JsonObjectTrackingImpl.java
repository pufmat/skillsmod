package net.puffish.skillsmod.impl.json;

import net.puffish.skillsmod.api.json.JsonArray;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class JsonObjectTrackingImpl implements JsonObject {
	private final Set<String> usedKeys = new HashSet<>();

	private final JsonObject parent;

	public JsonObjectTrackingImpl(JsonObject parent) {
		this.parent = parent;
	}

	public List<Problem> reportUnusedEntries() {
		return parent.stream()
				.filter(entry -> !usedKeys.contains(entry.getKey()))
				.map(entry -> parent.getPath().createProblem("Unused field `" + entry.getKey() + "`"))
				.toList();
	}

	@Override
	public Result<JsonElement, Problem> get(String key) {
		usedKeys.add(key);
		return parent.get(key);
	}

	@Override
	public Result<JsonObject, Problem> getObject(String key) {
		usedKeys.add(key);
		return parent.getObject(key);
	}

	@Override
	public Result<JsonArray, Problem> getArray(String key) {
		usedKeys.add(key);
		return parent.getArray(key);
	}

	@Override
	public Result<String, Problem> getString(String key) {
		usedKeys.add(key);
		return parent.getString(key);
	}

	@Override
	public Result<Float, Problem> getFloat(String key) {
		usedKeys.add(key);
		return parent.getFloat(key);
	}

	@Override
	public Result<Double, Problem> getDouble(String key) {
		usedKeys.add(key);
		return parent.getDouble(key);
	}

	@Override
	public Result<Integer, Problem> getInt(String key) {
		usedKeys.add(key);
		return parent.getInt(key);
	}

	@Override
	public Result<Boolean, Problem> getBoolean(String key) {
		usedKeys.add(key);
		return parent.getBoolean(key);
	}

	@Override
	public Stream<Map.Entry<String, JsonElement>> stream() {
		return parent.stream();
	}

	@Override
	public JsonElement getAsElement() {
		return parent.getAsElement();
	}

	@Override
	public <S, F> Result<Map<String, S>, Map<String, F>> getAsMap(BiFunction<String, JsonElement, Result<S, F>> function) {
		return parent.getAsMap(function);
	}

	@Override
	public <S> Result<S, Problem> noUnused(Function<JsonObject, Result<S, Problem>> function) {
		return parent.noUnused(function);
	}

	@Override
	public JsonPath getPath() {
		return parent.getPath();
	}

	@Override
	public com.google.gson.JsonObject getJson() {
		return parent.getJson();
	}
}
