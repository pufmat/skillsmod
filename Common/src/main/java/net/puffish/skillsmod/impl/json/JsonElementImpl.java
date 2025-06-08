package net.puffish.skillsmod.impl.json;

import net.puffish.skillsmod.api.json.JsonArray;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.JsonPathFailure;

public record JsonElementImpl(
		com.google.gson.JsonElement json,
		JsonPath path
) implements JsonElement {

	@Override
	public Result<JsonObject, Problem> getAsObject() {
		try {
			return Result.success(new JsonObjectImpl(json.getAsJsonObject(), path));
		} catch (Exception e) {
			return Result.failure(JsonPathFailure.expectedToBe(path, "an object"));
		}
	}

	@Override
	public Result<JsonArray, Problem> getAsArray() {
		try {
			return Result.success(new JsonArrayImpl(json.getAsJsonArray(), path));
		} catch (Exception e) {
			return Result.failure(JsonPathFailure.expectedToBe(path, "an array"));
		}
	}

	@Override
	public Result<String, Problem> getAsString() {
		try {
			return Result.success(json.getAsString());
		} catch (Exception e) {
			return Result.failure(JsonPathFailure.expectedToBe(path, "a string"));
		}
	}

	@Override
	public Result<Float, Problem> getAsFloat() {
		try {
			return Result.success(json.getAsFloat());
		} catch (Exception e) {
			return Result.failure(JsonPathFailure.expectedToBe(path, "a float"));
		}
	}

	@Override
	public Result<Double, Problem> getAsDouble() {
		try {
			return Result.success(json.getAsDouble());
		} catch (Exception e) {
			return Result.failure(JsonPathFailure.expectedToBe(path, "a double"));
		}
	}

	@Override
	public Result<Integer, Problem> getAsInt() {
		try {
			return Result.success(json.getAsInt());
		} catch (Exception e) {
			return Result.failure(JsonPathFailure.expectedToBe(path, "an int"));
		}
	}

	@Override
	public Result<Boolean, Problem> getAsBoolean() {
		try {
			return Result.success(json.getAsBoolean());
		} catch (Exception e) {
			return Result.failure(JsonPathFailure.expectedToBe(path, "a boolean"));
		}
	}

	@Override
	public com.google.gson.JsonElement getJson() {
		return json;
	}

	@Override
	public JsonPath getPath() {
		return path;
	}
}
