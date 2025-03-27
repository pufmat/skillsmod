package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceDisposeContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.Map;

public class EnchantItemExperienceSource implements ExperienceSource {
	private static final Identifier ID = SkillsMod.createIdentifier("enchant_item");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_enchanted_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::itemStack)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_levels"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(Data::levels)
		);
	}

	private final Calculation<Data> calculation;

	private EnchantItemExperienceSource(Calculation<Data> calculation) {
		this.calculation = calculation;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				EnchantItemExperienceSource::parse
		);
	}

	private static Result<EnchantItemExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.noUnused(o -> parse(o, context)));
	}

	private static Result<EnchantItemExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
		var problems = new ArrayList<Problem>();

		var variables = rootObject.get("variables")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(variablesElement -> Variables.parse(variablesElement, PROTOTYPE, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(() -> Variables.create(Map.of()));

		var optCalculation = rootObject.get("experience")
				.andThen(experienceElement -> Calculation.parse(
						experienceElement,
						variables,
						context
				))
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new EnchantItemExperienceSource(
					optCalculation.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private record Data(ServerPlayerEntity player, ItemStack itemStack, double levels) { }

	public int getValue(ServerPlayerEntity player, ItemStack itemStack, int levels) {
		return (int) Math.round(calculation.evaluate(
				new Data(player, itemStack, levels)
		));
	}

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}
}
