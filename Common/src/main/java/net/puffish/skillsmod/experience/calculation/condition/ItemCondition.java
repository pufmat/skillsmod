package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class ItemCondition implements Condition<ItemStack> {
	private final Item item;
	private final NbtPredicate nbt;

	public ItemCondition(Item item, NbtPredicate nbt) {
		this.item = item;
		this.nbt = nbt;
	}

	public static ConditionFactory<ItemStack> factory() {
		return ConditionFactory.withData(ItemCondition::parse);
	}

	public static Result<ItemCondition, Error> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(ItemCondition::parse);
	}

	public static Result<ItemCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optItem = rootObject.get("item")
				.andThen(JsonParseUtils::parseItem)
				.ifFailure(errors::add)
				.getSuccess();

		var nbt = rootObject.get("nbt")
				.getSuccess()
				.flatMap(stateElement -> JsonParseUtils.parseNbtPredicate(stateElement)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElseGet(() -> new NbtPredicate(new NbtCompound()));

		if (errors.isEmpty()) {
			return Result.success(new ItemCondition(
					optItem.orElseThrow(),
					nbt
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return itemStack.isOf(item) && nbt.test(itemStack);
	}
}
