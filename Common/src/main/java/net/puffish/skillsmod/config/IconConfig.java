package net.puffish.skillsmod.config;

import com.google.gson.JsonElement;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;

public class IconConfig {
	private final String type;
	private final JsonElement data;

	private IconConfig(String type, JsonElement data) {
		this.type = type;
		this.data = data;
	}

	public static Result<IconConfig, Failure> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(IconConfig::parse);
	}

	public static Result<IconConfig, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var type = rootObject.getString("type")
				.ifFailure(failures::add)
				.getSuccess();

		var data = rootObject.get("data")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new IconConfig(
					type.orElseThrow(),
					data.orElseThrow().getJson()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	public String getType() {
		return type;
	}

	public JsonElement getData() {
		return data;
	}
}
