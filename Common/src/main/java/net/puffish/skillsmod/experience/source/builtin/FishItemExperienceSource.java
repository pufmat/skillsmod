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
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Map;

public record FishItemExperienceSource(
		Calculation<Data> calculation
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("fish_item");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_rod_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::rod)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_fished_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::fished)
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				FishItemExperienceSource::parse
		);
	}

	private static Result<FishItemExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	private static Result<FishItemExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
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
			return Result.success(new FishItemExperienceSource(
					optCalculation.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public record Data(ServerPlayerEntity player, ItemStack rod, ItemStack fished) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}
}
