package net.puffish.skillsmod.api.json;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.block.Block;
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
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
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
import java.util.function.Supplier;

public final class BuiltinJson {
	private BuiltinJson() { }

	public static Result<Identifier, Problem> parseIdentifier(JsonElement element) {
		return parseFromString(
				element,
				Identifier::new,
				() -> "Expected identifier",
				s -> "Invalid identifier `" + s + "`"
		);
	}

	public static Result<String, Problem> parseIdentifierPath(JsonElement element) {
		return parseFromString(
				element,
				s -> new Identifier(Identifier.DEFAULT_NAMESPACE, s).getPath(),
				() -> "Expected identifier path",
				s -> "Invalid identifier path `" + s + "`"
		);
	}

	public static Result<StatusEffect, Problem> parseEffect(JsonElement element) {
		return parseSomething(
				element,
				Registries.STATUS_EFFECT,
				() -> "Expected effect",
				id -> "Unknown effect `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<StatusEffect>, Problem> parseEffectTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.STATUS_EFFECT,
				() -> "Expected effect tag",
				id -> "Unknown effect tag `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<StatusEffect>, Problem> parseEffectOrEffectTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.STATUS_EFFECT,
				() -> "Expected effect or effect tag",
				id -> "Unknown effect or effect tag `" + id + "`"
		);
	}

	public static Result<Block, Problem> parseBlock(JsonElement element) {
		return parseSomething(
				element,
				Registries.BLOCK,
				() -> "Expected block",
				id -> "Unknown block `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<Block>, Problem> parseBlockTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.BLOCK,
				() -> "Expected block tag",
				id -> "Unknown block tag `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<Block>, Problem> parseBlockOrBlockTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.BLOCK,
				() -> "Expected block or block tag",
				id -> "Unknown block or block tag `" + id + "`"
		);
	}

	public static Result<DamageType, Problem> parseDamageType(JsonElement element, DynamicRegistryManager manager) {
		return parseSomething(
				element,
				manager.get(RegistryKeys.DAMAGE_TYPE),
				() -> "Expected damage type",
				id -> "Unknown damage type `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<DamageType>, Problem> parseDamageTypeTag(JsonElement element, DynamicRegistryManager manager) {
		return parseSomethingTag(
				element,
				manager.get(RegistryKeys.DAMAGE_TYPE),
				() -> "Expected damage type tag",
				id -> "Unknown damage type tag `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<DamageType>, Problem> parseDamageTypeOrDamageTypeTag(JsonElement element, DynamicRegistryManager manager) {
		return parseSomethingOrSomethingTag(
				element,
				manager.get(RegistryKeys.DAMAGE_TYPE),
				() -> "Expected damage type or damage type tag",
				id -> "Unknown damage type or damage type tag `" + id + "`"
		);
	}

	public static Result<EntityType<?>, Problem> parseEntityType(JsonElement element) {
		return parseSomething(
				element,
				Registries.ENTITY_TYPE,
				() -> "Expected entity type",
				id -> "Unknown entity type `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<EntityType<?>>, Problem> parseEntityTypeTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.ENTITY_TYPE,
				() -> "Expected entity type tag",
				id -> "Unknown entity type tag `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<EntityType<?>>, Problem> parseEntityTypeOrEntityTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.ENTITY_TYPE,
				() -> "Expected entity type or entity type tag",
				id -> "Unknown entity type or entity type tag `" + id + "`"
		);
	}

	public static Result<Item, Problem> parseItem(JsonElement element) {
		return parseSomething(
				element,
				Registries.ITEM,
				() -> "Expected item",
				id -> "Unknown item `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<Item>, Problem> parseItemTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.ITEM,
				() -> "Expected item tag",
				id -> "Unknown item tag `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<Item>, Problem> parseItemOrItemTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.ITEM,
				() -> "Expected item or item tag",
				id -> "Unknown item or item tag `" + id + "`"
		);
	}

	public static Result<StatType<?>, Problem> parseStatType(JsonElement element) {
		return parseSomething(
				element,
				Registries.STAT_TYPE,
				() -> "Expected stat type",
				id -> "Unknown stat type `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<StatType<?>>, Problem> parseStatTypeTag(JsonElement element) {
		return parseSomethingTag(
				element,
				Registries.STAT_TYPE,
				() -> "Expected stat type tag",
				id -> "Unknown stat type tag `" + id + "`"
		);
	}

	public static Result<RegistryEntryList<StatType<?>>, Problem> parseStatTypeOrStatTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(
				element,
				Registries.STAT_TYPE,
				() -> "Expected stat type or stat type tag",
				id -> "Unknown stat type or stat type tag `" + id + "`"
		);
	}

	public static Result<StatePredicate, Problem> parseStatePredicate(JsonElement element) {
		try {
			return Result.success(StatePredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected state predicate"));
		}
	}

	public static Result<NbtPredicate, Problem> parseNbtPredicate(JsonElement element) {
		return parseFromString(
				element,
				s -> {
					try {
						return new NbtPredicate(StringNbtReader.parse(s));
					} catch (Exception e) {
						throw  new RuntimeException(e);
					}
				},
				() -> "Expected state predicate",
				s -> "Invalid state predicate `" + s + "`"
		);
	}

	public static Result<Stat<?>, Problem> parseStat(JsonElement element) {
		return parseFromIdentifier(
				element,
				id -> getOrCreateStat(
						Registries.STAT_TYPE.getOrEmpty(
								Identifier.splitOn(id.getNamespace(), '.')
						).orElseThrow(),
						Identifier.splitOn(id.getPath(), '.')
				),
				() -> "Expected stat",
				id -> "Unknown stat `" + id + "`"
		);
	}

	private static <T> Stat<T> getOrCreateStat(StatType<T> statType, Identifier id) {
		return statType.getOrCreateStat(statType.getRegistry().getOrEmpty(id).orElseThrow());
	}

	public static Result<NbtCompound, Problem> parseNbt(JsonElement element) {
		return parseFromString(
				element,
				s -> {
					try {
						return StringNbtReader.parse(s);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				},
				() -> "Expected NBT",
				s -> "Invalid NBT `" + s + "`"
		);
	}

	public static Result<ItemStack, Problem> parseItemStack(JsonElement element) {
		try {
			return element.getAsObject().andThen(rootObject -> {
				var problems = new ArrayList<Problem>();

				var item = rootObject.get("item")
						.andThen(BuiltinJson::parseItem)
						.ifFailure(problems::add)
						.getSuccess();

				var nbt = rootObject.get("nbt")
						.getSuccess()
						.flatMap(nbtElement -> BuiltinJson.parseNbt(nbtElement)
								.ifFailure(problems::add)
								.getSuccess()
						);

				if (problems.isEmpty()) {
					var itemStack = new ItemStack(item.orElseThrow());
					nbt.ifPresent(itemStack::setNbt);
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
			return element.getAsString().andThen(name -> Result.success(AdvancementFrame.forName(name)));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected frame"));
		}
	}

	public static Result<Text, Problem> parseText(JsonElement element) {
		try {
			return Result.success(Text.Serializer.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected text"));
		}
	}

	public static Result<EntityAttribute, Problem> parseAttribute(JsonElement element) {
		return parseFromIdentifier(
				element,
				id -> {
					if (id.getNamespace().equals(SkillsAPI.MOD_ID)) {
						id = new Identifier("puffish_attributes", id.getPath());
					}
					return Registries.ATTRIBUTE.getOrEmpty(id).orElseThrow();
				},
				() -> "Expected attribute",
				id -> "Unknown attribute `" + id + "`"
		);
	}

	public static Result<EntityAttributeModifier.Operation, Problem> parseAttributeOperation(JsonElement element) {
		return parseFromString(
				element,
				s -> switch (s) {
					case "add", "add_value", "addition" -> EntityAttributeModifier.Operation.ADDITION;
					case "multiply_base", "add_multiplied_base" -> EntityAttributeModifier.Operation.MULTIPLY_BASE;
					case "multiply_total", "add_multiplied_total" -> EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
					default -> throw new RuntimeException();
				},
				() -> "Expected attribute operation",
				s -> "Unknown attribute operation `" + s + "`"
		);
	}

	private static <T> Result<T, Problem> parseFromString(JsonElement element, Function<String, T> parser, Supplier<String> expected, Function<String, String> unknown) {
		try {
			var s = element.getJson().getAsString();
			try {
				return Result.success(parser.apply(s));
			} catch (Exception ignored) {
				return Result.failure(element.getPath().createProblem(unknown.apply(s)));
			}
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem(expected.get()));
		}
	}

	private static <T> Result<T, Problem> parseFromIdentifier(JsonElement element, Function<Identifier, T> parser, Supplier<String> expected, Function<Identifier, String> unknown) {
		return parseIdentifier(element)
				.mapFailure(problem -> element.getPath().createProblem(expected.get()))
				.andThen(id -> {
					try {
						return Result.success(parser.apply(id));
					} catch (Exception ignored) {
						return Result.failure(element.getPath().createProblem(unknown.apply(id)));
					}
				});
	}

	private static <T> Result<T, Problem> parseSomething(JsonElement element, Registry<T> registry, Supplier<String> expected, Function<Identifier, String> unknown) {
		return parseFromIdentifier(
				element,
				id -> registry.getOrEmpty(id).orElseThrow(),
				expected,
				unknown
		);
	}

	private static <T> Result<RegistryEntryList<T>, Problem> parseSomethingTag(JsonElement element, Registry<T> registry, Supplier<String> expected, Function<Identifier, String> unknown) {
		try {
			var s = element.getJson().getAsString();
			var id = s.startsWith("#") ? new Identifier(s.substring(1)) : new Identifier(s);
			try {
				return Result.success(registry.getReadOnlyWrapper()
						.getOptional(TagKey.of(registry.getKey(), id))
						.orElseThrow());
			} catch (Exception ignored) {
				return Result.failure(element.getPath().createProblem(unknown.apply(id)));
			}
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem(expected.get()));
		}
	}

	private static <T> Result<RegistryEntryList<T>, Problem> parseSomethingOrSomethingTag(JsonElement element, Registry<T> registry, Supplier<String> expected, Function<Identifier, String> unknown) {
		try {
			var s = element.getJson().getAsString();
			if (s.startsWith("#")) {
				var id = new Identifier(s.substring(1));
				try {
					return Result.success(registry.getReadOnlyWrapper().getOptional(TagKey.of(registry.getKey(), id)).orElseThrow());
				} catch (Exception ignored) {
					return Result.failure(element.getPath().createProblem(unknown.apply(id)));
				}
			} else {
				var id = new Identifier(s);
				try {
					return Result.success(RegistryEntryList.of(registry.getEntry(RegistryKey.of(registry.getKey(), id)).orElseThrow()));
				} catch (Exception ignored) {
					return Result.failure(element.getPath().createProblem(unknown.apply(id)));
				}
			}
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem(expected.get()));
		}
	}
}
