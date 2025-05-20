package net.puffish.skillsmod.config.experience;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.experience.source.ExperienceSourceRegistry;
import net.puffish.skillsmod.impl.experience.source.ExperienceSourceConfigContextImpl;
import net.puffish.skillsmod.impl.experience.source.ExperienceSourceDisposeContextImpl;
import net.puffish.skillsmod.util.DisposeContext;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;

public record ExperienceSourceConfig(
		Identifier type,
		ExperienceSource instance
) {

	public static Result<ExperienceSourceConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context)
		);
	}

	public static Result<ExperienceSourceConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTypeElement = rootObject.get("type")
				.ifFailure(problems::add)
				.getSuccess();

		var optType = optTypeElement.flatMap(
				typeElement -> BuiltinJson.parseIdentifier(typeElement)
						.ifFailure(problems::add)
						.getSuccess()
		);

		var maybeDataElement = rootObject.get("data");

		if (problems.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					optTypeElement.orElseThrow().getPath(),
					context
			);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<ExperienceSourceConfig, Problem> build(Identifier type, Result<JsonElement, Problem> maybeDataElement, JsonPath typeElementPath, ConfigContext context) {
		return ExperienceSourceRegistry.getFactory(type)
				.map(factory -> factory.create(new ExperienceSourceConfigContextImpl(context, maybeDataElement))
						.mapSuccess(instance -> new ExperienceSourceConfig(type, instance))
				)
				.orElseGet(() -> Result.failure(typeElementPath.createProblem("Expected a valid source type")));
	}

	public void dispose(DisposeContext context) {
		this.instance.dispose(new ExperienceSourceDisposeContextImpl(context));
	}

}
