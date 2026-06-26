package net.puffish.skillsmod.api.json;

import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.function.Function;

public final class BuiltinJson {
	private BuiltinJson() { }

	public static Result<Identifier, Problem> parseIdentifier(JsonElement element) {
		return parseFromString(
				element,
				Identifier::parse,
				"identifier"
		);
	}

	public static Result<String, Problem> parseIdentifierPath(JsonElement element) {
		return parseFromString(
				element,
				s -> Identifier.withDefaultNamespace(s).getPath(),
				"identifier path"
		);
	}

	public static Result<MobEffect, Problem> parseEffect(JsonElement element) {
		return parseSomething(
				element,
				BuiltInRegistries.MOB_EFFECT,
				"effect"
		);
	}

	public static Result<HolderSet<MobEffect>, Problem> parseEffectTag(JsonElement element) {
		return parseSomethingTag(
				element,
				BuiltInRegistries.MOB_EFFECT,
				"effect"
		);
	}

	public static Result<HolderSet<MobEffect>, Problem> parseEffectOrEffectTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				BuiltInRegistries.MOB_EFFECT,
				"effect"
		);
	}

	public static Result<Block, Problem> parseBlock(JsonElement element) {
		return parseSomething(
				element,
				BuiltInRegistries.BLOCK,
				"block"
		);
	}

	public static Result<HolderSet<Block>, Problem> parseBlockTag(JsonElement element) {
		return parseSomethingTag(
				element,
				BuiltInRegistries.BLOCK,
				"block"
		);
	}

	public static Result<HolderSet<Block>, Problem> parseBlockOrBlockTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				BuiltInRegistries.BLOCK,
				"block"
		);
	}

	public static Result<DamageType, Problem> parseDamageType(JsonElement element, RegistryAccess manager) {
		return parseSomething(
				element,
				manager.lookupOrThrow(Registries.DAMAGE_TYPE),
				"damage type"
		);
	}

	public static Result<HolderSet<DamageType>, Problem> parseDamageTypeTag(JsonElement element, RegistryAccess manager) {
		return parseSomethingTag(
				element,
				manager.lookupOrThrow(Registries.DAMAGE_TYPE),
				"damage type"
		);
	}

	public static Result<HolderSet<DamageType>, Problem> parseDamageTypeOrDamageTypeTag(JsonElement element, RegistryAccess manager) {
		return parseSomethingOrSomethingTag(
				element,
				manager.lookupOrThrow(Registries.DAMAGE_TYPE),
				"damage type"
		);
	}

	public static Result<EntityType<?>, Problem> parseEntityType(JsonElement element) {
		return parseSomething(
				element,
				BuiltInRegistries.ENTITY_TYPE,
				"entity type"
		);
	}

	public static Result<HolderSet<EntityType<?>>, Problem> parseEntityTypeTag(JsonElement element) {
		return parseSomethingTag(
				element,
				BuiltInRegistries.ENTITY_TYPE,
				"entity type"
		);
	}

	public static Result<HolderSet<EntityType<?>>, Problem> parseEntityTypeOrEntityTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				BuiltInRegistries.ENTITY_TYPE,
				"entity type"
		);
	}

	public static Result<Item, Problem> parseItem(JsonElement element) {
		return parseSomething(
				element,
				BuiltInRegistries.ITEM,
				"item"
		);
	}

	public static Result<HolderSet<Item>, Problem> parseItemTag(JsonElement element) {
		return parseSomethingTag(
				element,
				BuiltInRegistries.ITEM,
				"item"
		);
	}

	public static Result<HolderSet<Item>, Problem> parseItemOrItemTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				BuiltInRegistries.ITEM,
				"item"
		);
	}

	public static Result<StatType<?>, Problem> parseStatType(JsonElement element) {
		return parseSomething(
				element,
				BuiltInRegistries.STAT_TYPE,
				"stat type"
		);
	}

	public static Result<HolderSet<StatType<?>>, Problem> parseStatTypeTag(JsonElement element) {
		return parseSomethingTag(
				element,
				BuiltInRegistries.STAT_TYPE,
				"stat type"
		);
	}

	public static Result<HolderSet<StatType<?>>, Problem> parseStatTypeOrStatTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				BuiltInRegistries.STAT_TYPE,
				"stat type"
		);
	}

	public static Result<StatePropertiesPredicate, Problem> parseStatePropertiesPredicate(JsonElement element) {
		try {
			return Result.success(StatePropertiesPredicate.CODEC.parse(JsonOps.INSTANCE, element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected state properties predicate"));
		}
	}

	public static Result<NbtPredicate, Problem> parseNbtPredicate(JsonElement element) {
		return parseFromString(
				element,
				s -> {
					try {
						return new NbtPredicate(TagParser.parseCompoundFully(s));
					} catch (Exception e) {
						throw  new RuntimeException(e);
					}
				},
				"nbt predicate"
		);
	}

	public static Result<DataComponentMatchers, Problem> parseDataComponentMatchers(JsonElement element, RegistryAccess manager) {
		try {
			return Result.success(DataComponentMatchers.CODEC.codec().parse(manager.createSerializationContext(JsonOps.INSTANCE), element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected data component matchers"));
		}
	}

	public static Result<Stat<?>, Problem> parseStat(JsonElement element) {
		return parseFromIdentifier(
				element,
				id -> getOrCreateStat(
						BuiltInRegistries.STAT_TYPE.getOptional(
								Identifier.bySeparator(id.getNamespace(), '.')
						).orElseThrow(),
						Identifier.bySeparator(id.getPath(), '.')
				),
				"stat"
		);
	}

	private static <T> Stat<T> getOrCreateStat(StatType<T> statType, Identifier id) {
		return statType.get(statType.getRegistry().getOptional(id).orElseThrow());
	}

	public static Result<Criterion<?>, Problem> parseCriterion(JsonElement element, RegistryAccess manager) {
		try {
			return Result.success(Criterion.CODEC.parse(manager.createSerializationContext(JsonOps.INSTANCE), element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected advancement criterion"));
		}
	}

	public static Result<CompoundTag, Problem> parseCompoundTag(JsonElement element) {
		return parseFromString(
				element,
				s -> {
					try {
						return TagParser.parseCompoundFully(s);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				},
				"nbt compound tag"
		);
	}

	public static Result<DataComponentPatch, Problem> parseDataComponentPatch(JsonElement element, RegistryAccess manager) {
		try {
			var ops = manager == null ? JsonOps.INSTANCE : manager.createSerializationContext(JsonOps.INSTANCE);
			return Result.success(DataComponentPatch.CODEC.parse(ops, element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected data component patch"));
		}
	}

	@Deprecated
	public static Result<ItemStack, Problem> parseItemStack(JsonElement element) {
		return parseItemStack(element, null);
	}

	public static Result<ItemStack, Problem> parseItemStack(JsonElement element, RegistryAccess manager) {
		try {
			return element.getAsObject().andThen(rootObject -> {
				var problems = new ArrayList<Problem>();

				var item = rootObject.get("item")
						.andThen(BuiltinJson::parseItem)
						.ifFailure(problems::add)
						.getSuccess();

				var components = rootObject.get("components")
						.getSuccess()
						.flatMap(nbtElement -> BuiltinJson.parseDataComponentPatch(nbtElement, manager)
								.ifFailure(problems::add)
								.getSuccess()
						);

				if (problems.isEmpty()) {
					var itemStack = new ItemStack(item.orElseThrow());
					components.ifPresent(itemStack::applyComponentsAndValidate);
					return Result.success(itemStack);
				} else {
					return Result.failure(Problem.combine(problems));
				}
			});
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected item stack"));
		}
	}

	public static Result<AdvancementType, Problem> parseAdvancementType(JsonElement element) {
		try {
			return Result.success(AdvancementType.CODEC.parse(JsonOps.INSTANCE, element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected advancement frame type"));
		}
	}

	public static Result<Component, Problem> parseComponent(JsonElement element, RegistryAccess manager) {
		try {
			return Result.success(ComponentSerialization.CODEC.parse(manager.createSerializationContext(JsonOps.INSTANCE), element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected text component"));
		}
	}

	public static Result<Attribute, Problem> parseAttribute(JsonElement element) {
		return parseFromIdentifier(
				element,
				id -> {
					if (id.getNamespace().equals(SkillsAPI.MOD_ID)) {
					id = Identifier.fromNamespaceAndPath("puffish_attributes", id.getPath());
					}
					return BuiltInRegistries.ATTRIBUTE.getOptional(id).orElseThrow();
				},
				"attribute"
		);
	}

	public static Result<AttributeModifier.Operation, Problem> parseAttributeModifierOperation(JsonElement element) {
		return parseFromString(
				element,
				s -> switch (s) {
					case "add", "add_value", "addition" -> AttributeModifier.Operation.ADD_VALUE;
					case "multiply_base", "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
					case "multiply_total", "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
					default -> throw new RuntimeException();
				},
				"attribute modifier operation"
		);
	}

	private static <T> Result<T, Problem> parseFromString(
			JsonElement element,
			Function<String, T> parser,
			String what
	) {
		try {
			var s = element.getJson().getAsString();
			try {
				return Result.success(parser.apply(s));
			} catch (Exception ignored) {
				return Result.failure(element.getPath().createProblem("Invalid " + what + " `" + s + "`"));
			}
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem("Expected " + what));
		}
	}

	private static <T> Result<T, Problem> parseFromIdentifier(
			JsonElement element,
			Function<Identifier, T> parser,
			String what
	) {
		return parseFromString(element, Identifier::parse, what)
				.andThen(id -> {
					try {
						return Result.success(parser.apply(id));
					} catch (Exception ignored) {
						return Result.failure(element.getPath().createProblem("Unknown " + what + " `" + id + "`"));
					}
				});
	}

	private static <T> Result<T, Problem> parseSomething(
			JsonElement element,
			Registry<T> registry,
			String what
	) {
		return parseFromIdentifier(
				element,
				id -> registry.getOptional(id).orElseThrow(),
				what
		);
	}

	private static <T> Result<HolderSet<T>, Problem> parseSomethingTag(
			JsonElement element,
			Registry<T> registry,
			String what
	) {
		return parseFromString(
				element,
				s -> s.startsWith("#") ? Identifier.parse(s.substring(1)) : Identifier.parse(s),
				what
		).andThen(id -> {
			try {
				return Result.success(registry
						.get(TagKey.create(registry.key(), id))
						.orElseThrow());
			} catch (Exception ignored) {
				return Result.failure(element.getPath().createProblem("Unknown " + what + " tag `" + id + "`"));
			}
		});
	}

	private static <T> Result<HolderSet<T>, Problem> parseSomethingOrSomethingTag(
			JsonElement element,
			Registry<T> registry,
			String what
	) {
		try {
			var s = element.getJson().getAsString();
			if (s.startsWith("#")) {
				try {
					var id = Identifier.parse(s.substring(1));
					try {
						return Result.success(registry.get(TagKey.create(registry.key(), id)).orElseThrow());
					} catch (Exception ignored) {
						return Result.failure(element.getPath().createProblem("Unknown " + what + " tag `" + id + "`"));
					}
				} catch (Exception ignored) {
					return Result.failure(element.getPath().createProblem("Invalid " + what + " tag `" + s + "`"));
				}
			} else {
				try {
					var id = Identifier.parse(s);
					try {
						return Result.success(HolderSet.direct(registry.get(id).orElseThrow()));
					} catch (Exception ignored) {
						return Result.failure(element.getPath().createProblem("Unknown " + what + " `" + id + "`"));
					}
				} catch (Exception ignored) {
					return Result.failure(element.getPath().createProblem("Invalid " + what + " `" + s + "`"));
				}
			}
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem("Expected " + what));
		}
	}
}
