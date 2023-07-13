package net.puffish.skillsmod.config.experience;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.experience.ExperienceSourceRegistry;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public class ExperienceSourceConfig {
	private final Identifier type;
	private final ExperienceSource instance;

	private ExperienceSourceConfig(Identifier type, ExperienceSource instance) {
		this.type = type;
		this.instance = instance;
	}

	public static Result<ExperienceSourceConfig, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<ExperienceSourceConfig, Error> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var errors = new ArrayList<Error>();

		var optTypeElement = rootObject.get("type")
				.ifFailure(errors::add)
				.getSuccess();

		var optType = optTypeElement.flatMap(
				typeElement -> JsonParseUtils.parseIdentifier(typeElement)
						.ifFailure(errors::add)
						.getSuccess()
		);

		var maybeDataElement = rootObject.get("data");

		if (errors.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					optTypeElement.orElseThrow().getPath(),
					context
			);
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	private static Result<ExperienceSourceConfig, Error> build(Identifier type, Result<JsonElementWrapper, Error> maybeDataElement, JsonPath typeElementPath, ConfigContext context) {
		return ExperienceSourceRegistry.getFactory(type)
				.map(factory -> factory.create(maybeDataElement, context).mapSuccess(instance -> new ExperienceSourceConfig(type, instance)))
				.orElseGet(() -> Result.failure(typeElementPath.errorAt("Expected a valid source type")));
	}

	public void dispose(MinecraftServer server) {
		this.instance.dispose(server);
	}

	public Identifier getType() {
		return type;
	}

	public ExperienceSource getInstance() {
		return instance;
	}
}
