package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.calculation.LegacyBuiltinPrototypes;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public class AttributeOperation implements Operation<LivingEntity, AttributeInstance> {
	private final Holder<Attribute> attribute;

	private AttributeOperation(Holder<Attribute> attribute) {
		this.attribute = attribute;
	}

	public static void register() {
		BuiltinPrototypes.LIVING_ENTITY.registerOperation(
				SkillsMod.createIdentifier("get_attribute"),
				BuiltinPrototypes.ATTRIBUTE_INSTANCE,
				AttributeOperation::parse
		);

		LegacyBuiltinPrototypes.registerAlias(
				BuiltinPrototypes.LIVING_ENTITY,
				SkillsMod.createIdentifier("attribute"),
				SkillsMod.createIdentifier("get_attribute")
		);
	}

	public static Result<AttributeOperation, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(AttributeOperation::parse, context));
	}

	public static Result<AttributeOperation, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optAttribute = rootObject.get("attribute")
				.andThen(BuiltinJson::parseAttribute)
				.ifFailure(problems::add)
				.getSuccess()
				.map(BuiltInRegistries.ATTRIBUTE::wrapAsHolder);

		if (problems.isEmpty()) {
			return Result.success(new AttributeOperation(
					optAttribute.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<AttributeInstance> apply(LivingEntity entity) {
		return Optional.ofNullable(entity.getAttribute(attribute));
	}
}
