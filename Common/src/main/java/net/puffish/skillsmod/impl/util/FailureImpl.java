package net.puffish.skillsmod.impl.util;

import net.puffish.skillsmod.api.utils.Failure;

import java.util.List;

public class FailureImpl implements Failure {
	private final List<String> failures;

	public FailureImpl(List<String> failures) {
		this.failures = failures;
	}

	@Override
	public List<String> getMessages() {
		return failures;
	}
}
