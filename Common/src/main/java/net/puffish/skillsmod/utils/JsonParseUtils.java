package net.puffish.skillsmod.utils;

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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;
import net.puffish.skillsmod.utils.failure.SingleFailure;

import java.util.ArrayList;

public class JsonParseUtils {
	public static Result<Identifier, Failure> parseIdentifier(JsonElementWrapper element) {
		try {
			return Result.success(new Identifier(JsonHelper.asString(element.getJson(), "")));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid nbt at " + element.getPath().toString()));
		}
	}

	public static Result<StatusEffect, Failure> parseEffect(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registries.STATUS_EFFECT.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid effect at " + element.getPath().toString()));
		}
	}

	public static Result<Block, Failure> parseBlock(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registries.BLOCK.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid block at " + element.getPath().toString()));
		}
	}

	public static Result<StatePredicate, Failure> parseStatePredicate(JsonElementWrapper element) {
		try {
			return Result.success(StatePredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid state predicate at " + element.getPath().toString()));
		}
	}

	public static Result<NbtPredicate, Failure> parseNbtPredicate(JsonElementWrapper element) {
		try {
			return Result.success(NbtPredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid state predicate at " + element.getPath().toString()));
		}
	}

	public static Result<RegistryEntryList.Named<Block>, Failure> parseBlockTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registries.BLOCK.getTagCreatingWrapper().getOptional(TagKey.of(RegistryKeys.BLOCK, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid block tag at " + element.getPath().toString()));
		}
	}

	public static Result<DamageType, Failure> parseDamageType(JsonElementWrapper element, DynamicRegistryManager manager) {
		try {
			return parseIdentifier(element).mapSuccess(id -> manager.get(RegistryKeys.DAMAGE_TYPE).getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid damage type at " + element.getPath().toString()));
		}
	}

	public static Result<RegistryEntryList.Named<DamageType>, Failure> parseDamageTypeTag(JsonElementWrapper element, DynamicRegistryManager manager) {
		try {
			return parseIdentifier(element).mapSuccess(id -> manager.get(RegistryKeys.DAMAGE_TYPE).getTagCreatingWrapper().getOptional(TagKey.of(RegistryKeys.DAMAGE_TYPE, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid entity tag at " + element.getPath().toString()));
		}
	}

	public static Result<EntityType<?>, Failure> parseEntityType(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registries.ENTITY_TYPE.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid entity at " + element.getPath().toString()));
		}
	}

	public static Result<RegistryEntryList.Named<EntityType<?>>, Failure> parseEntityTypeTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registries.ENTITY_TYPE.getTagCreatingWrapper().getOptional(TagKey.of(RegistryKeys.ENTITY_TYPE, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid entity tag at " + element.getPath().toString()));
		}
	}

	public static Result<Item, Failure> parseItem(JsonElementWrapper element) {
		try {
			return Result.success(JsonHelper.asItem(element.getJson(), ""));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid item at " + element.getPath().toString()));
		}
	}

	public static Result<RegistryEntryList.Named<Item>, Failure> parseItemTag(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registries.ITEM.getTagCreatingWrapper().getOptional(TagKey.of(RegistryKeys.ITEM, id)).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid item tag at " + element.getPath().toString()));
		}
	}

	public static Result<NbtCompound, Failure> parseNbt(JsonElementWrapper element) {
		try {
			return Result.success(StringNbtReader.parse(JsonHelper.asString(element.getJson(), "")));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid nbt at " + element.getPath().toString()));
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
			return Result.failure(SingleFailure.of("Expected valid item stack at " + element.getPath().toString()));
		}
	}

	public static Result<AdvancementFrame, Failure> parseFrame(JsonElementWrapper element) {
		try {
			return element.getAsString().andThen(name -> Result.success(AdvancementFrame.forName(name)));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid frame at " + element.getPath().toString()));
		}
	}

	public static Result<Text, Failure> parseText(JsonElementWrapper element) {
		try {
			return Result.success(Text.Serializer.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid text at " + element.getPath().toString()));
		}
	}

	public static Result<EntityAttribute, Failure> parseAttribute(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> Registries.ATTRIBUTE.getOrEmpty(id).orElseThrow());
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Expected valid attribute at " + element.getPath().toString()));
		}
	}

	public static Result<EntityAttributeModifier.Operation, Failure> parseAttributeOperation(JsonElementWrapper element) {
		return element.getAsString().andThen(string -> switch (string) {
			case "addition" -> Result.success(EntityAttributeModifier.Operation.ADDITION);
			case "multiply_base" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_BASE);
			case "multiply_total" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
			default -> Result.failure(SingleFailure.of("Expected valid attribute operation at " + element.getPath().toString()));
		});
	}
}
