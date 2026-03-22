package net.puffish.skillsmod.calculation.operation.builtin.legacy;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class LegacyItemTagCondition implements Operation<ItemStack, Boolean> {
	private final HolderSet<Item> entries;

	private LegacyItemTagCondition(HolderSet<Item> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.ITEM_STACK.registerOperation(
				SkillsMod.createIdentifier("legacy_item_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyItemTagCondition::parse
		);
	}

	public static Result<LegacyItemTagCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyItemTagCondition::parse);
	}

	public static Result<LegacyItemTagCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTag = rootObject.get("tag")
				.andThen(BuiltinJson::parseItemTag)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new LegacyItemTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(ItemStack itemStack) {
		return Optional.of(itemStack.is(entries));
	}
}
