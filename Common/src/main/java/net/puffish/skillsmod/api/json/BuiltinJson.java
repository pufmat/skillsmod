package net.puffish.skillsmod.api.json;

import com.mojang.serialization.JsonOps;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentChanges;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
				Identifier::of,
				"identifier"
		);
	}

	public static Result<String, Problem> parseIdentifierPath(JsonElement element) {
		return parseFromString(
				element,
				s -> Identifier.ofVanilla(s).getPath(),
				"identifier path"
		);
	}

	public static Result<StatusEffect, Problem> parseEffect(JsonElement element) {
		return parseSomething(
				element,
				Registries.STATUS_EFFECT,
				"effect"
		);
	}

	public static Result<RegistryEntryList<StatusEffect>, Problem> parseEffectTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.STATUS_EFFECT,
				"effect"
		);
	}

	public static Result<RegistryEntryList<StatusEffect>, Problem> parseEffectOrEffectTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.STATUS_EFFECT,
				"effect"
		);
	}

	public static Result<Block, Problem> parseBlock(JsonElement element) {
		return parseSomething(
				element,
				Registries.BLOCK,
				"block"
		);
	}

	public static Result<RegistryEntryList<Block>, Problem> parseBlockTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.BLOCK,
				"block"
		);
	}

	public static Result<RegistryEntryList<Block>, Problem> parseBlockOrBlockTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.BLOCK,
				"block"
		);
	}

	public static Result<DamageType, Problem> parseDamageType(JsonElement element, DynamicRegistryManager manager) {
		return parseSomething(
				element,
				manager.getOrThrow(RegistryKeys.DAMAGE_TYPE),
				"damage type"
		);
	}

	public static Result<RegistryEntryList<DamageType>, Problem> parseDamageTypeTag(JsonElement element, DynamicRegistryManager manager) {
		return parseSomethingTag(
				element,
				manager.getOrThrow(RegistryKeys.DAMAGE_TYPE),
				"damage type"
		);
	}

	public static Result<RegistryEntryList<DamageType>, Problem> parseDamageTypeOrDamageTypeTag(JsonElement element, DynamicRegistryManager manager) {
		return parseSomethingOrSomethingTag(
				element,
				manager.getOrThrow(RegistryKeys.DAMAGE_TYPE),
				"damage type"
		);
	}

	public static Result<EntityType<?>, Problem> parseEntityType(JsonElement element) {
		return parseSomething(
				element,
				Registries.ENTITY_TYPE,
				"entity type"
		);
	}

	public static Result<RegistryEntryList<EntityType<?>>, Problem> parseEntityTypeTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.ENTITY_TYPE,
				"entity type"
		);
	}

	public static Result<RegistryEntryList<EntityType<?>>, Problem> parseEntityTypeOrEntityTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.ENTITY_TYPE,
				"entity type"
		);
	}

	public static Result<Item, Problem> parseItem(JsonElement element) {
		return parseSomething(
				element,
				Registries.ITEM,
				"item"
		);
	}

	public static Result<RegistryEntryList<Item>, Problem> parseItemTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.ITEM,
				"item"
		);
	}

	public static Result<RegistryEntryList<Item>, Problem> parseItemOrItemTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.ITEM,
				"item"
		);
	}

	public static Result<StatType<?>, Problem> parseStatType(JsonElement element) {
		return parseSomething(
				element,
				Registries.STAT_TYPE,
				"stat type"
		);
	}

	public static Result<RegistryEntryList<StatType<?>>, Problem> parseStatTypeTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.STAT_TYPE,
				"stat type"
		);
	}

	public static Result<RegistryEntryList<StatType<?>>, Problem> parseStatTypeOrStatTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.STAT_TYPE,
				"stat type"
		);
	}

	public static Result<StatePredicate, Problem> parseStatePredicate(JsonElement element) {
		try {
			return Result.success(StatePredicate.CODEC.parse(JsonOps.INSTANCE, element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected state predicate"));
		}
	}

	public static Result<NbtPredicate, Problem> parseNbtPredicate(JsonElement element) {
		return parseFromString(
				element,
				s -> {
					try {
						return new NbtPredicate(StringNbtReader.readCompound(s));
					} catch (Exception e) {
						throw  new RuntimeException(e);
					}
				},
				"nbt predicate"
		);
	}

	public static Result<ComponentsPredicate, Problem> parseComponentsPredicate(JsonElement element, DynamicRegistryManager manager) {
		try {
			return Result.success(ComponentsPredicate.CODEC.codec().parse(manager.getOps(JsonOps.INSTANCE), element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected component predicate"));
		}
	}

	public static Result<Stat<?>, Problem> parseStat(JsonElement element) {
		return parseFromIdentifier(
				element,
				id -> getOrCreateStat(
						Registries.STAT_TYPE.getOptionalValue(
								Identifier.splitOn(id.getNamespace(), '.')
						).orElseThrow(),
						Identifier.splitOn(id.getPath(), '.')
				),
				"stat"
		);
	}

	private static <T> Stat<T> getOrCreateStat(StatType<T> statType, Identifier id) {
		return statType.getOrCreateStat(statType.getRegistry().getOptionalValue(id).orElseThrow());
	}

	public static Result<AdvancementCriterion<?>, Problem> parseAdvancementCriterion(JsonElement element, DynamicRegistryManager manager) {
		try {
			return Result.success(AdvancementCriterion.CODEC.parse(manager.getOps(JsonOps.INSTANCE), element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected advancement criterion"));
		}
	}

	public static Result<NbtCompound, Problem> parseNbt(JsonElement element) {
		return parseFromString(
				element,
				s -> {
					try {
						return StringNbtReader.readCompound(s);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				},
				"nbt"
		);
	}

	@Deprecated
	public static Result<ComponentChanges, Problem> parseComponentChanges(JsonElement element) {
		return parseComponentChanges(element, null);
	}

	public static Result<ComponentChanges, Problem> parseComponentChanges(JsonElement element, DynamicRegistryManager manager) {
		try {
			var ops = manager == null ? JsonOps.INSTANCE : manager.getOps(JsonOps.INSTANCE);
			return Result.success(ComponentChanges.CODEC.parse(ops, element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected components"));
		}
	}

	@Deprecated
	public static Result<ItemStack, Problem> parseItemStack(JsonElement element) {
		return parseItemStack(element, null);
	}

	public static Result<ItemStack, Problem> parseItemStack(JsonElement element, DynamicRegistryManager manager) {
		try {
			return element.getAsObject().andThen(rootObject -> {
				var problems = new ArrayList<Problem>();

				var item = rootObject.get("item")
						.andThen(BuiltinJson::parseItem)
						.ifFailure(problems::add)
						.getSuccess();

				var components = rootObject.get("components")
						.getSuccess()
						.flatMap(nbtElement -> BuiltinJson.parseComponentChanges(nbtElement, manager)
								.ifFailure(problems::add)
								.getSuccess()
						);

				if (problems.isEmpty()) {
					var itemStack = new ItemStack(item.orElseThrow());
					components.ifPresent(itemStack::applyChanges);
					return Result.success(itemStack);
				} else {
					return Result.failure(Problem.combine(problems));
				}
			});
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected item stack"));
		}
	}

	public static Result<AdvancementFrame, Problem> parseFrame(JsonElement element) {
		try {
			return Result.success(AdvancementFrame.CODEC.parse(JsonOps.INSTANCE, element.getJson()).result().orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected frame"));
		}
	}

	public static Result<Text, Problem> parseText(JsonElement element, DynamicRegistryManager manager) {
		try {
			return Result.success(Text.Serialization.fromJsonTree(element.getJson(), manager));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected text"));
		}
	}

	public static Result<EntityAttribute, Problem> parseAttribute(JsonElement element) {
		return parseFromIdentifier(
				element,
				id -> {
					if (id.getNamespace().equals(SkillsAPI.MOD_ID)) {
					id = Identifier.of("puffish_attributes", id.getPath());
					}
					return Registries.ATTRIBUTE.getOptionalValue(id).orElseThrow();
				},
				"attribute"
		);
	}

	public static Result<EntityAttributeModifier.Operation, Problem> parseAttributeOperation(JsonElement element) {
		return parseFromString(
				element,
				s -> switch (s) {
					case "add", "add_value", "addition" -> EntityAttributeModifier.Operation.ADD_VALUE;
					case "multiply_base", "add_multiplied_base" -> EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
					case "multiply_total", "add_multiplied_total" -> EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
					default -> throw new RuntimeException();
				},
				"attribute operation"
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
		return parseFromString(element, Identifier::of, what)
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
				id -> registry.getOptionalValue(id).orElseThrow(),
				what
		);
	}

	private static <T> Result<RegistryEntryList<T>, Problem> parseSomethingTag(
			JsonElement element,
			Registry<T> registry,
			String what
	) {
		return parseFromString(
				element,
				s -> s.startsWith("#") ? Identifier.of(s.substring(1)) : Identifier.of(s),
				what
		).andThen(id -> {
			try {
				return Result.success(registry
						.getOptional(TagKey.of(registry.getKey(), id))
						.orElseThrow());
			} catch (Exception ignored) {
				return Result.failure(element.getPath().createProblem("Unknown " + what + " tag `" + id + "`"));
			}
		});
	}

	private static <T> Result<RegistryEntryList<T>, Problem> parseSomethingOrSomethingTag(
			JsonElement element,
			Registry<T> registry,
			String what
	) {
		try {
			var s = element.getJson().getAsString();
			if (s.startsWith("#")) {
				try {
					var id = Identifier.of(s.substring(1));
					try {
						return Result.success(registry.getOptional(TagKey.of(registry.getKey(), id)).orElseThrow());
					} catch (Exception ignored) {
						return Result.failure(element.getPath().createProblem("Unknown " + what + " tag `" + id + "`"));
					}
				} catch (Exception ignored) {
					return Result.failure(element.getPath().createProblem("Invalid " + what + " tag `" + s + "`"));
				}
			} else {
				try {
					var id = Identifier.of(s);
					try {
						return Result.success(RegistryEntryList.of(registry.getEntry(id).orElseThrow()));
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
