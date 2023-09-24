package net.puffish.skillsmod.config;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;
import net.puffish.skillsmod.api.utils.failure.SingleFailure;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	private final boolean showWarnings;
	private final List<String> categories;

	private ModConfig(boolean showWarnings, List<String> categories) {
		this.showWarnings = showWarnings;
		this.categories = categories;
	}

	public static Result<ModConfig, Failure> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(ModConfig::parse);
	}

	public static Result<ModConfig, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var version = rootObject.getInt("version")
				.getSuccessOrElse(e -> Integer.MIN_VALUE);

		var showWarnings = rootObject.getBoolean("show_warnings")
				.getSuccessOrElse(e -> false);

		if (version < SkillsMod.CONFIG_VERSION) {
			return Result.failure(SingleFailure.of("Configuration is outdated. Check out the mod's wiki to learn how to update the configuration."));
		}
		if (version > SkillsMod.CONFIG_VERSION) {
			return Result.failure(SingleFailure.of("Configuration is for a newer version of the mod. Please update the mod."));
		}

		var optCategories = rootObject.getArray("categories")
				.andThen(array -> array.getAsList((i, element) -> JsonParseUtils.parseIdentifierPath(element))
						.mapFailure(ManyFailures::ofList)
				)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new ModConfig(
					showWarnings,
					optCategories.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public boolean getShowWarnings() {
		return showWarnings;
	}

	public List<String> getCategories() {
		return categories;
	}
}
