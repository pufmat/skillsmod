package net.puffish.skillsmod.impl.json;

import com.google.gson.JsonObject;
import net.puffish.skillsmod.api.json.JsonArrayWrapper;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonMapReader;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class JsonObjectWrapperImpl extends JsonWrapperImpl implements JsonObjectWrapper {
	private final JsonObject json;

	public JsonObjectWrapperImpl(JsonObject json, JsonPath path) {
		super(path);
		this.json = json;
	}

	@Override
	public Result<JsonElementWrapper, Failure> get(String key) {
		var newPath = path.thenObject(key);
		var element = json.get(key);
		if (element == null) {
			return Result.failure(newPath.expectedToExist());
		} else {
			return Result.success(new JsonElementWrapperImpl(element, newPath));
		}
	}

	@Override
	public Result<JsonObjectWrapper, Failure> getObject(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("an object"))
				.andThen(JsonElementWrapper::getAsObject);
	}

	@Override
	public Result<JsonArrayWrapper, Failure> getArray(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("an array"))
				.andThen(JsonElementWrapper::getAsArray);
	}

	@Override
	public Result<String, Failure> getString(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("a string"))
				.andThen(JsonElementWrapper::getAsString);
	}

	@Override
	public Result<Float, Failure> getFloat(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("a float"))
				.andThen(JsonElementWrapper::getAsFloat);
	}

	@Override
	public Result<Double, Failure> getDouble(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("a double"))
				.andThen(JsonElementWrapper::getAsDouble);
	}

	@Override
	public Result<Integer, Failure> getInt(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("an int"))
				.andThen(JsonElementWrapper::getAsInt);
	}

	@Override
	public Result<Boolean, Failure> getBoolean(String key) {
		return get(key)
				.mapFailure(e -> path.thenObject(key).expectedToExistAndBe("a boolean"))
				.andThen(JsonElementWrapper::getAsBoolean);
	}

	@Override
	public Stream<Map.Entry<String, JsonElementWrapper>> stream() {
		return json.asMap()
				.entrySet()
				.stream()
				.map(entry -> Map.entry(
						entry.getKey(),
						new JsonElementWrapperImpl(entry.getValue(), path.thenObject(entry.getKey()))
				));
	}

	@Override
	public <S, F> Result<Map<String, S>, Map<String, F>> getAsMap(JsonMapReader<S, F> reader) {
		var successMap = new HashMap<String, S>();
		var failureMap = new HashMap<String, F>();

		json.asMap().forEach((key, value) -> reader.apply(
				key,
				new JsonElementWrapperImpl(value, path.thenObject(key))
		).peek(
				t -> successMap.put(key, t),
				t -> failureMap.put(key, t)
		));

		if (failureMap.isEmpty()) {
			return Result.success(successMap);
		} else {
			return Result.failure(failureMap);
		}
	}

	@Override
	public JsonObject getJson() {
		return json;
	}
}
