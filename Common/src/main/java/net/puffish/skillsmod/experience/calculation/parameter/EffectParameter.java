package net.puffish.skillsmod.experience.calculation.parameter;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public class EffectParameter implements Parameter<LivingEntity> {
	private final StatusEffect effect;

	private EffectParameter(StatusEffect effect) {
		this.effect = effect;
	}

	public static ParameterFactory<LivingEntity> factory() {
		return ParameterFactory.withData(EffectParameter::parse);
	}

	public static Result<EffectParameter, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(EffectParameter::parse);
	}

	public static Result<EffectParameter, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optEffect = rootObject.get("effect")
				.andThen(JsonParseUtils::parseEffect)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new EffectParameter(
					optEffect.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
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
