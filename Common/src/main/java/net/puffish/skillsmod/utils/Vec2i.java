package net.puffish.skillsmod.utils;

public class Vec2i {
	public int x;
	public int y;

	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void add(int x, int y) {
		this.x += x;
		this.y += y;
	}

	public void sub(int x, int y) {
		this.x -= x;
		this.y -= y;
	}

	public void min(Vec2i v) {
		this.x = Math.min(this.x, v.x);
		this.y = Math.min(this.y, v.y);
	}

	public void max(Vec2i v) {
		this.x = Math.max(this.x, v.x);
		this.y = Math.max(this.y, v.y);
	}
}
