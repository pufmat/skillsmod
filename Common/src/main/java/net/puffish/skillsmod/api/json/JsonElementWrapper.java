package net.puffish.skillsmod.api.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.impl.json.JsonElementWrapperImpl;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public interface JsonElementWrapper extends JsonWrapper {
	static Result<JsonElementWrapper, Failure> parseString(String jsonData, JsonPath jsonPath) {
		return parseReader(new StringReader(jsonData), jsonPath);
	}

	static Result<JsonElementWrapper, Failure> parseReader(Reader reader, JsonPath jsonPath) {
		try {
			return Result.success(new JsonElementWrapperImpl(
					JsonParser.parseReader(reader),
					jsonPath
			));
		} catch (Exception e) {
			return Result.failure(jsonPath.createFailure("Could not parse JSON due to malformed syntax"));
		}
	}

	static Result<JsonElementWrapper, Failure> parseFile(Path filePath, JsonPath jsonPath) {
		try {
			var content = Files.readString(filePath);
			if (content.isEmpty()) {
				return Result.failure(jsonPath.createFailure("File is empty"));
			}
			return parseString(content, jsonPath);
		} catch (Exception e) {
			return Result.failure(jsonPath.createFailure("Could not read file"));
		}
	}

	Result<JsonObjectWrapper, Failure> getAsObject();

	Result<JsonArrayWrapper, Failure> getAsArray();

	Result<String, Failure> getAsString();

	Result<Float, Failure> getAsFloat();

	Result<Double, Failure> getAsDouble();

	Result<Integer, Failure> getAsInt();

	Result<Boolean, Failure> getAsBoolean();

	JsonElement getJson();
}
