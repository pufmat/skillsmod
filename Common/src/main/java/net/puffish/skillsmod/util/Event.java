package net.puffish.skillsmod.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Event<T> {
	private final List<T> listeners;
	private final T invoker;

	private Event(List<T> listeners, T invoker) {
		this.listeners = listeners;
		this.invoker = invoker;
	}

	public static <T> Event<T> create(Function<Collection<T>, T> invoker) {
		var listeners = new ArrayList<T>();
		return new Event<>(listeners, invoker.apply(listeners));
	}

	public void register(T listener) {
		listeners.add(listener);
	}

	public T invoker() {
		return invoker;
	}
}
