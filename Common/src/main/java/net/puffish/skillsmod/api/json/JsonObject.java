package net.puffish.skillsmod.api.json;

import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public interface JsonObject {
	Result<JsonElement, Problem> get(String key);

	Result<JsonObject, Problem> getObject(String key);

	Result<JsonArray, Problem> getArray(String key);

	Result<String, Problem> getString(String key);

	Result<Float, Problem> getFloat(String key);

	Result<Double, Problem> getDouble(String key);

	Result<Integer, Problem> getInt(String key);

	Result<Boolean, Problem> getBoolean(String key);

	Stream<Map.Entry<String, JsonElement>> stream();

	JsonElement getAsElement();

	<S, F> Result<Map<String, S>, Map<String, F>> getAsMap(BiFunction<String, JsonElement, Result<S, F>> function);

	<S> Result<S, Problem> noUnused(Function<JsonObject, Result<S, Problem>> function);

	JsonPath getPath();

	com.google.gson.JsonObject getJson();
}
