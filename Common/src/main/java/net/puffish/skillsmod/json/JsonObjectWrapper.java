package net.puffish.skillsmod.json;

import com.google.gson.JsonObject;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class JsonObjectWrapper extends JsonWrapper {
	private final JsonObject json;

	public JsonObjectWrapper(JsonObject json, JsonPath path) {
		super(path);
		this.json = json;
	}

	public Result<JsonElementWrapper, Error> get(String key) {
		var newPath = path.thenObject(key);
		var element = json.get(key);
		if (element == null) {
			return Result.failure(newPath.expectedToExist());
		} else {
			return Result.success(new JsonElementWrapper(element, newPath));
		}
	}

	public Result<JsonObjectWrapper, Error> getObject(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("an object"))
				.andThen(JsonElementWrapper::getAsObject);
	}

	public Result<JsonArrayWrapper, Error> getArray(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("an array"))
				.andThen(JsonElementWrapper::getAsArray);
	}

	public Result<String, Error> getString(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("a string"))
				.andThen(JsonElementWrapper::getAsString);
	}

	public Result<Float, Error> getFloat(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("a float"))
				.andThen(JsonElementWrapper::getAsFloat);
	}

	public Result<Integer, Error> getInt(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("an int"))
				.andThen(JsonElementWrapper::getAsInt);
	}

	public Result<Boolean, Error> getBoolean(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("a boolean"))
				.andThen(JsonElementWrapper::getAsBoolean);
	}

	public Stream<Map.Entry<String, JsonElementWrapper>> stream() {
		return json.entrySet()
				.stream()
				.map(entry -> Map.entry(
						entry.getKey(),
						new JsonElementWrapper(entry.getValue(), path.thenObject(entry.getKey()))
				));
	}

	public <S, F> Result<Map<String, S>, List<F>> getAsMap(JsonMapReader<S, F> reader) {
		var exceptions = new ArrayList<F>();
		var map = new HashMap<String, S>();

		json.entrySet().forEach(entry -> reader.apply(
				entry.getKey(),
				new JsonElementWrapper(entry.getValue(), path.thenObject(entry.getKey()))
		).peek(
				t -> map.put(entry.getKey(), t),
				exceptions::add
		));

		if (exceptions.isEmpty()) {
			return Result.success(map);
		} else {
			return Result.failure(exceptions);
		}
	}

	public JsonObject getJson() {
		return json;
	}
}
