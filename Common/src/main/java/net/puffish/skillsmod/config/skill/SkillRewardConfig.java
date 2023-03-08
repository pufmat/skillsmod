package net.puffish.skillsmod.config.skill;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.rewards.Reward;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.rewards.RewardRegistry;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.Result;

import java.util.ArrayList;

public class SkillRewardConfig {
	private final Identifier type;
	private final Reward instance;

	private SkillRewardConfig(Identifier type, Reward instance) {
		this.type = type;
		this.instance = instance;
	}

	public static Result<SkillRewardConfig, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(SkillRewardConfig::parse);
	}

	public static Result<SkillRewardConfig, Error> parse(JsonObjectWrapper rootObject) {
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
					rootObject.getPath().thenObject("type")
			);
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	private static Result<SkillRewardConfig, Error> build(Identifier type, Result<JsonElementWrapper, Error> maybeDataElement, JsonPath typePath) {
		return RewardRegistry.getFactory(type)
				.map(factory -> factory.create(maybeDataElement).mapSuccess(instance -> new SkillRewardConfig(type, instance)))
				.orElseGet(() -> Result.failure(typePath.errorAt("Expected a valid reward type")));
	}

	public void dispose(MinecraftServer server) {
		this.instance.dispose(server);
	}

	public Identifier getType() {
		return type;
	}

	public Reward getInstance() {
		return instance;
	}
}
