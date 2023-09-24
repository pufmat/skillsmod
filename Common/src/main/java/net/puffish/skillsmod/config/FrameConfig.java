package net.puffish.skillsmod.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancement.AdvancementFrame;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public class FrameConfig {
	private final String type;
	private final JsonElement data;

	private FrameConfig(String type, JsonElement data) {
		this.type = type;
		this.data = data;
	}

	public static FrameConfig fromAdvancementFrame(AdvancementFrame frame) {
		var data = new JsonObject();
		data.addProperty("frame", frame.getId());
		return new FrameConfig(
				"advancement",
				data
		);
	}

	public static Result<FrameConfig, Failure> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(FrameConfig::parse);
	}

	public static Result<FrameConfig, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var type = rootObject.getString("type")
				.ifFailure(failures::add)
				.getSuccess();

		var data = rootObject.get("data")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new FrameConfig(
					type.orElseThrow(),
					data.orElseThrow().getJson()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public String getType() {
		return type;
	}

	public JsonElement getData() {
		return data;
	}
}
