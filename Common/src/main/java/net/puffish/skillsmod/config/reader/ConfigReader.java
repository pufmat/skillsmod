package net.puffish.skillsmod.config.reader;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ConfigReader {
	public abstract Result<JsonElementWrapper, Failure> read(Path path);
	public abstract boolean exists(Path path);

	public Result<Map<Identifier, CategoryConfig>, Failure> readCategories(String namespace, List<String> ids, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var map = new LinkedHashMap<Identifier, CategoryConfig>();

		for (var id : ids) {
			readCategory(namespace, id, context).peek(
					category -> map.put(Identifier.of(namespace, id), category),
					failures::add
			);
		}

		if (failures.isEmpty()) {
			return Result.success(map);
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	public Result<CategoryConfig, Failure> readCategory(String namespace, String id, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optGeneralElement = read(Path.of("categories", id, "category.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var optDefinitionsElement = read(Path.of("categories", id, "definitions.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var optSkillsElement = read(Path.of("categories", id, "skills.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var optConnectionsElement = read(Path.of("categories", id, "connections.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var optExperienceElement = Optional.<JsonElementWrapper>empty();
		var experiencePath = Path.of("categories", id, "experience.json");
		if (exists(experiencePath)) {
			optExperienceElement = read(experiencePath)
					.ifFailure(failures::add)
					.getSuccess();
		}

		if (failures.isEmpty()) {
			return CategoryConfig.parse(
					Identifier.of(namespace, id),
					optGeneralElement.orElseThrow(),
					optDefinitionsElement.orElseThrow(),
					optSkillsElement.orElseThrow(),
					optConnectionsElement.orElseThrow(),
					optExperienceElement,
					context
			);
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}
}
