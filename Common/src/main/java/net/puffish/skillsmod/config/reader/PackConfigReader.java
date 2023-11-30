package net.puffish.skillsmod.config.reader;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.utils.PathUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.nio.file.Path;

public class PackConfigReader extends ConfigReader {
	private final ResourceManager resourceManager;
	private final String namespace;

	public PackConfigReader(ResourceManager resourceManager, String namespace) {
		this.resourceManager = resourceManager;
		this.namespace = namespace;
	}

	public Result<JsonElementWrapper, Failure> readResource(Identifier id, Resource resource) {
		try (var reader = resource.getReader()) {
			return JsonElementWrapper.parseReader(reader, JsonPath.named(id.toString()));
		} catch (Exception e) {
			return Result.failure(Failure.message("Failed to read resource `" + id + "`"));
		}
	}

	@Override
	public Result<JsonElementWrapper, Failure> read(Path path) {
		var id = Identifier.of(namespace, PathUtils.pathToString(Path.of(SkillsAPI.MOD_ID).resolve(path)));

		return resourceManager.getResource(id)
				.map(resource -> readResource(id, resource))
				.orElseGet(() -> Result.failure(Failure.message("Resource `" + id + "` does not exist")));
	}

	@Override
	public boolean exists(Path path) {
		var id = Identifier.of(namespace, PathUtils.pathToString(Path.of(SkillsAPI.MOD_ID).resolve(path)));

		return resourceManager.getResource(id).isPresent();
	}
}
