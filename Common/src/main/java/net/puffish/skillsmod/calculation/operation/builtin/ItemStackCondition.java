package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class ItemStackCondition implements Operation<ItemStack, Boolean> {
	private final Optional<HolderSet<Item>> optItemEntries;
	private final Optional<NbtPredicate> optNbt;
	private final Optional<DataComponentMatchers> optComponents;

	private ItemStackCondition(Optional<HolderSet<Item>> optItemEntries, Optional<NbtPredicate> optNbt, Optional<DataComponentMatchers> optComponents) {
		this.optItemEntries = optItemEntries;
		this.optNbt = optNbt;
		this.optComponents = optComponents;
	}

	public static void register() {
		BuiltinPrototypes.ITEM_STACK.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				ItemStackCondition::parse
		);
	}

	public static Result<ItemStackCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	public static Result<ItemStackCondition, Problem> parse(JsonObject rootObject, OperationConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optItem = rootObject.get("item")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(itemElement -> BuiltinJson.parseItemOrItemTag(itemElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		var optNbt = rootObject.get("nbt")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(nbtElement -> BuiltinJson.parseNbtPredicate(nbtElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		// these fields are used in components predicate, access them to avoid unused field error
		rootObject.get("components");
		rootObject.get("predicates");

		var optComponents = BuiltinJson.parseDataComponentMatchers(rootObject.getAsElement(), context.getServer().registryAccess())
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new ItemStackCondition(
					optItem,
					optNbt,
					optComponents
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(ItemStack itemStack) {
		return Optional.of(
				optItemEntries.map(itemStack::is).orElse(true)
						&& optNbt.map(nbt -> nbt.matches(itemStack)).orElse(true)
						&& optComponents.map(components -> components.test(itemStack)).orElse(true)
		);
	}
}
