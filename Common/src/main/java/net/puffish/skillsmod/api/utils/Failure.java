package net.puffish.skillsmod.api.utils;

import net.puffish.skillsmod.impl.util.FailureImpl;

import java.util.Collection;
import java.util.List;

public interface Failure {
	static Failure message(String message) {
		return new FailureImpl(List.of(message));
	}

	static Failure fromMany(Collection<Failure> failures) {
		return new FailureImpl(failures.stream().map(Failure::getMessages).flatMap(List::stream).toList());
	}

	List<String> getMessages();
}
