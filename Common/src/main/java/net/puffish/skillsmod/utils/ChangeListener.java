package net.puffish.skillsmod.utils;

import java.util.function.Consumer;

public class ChangeListener<T> {
	private final Consumer<T> callback;
	private T t;

	public ChangeListener(Consumer<T> callback, T initial) {
		this.callback = callback;
		this.t = initial;
	}

	public T get() {
		return t;
	}

	public void set(T t) {
		if (this.t != null) {
			this.callback.accept(this.t);
		}
		this.t = t;
	}
}
