package net.puffish.skillsmod.impl.json;

import com.google.gson.JsonElement;
import net.puffish.skillsmod.api.json.JsonArrayWrapper;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

public class JsonElementWrapperImpl extends JsonWrapperImpl implements JsonElementWrapper {
	private final JsonElement json;

	public JsonElementWrapperImpl(JsonElement json, JsonPath path) {
		super(path);
		this.json = json;
	}

	@Override
	public Result<JsonObjectWrapper, Failure> getAsObject() {
		try {
			return Result.success(new JsonObjectWrapperImpl(json.getAsJsonObject(), path));
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("an object"));
		}
	}

	@Override
	public Result<JsonArrayWrapper, Failure> getAsArray() {
		try {
			return Result.success(new JsonArrayWrapperImpl(json.getAsJsonArray(), path));
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("an array"));
		}
	}

	@Override
	public Result<String, Failure> getAsString() {
		try {
			return Result.success(json.getAsString());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("a string"));
		}
	}

	@Override
	public Result<Float, Failure> getAsFloat() {
		try {
			return Result.success(json.getAsFloat());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("a float"));
		}
	}

	@Override
	public Result<Double, Failure> getAsDouble() {
		try {
			return Result.success(json.getAsDouble());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("a double"));
		}
	}

	@Override
	public Result<Integer, Failure> getAsInt() {
		try {
			return Result.success(json.getAsInt());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("an int"));
		}
	}

	@Override
	public Result<Boolean, Failure> getAsBoolean() {
		try {
			return Result.success(json.getAsBoolean());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("a boolean"));
		}
	}

	@Override
	public JsonElement getJson() {
		return json;
	}
}
