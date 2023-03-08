package net.puffish.skillsmod.utils.error;

public class SingleError implements Error {
	private final String message;

	private SingleError(String message) {
		this.message = message;
	}

	public static SingleError of(String message) {
		return new SingleError(message);
	}

	public String getMessage() {
		return message;
	}
}
