package net.puffish.skillsmod.experience.calculation.parameter;

import net.puffish.skillsmod.api.experience.calculation.parameter.Parameter;

public class FallbackParameter<T> implements Parameter<T> {
	private final double fallback;

	public FallbackParameter(double fallback) {
		this.fallback = fallback;
	}

	@Override
	public Double apply(T t) {
		return fallback;
	}
}
