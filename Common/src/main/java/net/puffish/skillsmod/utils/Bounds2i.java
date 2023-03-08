package net.puffish.skillsmod.utils;

import org.joml.Vector2i;

public record Bounds2i(Vector2i min, Vector2i max) {
	public static Bounds2i zero() {
		return new Bounds2i(new Vector2i(0, 0), new Vector2i(0, 0));
	}

	public void extend(Vector2i p) {
		min.min(p);
		max.max(p);
	}

	public void grow(int d) {
		min.sub(d, d);
		max.add(d, d);
	}

	public int width() {
		return max.x() - min.x();
	}

	public int height() {
		return max.y() - min.y();
	}
}
