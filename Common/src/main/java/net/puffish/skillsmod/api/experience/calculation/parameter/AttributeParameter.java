package net.puffish.skillsmod.api.experience.calculation.parameter;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;

public class AttributeParameter implements Parameter<LivingEntity> {
	private final EntityAttribute attribute;

	private AttributeParameter(EntityAttribute attribute) {
		this.attribute = attribute;
	}

	public static ParameterFactory<LivingEntity> factory() {
		return ParameterFactory.withData(AttributeParameter::parse);
	}

	public static Result<AttributeParameter, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(AttributeParameter::parse);
	}

	public static Result<AttributeParameter, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optAttribute = rootObject.get("attribute")
				.andThen(JsonParseUtils::parseAttribute)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new AttributeParameter(
					optAttribute.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Double apply(LivingEntity entity) {
		var instance = entity.getAttributeInstance(attribute);
		if (instance == null) {
			return attribute.getDefaultValue();
		}
		return instance.getValue();
	}
}
