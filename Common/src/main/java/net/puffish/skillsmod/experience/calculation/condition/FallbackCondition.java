package net.puffish.skillsmod.experience.calculation.condition;

public final class FallbackCondition<T> implements Condition<T> {
	private final boolean fallback;

	public FallbackCondition(boolean fallback) {
		this.fallback = fallback;
	}

	@Override
	public boolean test(T t) {
		return fallback;
	}
}
