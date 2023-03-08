package net.puffish.skillsmod.config;

import com.google.gson.JsonElement;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.Result;

import java.util.ArrayList;

public class IconConfig {
	private final String type;
	private final JsonElement data;

	private IconConfig(String type, JsonElement data) {
		this.type = type;
		this.data = data;
	}

	public static Result<IconConfig, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(IconConfig::parse);
	}

	public static Result<IconConfig, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var type = rootObject.getString("type")
				.ifFailure(errors::add)
				.getSuccess();

		var data = rootObject.get("data")
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new IconConfig(
					type.orElseThrow(),
					data.orElseThrow().getJson()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public String getType() {
		return type;
	}

	public JsonElement getData() {
		return data;
	}
}
