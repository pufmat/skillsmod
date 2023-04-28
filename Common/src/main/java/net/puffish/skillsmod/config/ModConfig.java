package net.puffish.skillsmod.config;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.error.SingleError;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	private final List<String> categories;

	private ModConfig(List<String> categories) {
		this.categories = categories;
	}

	public static Result<ModConfig, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(ModConfig::parse);
	}

	public static Result<ModConfig, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var version = rootObject.getInt("version")
				.getSuccessOrElse(e -> Integer.MIN_VALUE);

		if (version < SkillsMod.CONFIG_VERSION) {
			return Result.failure(SingleError.of("Configuration is outdated. Check out the mod's wiki to learn how to update the configuration."));
		}
		if (version > SkillsMod.CONFIG_VERSION) {
			return Result.failure(SingleError.of("Configuration is for a newer version of the mod. Please update the mod."));
		}

		var optCategories = rootObject.getArray("categories")
				.andThen(array -> array.getAsList((i, element) -> element.getAsString()).mapFailure(ManyErrors::ofList))
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new ModConfig(
					optCategories.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public List<String> getCategories() {
		return categories;
	}
}
