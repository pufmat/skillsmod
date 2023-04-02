package net.puffish.skillsmod.experience.builtin;

import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.experience.ExperienceSource;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;
import java.util.List;

public class KillEntityExperienceSource implements ExperienceSource {
	public static final Identifier ID = SkillsMod.createIdentifier("kill_entity");

	private final List<Modifier> modifiers;
	private final AntiFarming antiFarming;

	public KillEntityExperienceSource(List<Modifier> modifiers, AntiFarming antiFarming) {
		this.modifiers = modifiers;
		this.antiFarming = antiFarming;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(ID, KillEntityExperienceSource::create);
	}

	private static Result<KillEntityExperienceSource, Error> create(Result<JsonElementWrapper, Error> maybeDataElement) {
		return maybeDataElement.andThen(KillEntityExperienceSource::create);
	}

	private static Result<KillEntityExperienceSource, Error> create(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(KillEntityExperienceSource::create);
	}

	private static Result<KillEntityExperienceSource, Error> create(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var cases = rootObject.getArray("modifiers")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(array -> array.getAsList((i, element) -> Modifier.parse(element))
						.mapFailure(ManyErrors::ofList)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		var optAntiFarming = rootObject.get("anti_farming")
				.andThen(AntiFarming::parse)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new KillEntityExperienceSource(
					cases,
					optAntiFarming.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	private abstract static class Modifier {
		private final List<Condition> conditions;

		private Modifier(List<Condition> conditions) {
			this.conditions = conditions;
		}

		public static Result<Modifier, Error> parse(JsonElementWrapper rootElement) {
			return rootElement.getAsObject()
					.andThen(Modifier::parse);
		}

		public static Result<Modifier, Error> parse(JsonObjectWrapper rootObject) {
			var errors = new ArrayList<Error>();

			var conditions = rootObject.getArray("conditions")
					.getSuccess() // ignore failure because this property is optional
					.flatMap(array -> array.getAsList((i, element) -> Condition.parse(element))
							.mapFailure(ManyErrors::ofList)
							.ifFailure(errors::add)
							.getSuccess()
					)
					.orElseGet(List::of);

			var optTypeElement = rootObject.get("type")
					.ifFailure(errors::add)
					.getSuccess();

			var optType = optTypeElement.flatMap(
					typeElement -> typeElement.getAsString()
							.ifFailure(errors::add)
							.getSuccess()
			);

			var maybeDataElement = rootObject.get("data");

			if (errors.isEmpty()) {
				return build(
						conditions,
						optType.orElseThrow(),
						maybeDataElement,
						optTypeElement.orElseThrow().getPath()
				);
			} else {
				return Result.failure(ManyErrors.ofList(errors));
			}
		}

		private static Result<Modifier, Error> build(List<Condition> conditions, String type, Result<JsonElementWrapper, Error> maybeDataElement, JsonPath typeElementPath) {
			return switch (type) {
				case "addition" -> maybeDataElement.andThen(dataElement -> AdditionModifier.parseInternal(conditions, dataElement).mapSuccess(tmp -> tmp));
				case "multiply" -> maybeDataElement.andThen(dataElement -> MultiplyModifier.parseInternal(conditions, dataElement).mapSuccess(tmp -> tmp));
				case "fixed" -> maybeDataElement.andThen(dataElement -> FixedModifier.parseInternal(conditions, dataElement).mapSuccess(tmp -> tmp));
				default -> Result.failure(typeElementPath.errorAt("Expected a valid modifier type"));
			};
		}

		public static final class AdditionModifier extends Modifier {
			private final float value;

			private AdditionModifier(List<Condition> conditions, float value) {
				super(conditions);
				this.value = value;
			}

			public static Result<AdditionModifier, Error> parseInternal(List<Condition> conditions, JsonElementWrapper rootElement) {
				return rootElement.getAsObject()
						.andThen(rootObject -> AdditionModifier.parseInternal(conditions, rootObject));
			}

			public static Result<AdditionModifier, Error> parseInternal(List<Condition> conditions, JsonObjectWrapper rootObject) {
				var errors = new ArrayList<Error>();

				var optValue = rootObject.getFloat("value")
						.ifFailure(errors::add)
						.getSuccess();

				if (errors.isEmpty()) {
					return Result.success(new AdditionModifier(
							conditions,
							optValue.orElseThrow()
					));
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			}

			@Override
			public float apply(float value) {
				return this.value + value;
			}
		}

		public static final class MultiplyModifier extends Modifier {
			private final float value;

			private MultiplyModifier(List<Condition> conditions, float value) {
				super(conditions);
				this.value = value;
			}

			public static Result<MultiplyModifier, Error> parseInternal(List<Condition> conditions, JsonElementWrapper rootElement) {
				return rootElement.getAsObject()
						.andThen(rootObject -> MultiplyModifier.parseInternal(conditions, rootObject));
			}

			public static Result<MultiplyModifier, Error> parseInternal(List<Condition> conditions, JsonObjectWrapper rootObject) {
				var errors = new ArrayList<Error>();

				var optValue = rootObject.getFloat("value")
						.ifFailure(errors::add)
						.getSuccess();

				if (errors.isEmpty()) {
					return Result.success(new MultiplyModifier(
							conditions,
							optValue.orElseThrow()
					));
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			}

			@Override
			public float apply(float value) {
				return this.value * value;
			}
		}

		public static final class FixedModifier extends Modifier {
			private final float value;

			private FixedModifier(List<Condition> conditions, float value) {
				super(conditions);
				this.value = value;
			}

			public static Result<FixedModifier, Error> parseInternal(List<Condition> conditions, JsonElementWrapper rootElement) {
				return rootElement.getAsObject()
						.andThen(rootObject -> FixedModifier.parseInternal(conditions, rootObject));
			}

			public static Result<FixedModifier, Error> parseInternal(List<Condition> conditions, JsonObjectWrapper rootObject) {
				var errors = new ArrayList<Error>();

				var optValue = rootObject.getFloat("value")
						.ifFailure(errors::add)
						.getSuccess();

				if (errors.isEmpty()) {
					return Result.success(new FixedModifier(
							conditions,
							optValue.orElseThrow()
					));
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			}

			@Override
			public float apply(float value) {
				return this.value;
			}
		}

		protected abstract float apply(float value);

		public float testAndApply(EntityType<?> entityType, float value) {
			if (conditions.stream().anyMatch(condition -> condition.test(entityType))) {
				return apply(value);
			}
			return value;
		}
	}

	private interface Condition {
		boolean test(EntityType<?> entityType);

		static Result<Condition, Error> parse(JsonElementWrapper rootElement) {
			return rootElement.getAsObject().andThen(Condition::parse);
		}

		static Result<Condition, Error> parse(JsonObjectWrapper rootObject) {
			var errors = new ArrayList<Error>();

			var optType = rootObject.getString("type")
					.ifFailure(errors::add)
					.getSuccess();

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

		private static Result<Condition, Error> build(String type, Result<JsonElementWrapper, Error> maybeDataElement, JsonPath typeElementPath) {
			return switch (type) {
				case "entity" -> maybeDataElement.andThen(EntityCondition::parseInternal).mapSuccess(tmp -> tmp);
				case "tag" -> maybeDataElement.andThen(EntityTagCondition::parseInternal).mapSuccess(tmp -> tmp);
				default -> Result.failure(typeElementPath.errorAt("Expected a valid condition type"));
			};
		}

		final class EntityCondition implements Condition {
			private final EntityType<?> entityType;

			private EntityCondition(EntityType<?> entityType) {
				this.entityType = entityType;
			}

			public static Result<EntityCondition, Error> parseInternal(JsonElementWrapper rootElement) {
				return rootElement.getAsObject().andThen(EntityCondition::parseInternal);
			}

			public static Result<EntityCondition, Error> parseInternal(JsonObjectWrapper rootObject) {
				var errors = new ArrayList<Error>();

				var optEntity = rootObject.get("entity")
						.andThen(JsonParseUtils::parseEntityType)
						.ifFailure(errors::add)
						.getSuccess();

				if (errors.isEmpty()) {
					return Result.success(new EntityCondition(
							optEntity.orElseThrow()
					));
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			}

			@Override
			public boolean test(EntityType<?> entityType) {
				return this.entityType == entityType;
			}
		}

		final class EntityTagCondition implements Condition {
			private final RegistryEntryList.Named<EntityType<?>> entries;

			private EntityTagCondition(RegistryEntryList.Named<EntityType<?>> entries) {
				this.entries = entries;
			}

			public static Result<EntityTagCondition, Error> parseInternal(JsonElementWrapper rootElement) {
				return rootElement.getAsObject().andThen(EntityTagCondition::parseInternal);
			}

			public static Result<EntityTagCondition, Error> parseInternal(JsonObjectWrapper rootObject) {
				var errors = new ArrayList<Error>();

				var optTag = rootObject.get("tag")
						.andThen(JsonParseUtils::parseEntityTypeTag)
						.ifFailure(errors::add)
						.getSuccess();

				if (errors.isEmpty()) {
					return Result.success(new EntityTagCondition(
							optTag.orElseThrow()
					));
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			}

			@Override
			public boolean test(EntityType<?> entityType) {
				return Registry.ENTITY_TYPE.getKey(entityType)
						.map(key -> entries.contains(Registry.ENTITY_TYPE.entryOf(key)))
						.orElse(false);
			}
		}
	}

	public record AntiFarming(boolean enabled, int limitPerChunk, int resetAfterSeconds) {
		public static Result<AntiFarming, Error> parse(JsonElementWrapper rootElement) {
			return rootElement.getAsObject()
					.andThen(AntiFarming::parse);
		}

		public static Result<AntiFarming, Error> parse(JsonObjectWrapper rootObject) {
			var errors = new ArrayList<Error>();

			var enabled = rootObject.getBoolean("enabled")
					.ifFailure(errors::add)
					.getSuccess();

			var limitPerChunk = rootObject.getInt("limit_per_chunk")
					.ifFailure(errors::add)
					.getSuccess();

			var resetAfterSeconds = rootObject.getInt("reset_after_seconds")
					.ifFailure(errors::add)
					.getSuccess();

			if (errors.isEmpty()) {
				return Result.success(new AntiFarming(
						enabled.orElseThrow(),
						limitPerChunk.orElseThrow(),
						resetAfterSeconds.orElseThrow()
				));
			} else {
				return Result.failure(ManyErrors.ofList(errors));
			}
		}
	}

	@Override
	public void dispose(MinecraftServer server) {

	}

	public int getValue(EntityType<?> entityType, int value) {
		float fValue = value;
		for (var modifier : modifiers) {
			fValue = modifier.testAndApply(entityType, fValue);
		}
		return Math.round(fValue);
	}

	public AntiFarming getAntiFarming() {
		return antiFarming;
	}
}
