package net.puffish.skillsmod.config.reader;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class ConfigReader {
	public abstract Result<JsonElementWrapper, Failure> read(Path path);

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
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public Result<CategoryConfig, Failure> readCategory(String namespace, String id, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var generalElement = read(Path.of("categories", id, "category.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var definitionsElement = read(Path.of("categories", id, "definitions.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var skillsElement = read(Path.of("categories", id, "skills.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var connectionsElement = read(Path.of("categories", id, "connections.json"))
				.ifFailure(failures::add)
				.getSuccess();

		var experienceElement = read(Path.of("categories", id, "experience.json"))
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return CategoryConfig.parse(
					Identifier.of(namespace, id),
					generalElement.orElseThrow(),
					definitionsElement.orElseThrow(),
					skillsElement.orElseThrow(),
					connectionsElement.orElseThrow(),
					experienceElement.orElseThrow(),
					context
			);
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}
}
