package net.puffish.skillsmod.config.reader;

import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.PathUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;

import java.nio.file.Path;

public class FileConfigReader extends ConfigReader {
	private final Path modConfigDir;

	public FileConfigReader(Path modConfigDir) {
		this.modConfigDir = modConfigDir;
	}

	public Result<JsonElementWrapper, Failure> readFile(Path file) {
		PathUtils.createFileIfMissing(file);
		return JsonElementWrapper.parseFile(
				file,
				JsonPath.fromPath(modConfigDir.relativize(file))
		);
	}

	@Override
	public Result<JsonElementWrapper, Failure> read(Path path) {
		return readFile(modConfigDir.resolve(path));
	}
}
