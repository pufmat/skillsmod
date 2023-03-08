package net.puffish.skillsmod.experience.builtin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
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

public class MineBlockExperienceSource implements ExperienceSource {
	public static final Identifier ID = SkillsMod.createIdentifier("mine_block");

	private final List<Modifier> modifiers;

	private MineBlockExperienceSource(List<Modifier> modifiers) {
		this.modifiers = modifiers;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(ID, MineBlockExperienceSource::create);
	}

	private static Result<MineBlockExperienceSource, Error> create(Result<JsonElementWrapper, Error> maybeDataElement) {
		return maybeDataElement.andThen(MineBlockExperienceSource::create);
	}

	private static Result<MineBlockExperienceSource, Error> create(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(MineBlockExperienceSource::create);
	}

	private static Result<MineBlockExperienceSource, Error> create(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var modifiers = rootObject.getArray("modifiers")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(array -> array.getAsList((i, element) -> Modifier.parse(element))
						.mapFailure(ManyErrors::ofList)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		if (errors.isEmpty()) {
			return Result.success(new MineBlockExperienceSource(
					modifiers
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

		public float testAndApply(BlockState blockState, float value) {
			if (conditions.stream().anyMatch(condition -> condition.test(blockState))) {
				return apply(value);
			}
			return value;
		}
	}

	private interface Condition {
		boolean test(BlockState blockState);

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
				case "block" -> maybeDataElement.andThen(Condition.EntityCondition::parseInternal).mapSuccess(tmp -> tmp);
				case "tag" -> maybeDataElement.andThen(Condition.BlockTagCondition::parseInternal).mapSuccess(tmp -> tmp);
				default -> Result.failure(typeElementPath.errorAt("Expected a valid condition type"));
			};
		}

		final class EntityCondition implements Condition {
			private final Block block;
			private final StatePredicate state;

			public EntityCondition(Block block, StatePredicate state) {
				this.block = block;
				this.state = state;
			}

			public static Result<EntityCondition, Error> parseInternal(JsonElementWrapper rootElement) {
				return rootElement.getAsObject().andThen(EntityCondition::parseInternal);
			}

			public static Result<EntityCondition, Error> parseInternal(JsonObjectWrapper rootObject) {
				var errors = new ArrayList<Error>();

				var optBlock = rootObject.get("block")
						.andThen(JsonParseUtils::parseBlock)
						.ifFailure(errors::add)
						.getSuccess();

				var state = rootObject.get("state")
						.getSuccess()
						.flatMap(stateElement -> JsonParseUtils.parseStatePredicate(stateElement)
								.ifFailure(errors::add)
								.getSuccess()
						)
						.orElseGet(() -> StatePredicate.Builder.create().build());

				if (errors.isEmpty()) {
					return Result.success(new EntityCondition(
							optBlock.orElseThrow(),
							state
					));
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			}

			@Override
			public boolean test(BlockState blockState) {
				return blockState.isOf(block) && state.test(blockState);
			}
		}

		final class BlockTagCondition implements Condition {
			private final RegistryEntryList.Named<Block> entries;

			private BlockTagCondition(RegistryEntryList.Named<Block> entries) {
				this.entries = entries;
			}

			public static Result<BlockTagCondition, Error> parseInternal(JsonElementWrapper rootElement) {
				return rootElement.getAsObject().andThen(BlockTagCondition::parseInternal);
			}

			public static Result<BlockTagCondition, Error> parseInternal(JsonObjectWrapper rootObject) {
				var errors = new ArrayList<Error>();

				var optTag = rootObject.get("tag")
						.andThen(JsonParseUtils::parseBlockTag)
						.ifFailure(errors::add)
						.getSuccess();

				if (errors.isEmpty()) {
					return Result.success(new BlockTagCondition(
							optTag.orElseThrow()
					));
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			}

			@Override
			public boolean test(BlockState blockState) {
				return blockState.isIn(entries);
			}
		}
	}

	@Override
	public void dispose(MinecraftServer server) {

	}

	public int getValue(BlockState blockState) {
		float value = 0;
		for (var modifier : modifiers) {
			value = modifier.testAndApply(blockState, value);
		}
		return Math.round(value);
	}
}
