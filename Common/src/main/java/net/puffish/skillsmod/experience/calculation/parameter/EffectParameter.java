package net.puffish.skillsmod.experience.calculation.parameter;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public class EffectParameter implements Parameter<LivingEntity> {
	private final StatusEffect effect;

	private EffectParameter(StatusEffect effect) {
		this.effect = effect;
	}

	public static Result<EffectParameter, Error> parse(Result<JsonElementWrapper, Error> maybeElement) {
		return maybeElement.andThen(EffectParameter::parse);
	}

	public static Result<EffectParameter, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject().andThen(EffectParameter::parse);
	}

	public static Result<EffectParameter, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optEffect = rootObject.get("effect")
				.andThen(JsonParseUtils::parseEffect)
				.ifFailure(errors::add)
				.getSuccess();

		if (errors.isEmpty()) {
			return Result.success(new EffectParameter(
					optEffect.orElseThrow()
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	@Override
	public Double apply(LivingEntity entity) {
		var instance = entity.getStatusEffect(effect);
		if (instance == null) {
			return 0.0;
		}
		return instance.getAmplifier() + 1.0;
	}
}
