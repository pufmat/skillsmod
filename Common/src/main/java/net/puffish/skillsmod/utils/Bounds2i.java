package net.puffish.skillsmod.utils;

public record Bounds2i(Vec2i min, Vec2i max) {
	public static Bounds2i zero() {
		return new Bounds2i(new Vec2i(0, 0), new Vec2i(0, 0));
	}

	public void extend(Vec2i p) {
		min.min(p);
		max.max(p);
	}

	public void grow(int d) {
		min.sub(d, d);
		max.add(d, d);
	}

	public int width() {
		return max.x - min.x;
	}

	public int height() {
		return max.y - min.y;
	}
}
