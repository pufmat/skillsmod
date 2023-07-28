package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NbtPredicate;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public final class ItemNbtCondition implements Condition<ItemStack> {
	private final NbtPredicate nbt;

	public ItemNbtCondition(NbtPredicate nbt) {
		this.nbt = nbt;
	}

	public static ConditionFactory<ItemStack> factory() {
		return ConditionFactory.withData(ItemNbtCondition::parse);
	}

	public static Result<ItemNbtCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(ItemNbtCondition::parse);
	}

	public static Result<ItemNbtCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optNbt = rootObject.get("nbt")
				.andThen(JsonParseUtils::parseNbtPredicate)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new ItemNbtCondition(
					optNbt.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return nbt.test(itemStack);
	}
}
