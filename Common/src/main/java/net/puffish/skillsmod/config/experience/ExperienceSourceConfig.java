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
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public class ExperienceSourceConfig {
	private final Identifier type;
	private final ExperienceSource instance;

	private ExperienceSourceConfig(Identifier type, ExperienceSource instance) {
		this.type = type;
		this.instance = instance;
	}

	public static Result<ExperienceSourceConfig, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<ExperienceSourceConfig, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optTypeElement = rootObject.get("type")
				.ifFailure(failures::add)
				.getSuccess();

		var optType = optTypeElement.flatMap(
				typeElement -> JsonParseUtils.parseIdentifier(typeElement)
						.ifFailure(failures::add)
						.getSuccess()
		);

		var maybeDataElement = rootObject.get("data");

		if (failures.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					optTypeElement.orElseThrow().getPath(),
					context
			);
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	private static Result<ExperienceSourceConfig, Failure> build(Identifier type, Result<JsonElementWrapper, Failure> maybeDataElement, JsonPath typeElementPath, ConfigContext context) {
		return ExperienceSourceRegistry.getFactory(type)
				.map(factory -> factory.create(maybeDataElement, context).mapSuccess(instance -> new ExperienceSourceConfig(type, instance)))
				.orElseGet(() -> Result.failure(typeElementPath.failureAt("Expected a valid source type")));
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
