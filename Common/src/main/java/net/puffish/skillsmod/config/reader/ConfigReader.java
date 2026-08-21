package net.puffish.skillsmod.config.reader;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Result;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ConfigReader {
	public abstract Result<JsonElement, Problem> read(Path path);
	public abstract boolean exists(Path path);

	public Result<Map<Identifier, CategoryConfig>, Problem> readCategories(String namespace, List<String> ids, int position, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var map = new LinkedHashMap<Identifier, CategoryConfig>();

		for (var id : ids) {
			readCategory(namespace, id, position, context)
					.ifSuccess(category -> map.put(Identifier.of(namespace, id), category))
					.ifFailure(problems::add);
			position++;
		}

		if (problems.isEmpty()) {
			return Result.success(map);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public Result<CategoryConfig, Problem> readCategory(String namespace, String id, int position, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optGeneralElement = read(Path.of("categories", id, "category.json"))
				.ifFailure(problems::add)
				.getSuccess();

		var optDefinitionsElement = read(Path.of("categories", id, "definitions.json"))
				.ifFailure(problems::add)
				.getSuccess();

		var optSkillsElement = read(Path.of("categories", id, "skills.json"))
				.ifFailure(problems::add)
				.getSuccess();

		var optConnectionsElement = read(Path.of("categories", id, "connections.json"))
				.ifFailure(problems::add)
				.getSuccess();

		var optExperienceElement = Optional.<JsonElement>empty();
		var experiencePath = Path.of("categories", id, "experience.json");
		if (exists(experiencePath)) {
			optExperienceElement = read(experiencePath)
					.ifFailure(problems::add)
					.getSuccess();
		}

		var optExchangeElement = Optional.<JsonElement>empty();
		var exchangePath = Path.of("categories", id, "exchange.json");
		if (exists(exchangePath)) {
			optExchangeElement = read(exchangePath)
					.ifFailure(problems::add)
					.getSuccess();
		}

		if (problems.isEmpty()) {
			return CategoryConfig.parse(
					Identifier.of(namespace, id),
					position,
					optGeneralElement.orElseThrow(),
					optDefinitionsElement.orElseThrow(),
					optSkillsElement.orElseThrow(),
					optConnectionsElement.orElseThrow(),
					optExperienceElement,
					optExchangeElement,
					context
			);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}
}
