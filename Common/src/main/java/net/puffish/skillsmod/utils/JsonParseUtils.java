package net.puffish.skillsmod.utils;

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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.error.SingleError;
import net.puffish.skillsmod.json.JsonElementWrapper;

import java.util.ArrayList;

public class JsonParseUtils {
	public static Result<Identifier, Error> parseIdentifier(JsonElementWrapper element) {
		try {
			return Result.success(new Identifier(JsonHelper.asString(element.getJson(), "")));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid nbt at " + element.getPath().toString()));
		}
	}

	public static Result<StatusEffect, Error> parseEffect(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.STATUS_EFFECT.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid effect at " + element.getPath().toString()));
		}
	}

	public static Result<Block, Error> parseBlock(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.BLOCK.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid block at " + element.getPath().toString()));
		}
	}

	public static Result<StatePredicate, Error> parseStatePredicate(JsonElementWrapper element) {
		try {
			return Result.success(StatePredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid state predicate at " + element.getPath().toString()));
		}
	}

	public static Result<NbtPredicate, Error> parseNbtPredicate(JsonElementWrapper element) {
		try {
			return Result.success(NbtPredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid state predicate at " + element.getPath().toString()));
		}
	}

	public static Result<RegistryEntryList.Named<Block>, Error> parseBlockTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.BLOCK.getEntryList(TagKey.of(Registry.BLOCK_KEY, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid block tag at " + element.getPath().toString()));
		}
	}

	public static Result<EntityType<?>, Error> parseEntityType(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ENTITY_TYPE.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid entity at " + element.getPath().toString()));
		}
	}


	public static Result<RegistryEntryList.Named<EntityType<?>>, Error> parseEntityTypeTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ENTITY_TYPE.getEntryList(TagKey.of(Registry.ENTITY_TYPE_KEY, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid entity tag at " + element.getPath().toString()));
		}
	}

	public static Result<Item, Error> parseItem(JsonElementWrapper element) {
		try {
			return Result.success(JsonHelper.asItem(element.getJson(), ""));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid item at " + element.getPath().toString()));
		}
	}

	public static Result<RegistryEntryList.Named<Item>, Error> parseItemTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ITEM.getEntryList(TagKey.of(Registry.ITEM_KEY, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid item tag at " + element.getPath().toString()));
		}
	}

	public static Result<NbtCompound, Error> parseNbt(JsonElementWrapper element) {
		try {
			return Result.success(StringNbtReader.parse(JsonHelper.asString(element.getJson(), "")));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid nbt at " + element.getPath().toString()));
		}
	}

	public static Result<ItemStack, Error> parseItemStack(JsonElementWrapper element) {
		try {
			return element.getAsObject().andThen(object -> {
				var errors = new ArrayList<Error>();

				var item = object.get("item")
						.andThen(JsonParseUtils::parseItem)
						.ifFailure(errors::add)
						.getSuccess();

				var nbt = object.get("nbt")
						.getSuccess()
						.flatMap(nbtElement -> JsonParseUtils.parseNbt(nbtElement)
								.ifFailure(errors::add)
								.getSuccess()
						);

				if (errors.isEmpty()) {
					var itemStack = new ItemStack(item.orElseThrow());
					nbt.ifPresent(itemStack::setNbt);
					return Result.success(itemStack);
				} else {
					return Result.failure(ManyErrors.ofList(errors));
				}
			});
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid item stack at " + element.getPath().toString()));
		}
	}

	public static Result<AdvancementFrame, Error> parseFrame(JsonElementWrapper element) {
		try {
			return element.getAsString().andThen(name -> Result.success(AdvancementFrame.forName(name)));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid frame at " + element.getPath().toString()));
		}
	}

	public static Result<Text, Error> parseText(JsonElementWrapper element) {
		try {
			return Result.success(Text.Serializer.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid text at " + element.getPath().toString()));
		}
	}

	public static Result<EntityAttribute, Error> parseAttribute(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registry.ATTRIBUTE.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleError.of("Expected valid attribute at " + element.getPath().toString()));
		}
	}

	public static Result<EntityAttributeModifier.Operation, Error> parseAttributeOperation(JsonElementWrapper element) {
		return element.getAsString().andThen(string -> switch (string) {
			case "addition" -> Result.success(EntityAttributeModifier.Operation.ADDITION);
			case "multiply_base" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_BASE);
			case "multiply_total" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
			default -> Result.failure(SingleError.of("Expected valid attribute operation at " + element.getPath().toString()));
		});
	}
}
