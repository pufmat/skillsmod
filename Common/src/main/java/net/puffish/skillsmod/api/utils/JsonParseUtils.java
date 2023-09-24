package net.puffish.skillsmod.api.utils;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.tag.TagKey;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public class JsonParseUtils {
	public static Result<Identifier, Failure> parseIdentifier(JsonElementWrapper element) {
		try {
			return Result.success(new Identifier(JsonHelper.asString(element.getJson(), "")));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid identifier"));
		}
	}

	public static Result<String, Failure> parseIdentifierPath(JsonElementWrapper element) {
		try {
			return Result.success(new Identifier(Identifier.DEFAULT_NAMESPACE, JsonHelper.asString(element.getJson(), "")).getPath());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid identifier path"));
		}
	}

	public static Result<StatusEffect, Failure> parseEffect(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.STATUS_EFFECT.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid effect"));
		}
	}

	public static Result<Block, Failure> parseBlock(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.BLOCK.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid block"));
		}
	}

	public static Result<StatePredicate, Failure> parseStatePredicate(JsonElementWrapper element) {
		try {
			return Result.success(StatePredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid state predicate"));
		}
	}

	public static Result<NbtPredicate, Failure> parseNbtPredicate(JsonElementWrapper element) {
		try {
			return Result.success(NbtPredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid state predicate"));
		}
	}

	public static Result<RegistryEntryList.Named<Block>, Failure> parseBlockTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.BLOCK.getEntryList(TagKey.of(Registry.BLOCK_KEY, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid block tag"));
		}
	}

	public static Result<EntityType<?>, Failure> parseEntityType(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ENTITY_TYPE.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid entity type"));
		}
	}

	public static Result<RegistryEntryList.Named<EntityType<?>>, Failure> parseEntityTypeTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ENTITY_TYPE.getEntryList(TagKey.of(Registry.ENTITY_TYPE_KEY, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid entity type tag"));
		}
	}

	public static Result<Stat<?>, Failure> parseStat(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> getOrCreateStat(Registry.STAT_TYPE.getOrEmpty(
					Identifier.splitOn(id.getNamespace(), '.')
			).orElseThrow(), Identifier.splitOn(id.getPath(), '.')));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid statistic"));
		}
	}

	private static <T> Stat<T> getOrCreateStat(StatType<T> statType, Identifier id) {
		return statType.getOrCreateStat(statType.getRegistry().getOrEmpty(id).orElseThrow());
	}

	public static Result<Item, Failure> parseItem(JsonElementWrapper element) {
		try {
			return Result.success(JsonHelper.asItem(element.getJson(), ""));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid item"));
		}
	}

	public static Result<RegistryEntryList.Named<Item>, Failure> parseItemTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ITEM.getEntryList(TagKey.of(Registry.ITEM_KEY, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid item tag"));
		}
	}

	public static Result<NbtCompound, Failure> parseNbt(JsonElementWrapper element) {
		try {
			return Result.success(StringNbtReader.parse(JsonHelper.asString(element.getJson(), "")));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid nbt"));
		}
	}

	public static Result<ItemStack, Failure> parseItemStack(JsonElementWrapper element) {
		try {
			return element.getAsObject().andThen(object -> {
				var failures = new ArrayList<Failure>();

				var item = object.get("item")
						.andThen(JsonParseUtils::parseItem)
						.ifFailure(failures::add)
						.getSuccess();

				var nbt = object.get("nbt")
						.getSuccess()
						.flatMap(nbtElement -> JsonParseUtils.parseNbt(nbtElement)
								.ifFailure(failures::add)
								.getSuccess()
						);

				if (failures.isEmpty()) {
					var itemStack = new ItemStack(item.orElseThrow());
					nbt.ifPresent(itemStack::setNbt);
					return Result.success(itemStack);
				} else {
					return Result.failure(ManyFailures.ofList(failures));
				}
			});
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid item stack"));
		}
	}

	public static Result<AdvancementFrame, Failure> parseFrame(JsonElementWrapper element) {
		try {
			return element.getAsString().andThen(name -> Result.success(AdvancementFrame.forName(name)));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid frame"));
		}
	}

	public static Result<Text, Failure> parseText(JsonElementWrapper element) {
		try {
			return Result.success(Text.Serializer.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid text"));
		}
	}

	public static Result<EntityAttribute, Failure> parseAttribute(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ATTRIBUTE.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(element.getPath().failureAt("Expected valid attribute"));
		}
	}

	public static Result<EntityAttributeModifier.Operation, Failure> parseAttributeOperation(JsonElementWrapper element) {
		return element.getAsString().andThen(string -> switch (string) {
			case "addition" -> Result.success(EntityAttributeModifier.Operation.ADDITION);
			case "multiply_base" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_BASE);
			case "multiply_total" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
			default -> Result.failure(element.getPath().failureAt("Expected valid attribute operation"));
		});
	}
}
