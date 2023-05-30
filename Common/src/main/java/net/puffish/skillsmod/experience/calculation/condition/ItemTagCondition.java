package net.puffish.skillsmod.experience.calculation.condition;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public final class ItemTagCondition implements Condition<ItemStack> {
	private final RegistryEntryList.Named<Item> entries;

	private ItemTagCondition(RegistryEntryList.Named<Item> entries) {
		this.entries = entries;
	}

	public static Result<ItemTagCondition, Error> parse(Result<JsonElementWrapper, Error> maybeElement) {
		return maybeElement.andThen(ItemTagCondition::parse);
	}

	public static Result<ItemTagCondition, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(ItemTagCondition::parse);
	}

	public static Result<ItemTagCondition, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseItemTag)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new ItemTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return Registry.ITEM.getKey(itemStack.getItem())
				.map(key -> entries.contains(Registry.ITEM.entryOf(key)))
				.orElse(false);
	}
}
