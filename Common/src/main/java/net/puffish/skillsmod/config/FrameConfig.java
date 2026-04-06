package net.puffish.skillsmod.config;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public sealed interface FrameConfig permits FrameConfig.AdvancementFrameConfig, FrameConfig.TextureFrameConfig {

	static FrameConfig createDefault() {
		return new AdvancementFrameConfig(AdvancementType.TASK);
	}

	static Result<FrameConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().flatMap(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context),
				failure -> BuiltinJson.parseAdvancementType(rootElement)
						.mapSuccess(AdvancementFrameConfig::new)
		);
	}

	static Result<FrameConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTypeElement = rootObject.get("type")
				.ifFailure(problems::add)
				.getSuccess();

		var optType = optTypeElement.flatMap(
				typeElement -> typeElement.getAsString()
						.ifFailure(problems::add)
						.getSuccess()
		);

		var optData = rootObject.get("data")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return build(
					optType.orElseThrow(),
					optData.orElseThrow(),
					optTypeElement.orElseThrow().getPath(),
					context
			);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<FrameConfig, Problem> build(String type, JsonElement dataElement, JsonPath typeElementPath, ConfigContext context) {
		return switch (type) {
			case "advancement" -> AdvancementFrameConfig.parse(dataElement, context).mapSuccess(Function.identity());
			case "texture" -> TextureFrameConfig.parse(dataElement, context).mapSuccess(Function.identity());
			default -> Result.failure(typeElementPath.createProblem("Expected a valid icon type"));
		};
	}

	record AdvancementFrameConfig(AdvancementType frame) implements FrameConfig {
		public static Result<AdvancementFrameConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
			return rootElement.getAsObject().andThen(
					LegacyUtils.wrapNoUnused(AdvancementFrameConfig::parse, context)
			);
		}

		public static Result<AdvancementFrameConfig, Problem> parse(JsonObject rootObject) {
			var problems = new ArrayList<Problem>();

			var optEffect = rootObject.get("frame")
					.andThen(BuiltinJson::parseAdvancementType)
					.ifFailure(problems::add)
					.getSuccess();

			if (problems.isEmpty()) {
				return Result.success(new AdvancementFrameConfig(
						optEffect.orElseThrow()
				));
			} else {
				return Result.failure(Problem.combine(problems));
			}
		}
	}

	record TextureFrameConfig(
			Optional<Identifier> lockedTexture,
			Identifier availableTexture,
			Optional<Identifier> affordableTexture,
			Identifier unlockedTexture,
			Optional<Identifier> excludedTexture
	) implements FrameConfig {
		public static Result<TextureFrameConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
			return rootElement.getAsObject().andThen(
					LegacyUtils.wrapNoUnused(TextureFrameConfig::parse, context)
			);
		}

		private static Result<TextureFrameConfig, Problem> parse(JsonObject rootObject) {
			var problems = new ArrayList<Problem>();

			var optAffordableTexture = rootObject.get("affordable")
					.getSuccess() // ignore failure because this property is optional
					.flatMap(element -> BuiltinJson.parseIdentifier(element)
							.ifFailure(problems::add)
							.getSuccess()
					);

			var optAvailableTexture = rootObject.get("available")
					.andThen(BuiltinJson::parseIdentifier)
					.ifFailure(problems::add)
					.getSuccess();

			var optLockedTexture = rootObject.get("locked")
					.getSuccess() // ignore failure because this property is optional
					.flatMap(element -> BuiltinJson.parseIdentifier(element)
							.ifFailure(problems::add)
							.getSuccess()
					);

			var optUnlockedTexture = rootObject.get("unlocked")
					.andThen(BuiltinJson::parseIdentifier)
					.ifFailure(problems::add)
					.getSuccess();

			var optExcludedTexture = rootObject.get("excluded")
					.getSuccess() // ignore failure because this property is optional
					.flatMap(element -> BuiltinJson.parseIdentifier(element)
							.ifFailure(problems::add)
							.getSuccess()
					);

			if (problems.isEmpty()) {
				return Result.success(new TextureFrameConfig(
						optLockedTexture,
						optAvailableTexture.orElseThrow(),
						optAffordableTexture,
						optUnlockedTexture.orElseThrow(),
						optExcludedTexture
				));
			} else {
				return Result.failure(Problem.combine(problems));
			}
		}
	}
}
