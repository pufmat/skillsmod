package net.puffish.skillsmod.config.skill;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.rewards.Reward;
import net.puffish.skillsmod.rewards.RewardRegistry;
import net.puffish.skillsmod.rewards.builtin.DummyReward;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;

public class SkillRewardConfig {
	private final Identifier type;
	private final Reward instance;

	private SkillRewardConfig(Identifier type, Reward instance) {
		this.type = type;
		this.instance = instance;
	}

	public static Result<SkillRewardConfig, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<SkillRewardConfig, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
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

		var required = rootObject.getBoolean("required")
				.getSuccessOrElse(e -> true);

		if (failures.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					rootObject.getPath().thenObject("type"),
					context
			).orElse(failure -> {
				if (required) {
					return Result.failure(failure);
				} else {
					failure.getMessages().forEach(context::addWarning);
					return Result.success(new SkillRewardConfig(DummyReward.ID, new DummyReward()));
				}
			});
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	private static Result<SkillRewardConfig, Failure> build(Identifier type, Result<JsonElementWrapper, Failure> maybeDataElement, JsonPath typePath, ConfigContext context) {
		return RewardRegistry.getFactory(type)
				.map(factory -> factory.create(maybeDataElement, context).mapSuccess(instance -> new SkillRewardConfig(type, instance)))
				.orElseGet(() -> Result.failure(typePath.createFailure("Expected a valid reward type")));
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
