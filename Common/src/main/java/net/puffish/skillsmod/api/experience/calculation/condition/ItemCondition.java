package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

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

	public static Result<ItemCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(ItemCondition::parse);
	}

	public static Result<ItemCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optItem = rootObject.get("item")
				.andThen(JsonParseUtils::parseItem)
				.ifFailure(failures::add)
				.getSuccess();

		var nbt = rootObject.get("nbt")
				.getSuccess()
				.flatMap(stateElement -> JsonParseUtils.parseNbtPredicate(stateElement)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(() -> new NbtPredicate(new NbtCompound()));

		if (failures.isEmpty()) {
			return Result.success(new ItemCondition(
					optItem.orElseThrow(),
					nbt
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return itemStack.isOf(item) && nbt.test(itemStack);
	}
}
