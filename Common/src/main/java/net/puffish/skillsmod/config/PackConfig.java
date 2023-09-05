package net.puffish.skillsmod.config;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;
import net.puffish.skillsmod.utils.failure.SingleFailure;

import java.util.ArrayList;
import java.util.List;

public class PackConfig {
	private final List<String> categories;

	private PackConfig(List<String> categories) {
		this.categories = categories;
	}

	public static Result<PackConfig, Failure> parse(String name, JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(name, rootObject));
	}

	public static Result<PackConfig, Failure> parse(String name, JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var version = rootObject.getInt("version")
				.getSuccessOrElse(e -> Integer.MIN_VALUE);

		if (version < SkillsMod.CONFIG_VERSION) {
			return Result.failure(SingleFailure.of("Data pack `" + name + "` is outdated. Check out the mod's wiki to learn how to update the data pack."));
		}
		if (version > SkillsMod.CONFIG_VERSION) {
			return Result.failure(SingleFailure.of("Data pack `" + name + "` is for a newer version of the mod. Please update the mod."));
		}

		var optCategories = rootObject.getArray("categories")
				.andThen(array -> array.getAsList((i, element) -> JsonParseUtils.parseIdentifierPath(element))
						.mapFailure(ManyFailures::ofList)
				)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new PackConfig(
					optCategories.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public List<String> getCategories() {
		return categories;
	}
}
