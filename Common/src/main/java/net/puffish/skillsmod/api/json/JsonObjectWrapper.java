package net.puffish.skillsmod.api.json;

import com.google.gson.JsonObject;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.Map;
import java.util.stream.Stream;

public interface JsonObjectWrapper extends JsonWrapper {
	Result<JsonElementWrapper, Failure> get(String key);

	Result<JsonObjectWrapper, Failure> getObject(String key);

	Result<JsonArrayWrapper, Failure> getArray(String key);

	Result<String, Failure> getString(String key);

	Result<Float, Failure> getFloat(String key);

	Result<Double, Failure> getDouble(String key);

	Result<Integer, Failure> getInt(String key);

	Result<Boolean, Failure> getBoolean(String key);

	Stream<Map.Entry<String, JsonElementWrapper>> stream();

	<S, F> Result<Map<String, S>, Map<String, F>> getAsMap(JsonMapReader<S, F> reader);

	JsonObject getJson();
}
