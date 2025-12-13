package net.puffish.skillsmod.config;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.function.Function;

public sealed interface IconConfig permits IconConfig.EffectIconConfig, IconConfig.ItemIconConfig, IconConfig.TextureIconConfig {

	static Result<IconConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	static Result<IconConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
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

	private static Result<IconConfig, Problem> build(String type, JsonElement dataElement, JsonPath typeElementPath, ConfigContext context) {
		return switch (type) {
			case "item" -> ItemIconConfig.parse(dataElement, context).mapSuccess(Function.identity());
			case "effect" -> EffectIconConfig.parse(dataElement, context).mapSuccess(Function.identity());
			case "texture" -> TextureIconConfig.parse(dataElement, context).mapSuccess(Function.identity());
			default -> Result.failure(typeElementPath.createProblem("Expected a valid icon type"));
		};
	}

	record ItemIconConfig(ItemStack item) implements IconConfig {
		public static Result<ItemIconConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
			return BuiltinJson.parseItemStack(rootElement, context.getServer().getRegistryManager()).mapSuccess(ItemIconConfig::new);
		}
	}

	record EffectIconConfig(StatusEffect effect) implements IconConfig {
		public static Result<EffectIconConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
			return rootElement.getAsObject().andThen(
					LegacyUtils.wrapNoUnused(EffectIconConfig::parse, context)
			);
		}

		public static Result<EffectIconConfig, Problem> parse(JsonObject rootObject) {
			var problems = new ArrayList<Problem>();

			var optEffect = rootObject.get("effect")
					.andThen(BuiltinJson::parseEffect)
					.ifFailure(problems::add)
					.getSuccess();

			if (problems.isEmpty()) {
				return Result.success(new EffectIconConfig(
						optEffect.orElseThrow()
				));
			} else {
				return Result.failure(Problem.combine(problems));
			}
		}
	}

	record TextureIconConfig(Identifier texture) implements IconConfig {
		public static Result<TextureIconConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
			return rootElement.getAsObject().andThen(
					LegacyUtils.wrapNoUnused(TextureIconConfig::parse, context)
			);
		}

		public static Result<TextureIconConfig, Problem> parse(JsonObject rootObject) {
			var problems = new ArrayList<Problem>();

			var optEffect = rootObject.get("texture")
					.andThen(BuiltinJson::parseIdentifier)
					.ifFailure(problems::add)
					.getSuccess();

			if (problems.isEmpty()) {
				return Result.success(new TextureIconConfig(
						optEffect.orElseThrow()
				));
			} else {
				return Result.failure(Problem.combine(problems));
			}
		}
	}
}
