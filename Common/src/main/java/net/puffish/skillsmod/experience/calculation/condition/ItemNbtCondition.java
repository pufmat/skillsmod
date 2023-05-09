package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NbtPredicate;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class ItemNbtCondition implements Condition<ItemStack> {
	private final NbtPredicate nbt;

	public ItemNbtCondition(NbtPredicate nbt) {
		this.nbt = nbt;
	}

	public static Result<ItemNbtCondition, Error> parse(Result<JsonElementWrapper, Error> maybeElement) {
		return maybeElement.andThen(ItemNbtCondition::parse);
	}

	public static Result<ItemNbtCondition, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(ItemNbtCondition::parse);
	}

	public static Result<ItemNbtCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optNbt = rootObject.get("nbt")
				.andThen(JsonParseUtils::parseNbtPredicate)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new ItemNbtCondition(
					optNbt.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return nbt.test(itemStack);
	}
}
