package net.puffish.skillsmod.config.reader;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsAPI;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonPath;
import net.puffish.skillsmod.utils.PathUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.SingleFailure;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class PackConfigReader extends ConfigReader {
	private final ResourceManager resourceManager;
	private final String namespace;

	public PackConfigReader(ResourceManager resourceManager, String namespace) {
		this.resourceManager = resourceManager;
		this.namespace = namespace;
	}

	public Result<JsonElementWrapper, Failure> readResource(Identifier id, Resource resource) {
		try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			return JsonElementWrapper.parseReader(reader, JsonPath.createNamed(id.toString()));
		} catch (Exception e) {
			return Result.failure(SingleFailure.of("Failed to read resource `" + id + "`"));
		}
	}

	@Override
	public Result<JsonElementWrapper, Failure> read(Path path) {
		var id = new Identifier(namespace, PathUtils.pathToString(Path.of(SkillsAPI.MOD_ID).resolve(path)));

		try {
			return readResource(id, resourceManager.getResource(id));
		} catch (FileNotFoundException e) {
			return Result.failure(SingleFailure.of("Resource `" + id + "` does not exist"));
		} catch (IOException e) {
			return Result.failure(SingleFailure.of("Failed to read resource `" + id + "`"));
		}
	}
}
