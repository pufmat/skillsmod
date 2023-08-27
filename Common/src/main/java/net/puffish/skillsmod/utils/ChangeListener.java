package net.puffish.skillsmod.utils;

public class ChangeListener<T> {
	private Runnable undo;
	private T value;

	public ChangeListener(T value, Runnable undo) {
		this.value = value;
		this.undo = undo;
	}

	public T get() {
		return value;
	}

	public void set(T value, Runnable undo) {
		this.undo.run();
		this.value = value;
		this.undo = undo;
	}
}
