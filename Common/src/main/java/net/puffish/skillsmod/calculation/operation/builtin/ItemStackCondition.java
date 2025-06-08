package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.item.ItemStack;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ItemPredicate;
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
import java.util.Map;
import java.util.Optional;

public final class ItemStackCondition implements Operation<ItemStack, Boolean> {
	private final ItemPredicate predicate;
	private final Optional<NbtPredicate> optNbt;

	private ItemStackCondition(ItemPredicate predicate, Optional<NbtPredicate> optNbt) {
		this.predicate = predicate;
		this.optNbt = optNbt;
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

		var optComponents = rootObject.get("components")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(componentsElement -> BuiltinJson.parseComponentPredicate(componentsElement, context.getServer().getRegistryManager())
						.ifFailure(problems::add)
						.getSuccess()
				);

		var optPredicates = rootObject.get("predicates")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(predicatesElement -> BuiltinJson.parseItemSubPredicates(predicatesElement, context.getServer().getRegistryManager())
						.ifFailure(problems::add)
						.getSuccess()
				);

		if (problems.isEmpty()) {
			return Result.success(new ItemStackCondition(
					new ItemPredicate(
							optItem,
							NumberRange.IntRange.ANY,
							optComponents.orElse(ComponentPredicate.EMPTY),
							optPredicates.orElseGet(Map::of)
					),
					optNbt
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(ItemStack itemStack) {
		return Optional.of(
				predicate.test(itemStack)
						&& optNbt.map(nbt -> nbt.test(itemStack)).orElse(true)
		);
	}
}
