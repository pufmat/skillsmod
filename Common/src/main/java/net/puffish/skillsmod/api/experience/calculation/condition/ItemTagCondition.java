package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.RegistryEntryList;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public final class ItemTagCondition implements Condition<ItemStack> {
	private final RegistryEntryList.Named<Item> entries;

	private ItemTagCondition(RegistryEntryList.Named<Item> entries) {
		this.entries = entries;
	}

	public static ConditionFactory<ItemStack> factory() {
		return ConditionFactory.withData(ItemTagCondition::parse);
	}

	public static Result<ItemTagCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(ItemTagCondition::parse);
	}

	public static Result<ItemTagCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseItemTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new ItemTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return entries.contains(itemStack.getRegistryEntry());
	}
}
