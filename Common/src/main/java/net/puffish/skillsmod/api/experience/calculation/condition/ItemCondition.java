package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NbtPredicate;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;
import java.util.Optional;

public final class ItemCondition implements Condition<ItemStack> {
	private final Item item;
	private final Optional<NbtPredicate> optNbt;

	private ItemCondition(Item item, Optional<NbtPredicate> optNbt) {
		this.item = item;
		this.optNbt = optNbt;
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

		var optNbt = rootObject.get("nbt")
				.getSuccess()
				.flatMap(stateElement -> JsonParseUtils.parseNbtPredicate(stateElement)
						.ifFailure(failures::add)
						.getSuccess()
				);

		if (failures.isEmpty()) {
			return Result.success(new ItemCondition(
					optItem.orElseThrow(),
					optNbt
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return itemStack.isOf(item) && optNbt.map(nbt -> nbt.test(itemStack)).orElse(true);
	}
}
