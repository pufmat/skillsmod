package net.puffish.skillsmod.utils;

import net.puffish.skillsmod.SkillsMod;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PathUtils {
	public static void createFileIfMissing(Path path) {
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path.getParent());
				Files.createFile(path);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static boolean isDirectoryEmpty(Path path) {
		try {
			return FileUtils.isEmptyDirectory(path.toFile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void copyFileFromJar(Path source, Path target) {
		try {
			FileUtils.copyInputStreamToFile(Objects.requireNonNull(
					SkillsMod.getInstance().getClass().getResourceAsStream(
							StreamSupport.stream(source.spliterator(), false)
									.map(Path::toString)
									.collect(Collectors.joining("/", "/", ""))
					)
			), target.toFile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
