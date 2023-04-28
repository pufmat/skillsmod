package net.puffish.skillsmod.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.SingleError;

import java.nio.file.Files;
import java.nio.file.Path;

public class JsonElementWrapper extends JsonWrapper {
	private final JsonElement json;

	public JsonElementWrapper(JsonElement json, JsonPath path) {
		super(path);
		this.json = json;
	}

	public static Result<JsonElementWrapper, Error> parseString(String jsonData, JsonPath jsonPath) {
		try {
			return Result.success(new JsonElementWrapper(
					JsonParser.parseString(jsonData),
					jsonPath
			));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Could not read JSON"));
		}
	}

	public static Result<JsonElementWrapper, Error> parseFile(Path filePath, JsonPath jsonPath) {
		String name = filePath.getFileName().toString();
		try {
			var content = Files.readString(filePath);
			if (content.isEmpty()) {
				return Result.failure(SingleError.of("File `" + name + "` is empty."));
			}
			return Result.success(new JsonElementWrapper(
					JsonParser.parseString(content),
					jsonPath
			));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Could not read file `" + name + "`."));
		}
	}

	public Result<JsonObjectWrapper, Error> getAsObject() {
		try {
			return Result.success(new JsonObjectWrapper(json.getAsJsonObject(), path));
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("an object"));
		}
	}

	public Result<JsonArrayWrapper, Error> getAsArray() {
		try {
			return Result.success(new JsonArrayWrapper(json.getAsJsonArray(), path));
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("an array"));
		}
	}

	public Result<String, Error> getAsString() {
		try {
			return Result.success(json.getAsString());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("a string"));
		}
	}

	public Result<Float, Error> getAsFloat() {
		try {
			return Result.success(json.getAsFloat());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("a float"));
		}
	}

	public Result<Integer, Error> getAsInt() {
		try {
			return Result.success(json.getAsInt());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("an int"));
		}
	}

	public Result<Boolean, Error> getAsBoolean() {
		try {
			return Result.success(json.getAsBoolean());
		} catch (Exception e) {
			return Result.failure(path.expectedToBe("a boolean"));
		}
	}

	public JsonElement getJson() {
		return json;
	}
}
