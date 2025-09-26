package net.puffish.skillsmod.config.colors;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public record ColorConfig(int argb) {
	public static Result<ColorConfig, Problem> parse(JsonElement element) {
		return element.getAsString().andThen(string -> parse(string, element.getPath()));
	}

	public static Result<ColorConfig, Problem> parse(String string, JsonPath path) {
		try {
			if (string.startsWith("#")) {
				string = string.substring(1);
				switch (string.length()) {
					case 3 -> {
						var color = Integer.parseInt(string, 16);
						return Result.success(new ColorConfig(
								(((color & 0xf00) << 8) | ((color & 0xf0) << 4) | (color & 0xf)) * 0x11 | 0xff000000
						));
					}
					case 4 -> {
						var color = Integer.parseInt(string, 16);
						return Result.success(new ColorConfig(
								(((color & 0xf000) << 12) | ((color & 0xf00) << 8) | ((color & 0xf0) << 4) | (color & 0xf)) * 0x11
						));
					}
					case 6 -> {
						return Result.success(new ColorConfig(
								Integer.parseInt(string, 16) | 0xff000000
						));
					}
					case 8 -> {
						return Result.success(new ColorConfig(
								(int) Long.parseLong(string, 16)
						));
					}
					default -> { }
				}
			}
		} catch (Exception ignored) { }
		return Result.failure(path.createProblem("Expected a valid color"));
	}
}
