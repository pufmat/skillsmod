package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.skill.SkillState;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.Result;

import java.util.ArrayList;

public class SkillConfig {
	private final String id;
	private final int x;
	private final int y;
	private final String definitionId;
	private final boolean isRoot;

	private SkillConfig(String id, int x, int y, String definitionId, boolean isRoot) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.definitionId = definitionId;
		this.isRoot = isRoot;
	}

	public static Result<SkillConfig, Error> parse(String id, JsonElementWrapper rootElement, SkillDefinitionsConfig definitions) {
		return rootElement.getAsObject().andThen(
				rootObject -> SkillConfig.parse(id, rootObject, definitions)
		);
	}

	public static Result<SkillConfig, Error> parse(String id, JsonObjectWrapper rootObject, SkillDefinitionsConfig definitions) {
		var errors = new ArrayList<Error>();

		var optX = rootObject.getInt("x")
				.ifFailure(errors::add)
				.getSuccess();

		var optY = rootObject.getInt("y")
				.ifFailure(errors::add)
				.getSuccess();

		var optDefinitionId = rootObject.get("definition")
				.andThen(definitionElement -> definitionElement.getAsString()
						.andThen(definitionId -> {
							if (definitions.getById(definitionId).isPresent()) {
								return Result.success(definitionId);
							} else {
								return Result.failure(definitionElement.getPath().errorAt("Expected a valid definition"));
							}
						})
				)
				.ifFailure(errors::add)
				.getSuccess();

		var isRoot = rootObject.getBoolean("root")
				.getSuccess() // ignore failure because this property is optional
				.orElse(false);

		if (errors.isEmpty()) {
			return Result.success(new SkillConfig(
					id,
					optX.orElseThrow(),
					optY.orElseThrow(),
					optDefinitionId.orElseThrow(),
					isRoot
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public SkillState getStateFor(CategoryConfig category, CategoryData categoryData) {
		if (categoryData.getUnlockedSkillIds().contains(this.id)) {
			return SkillState.UNLOCKED;
		}

		var neighborIds = category.getConnections().getNeighbors().get(this.id);
		if (neighborIds == null || neighborIds.stream().anyMatch(neighborId -> categoryData.getUnlockedSkillIds().contains(neighborId))) {
			return SkillState.AVAILABLE;
		}

		if (this.isRoot) {
			if (categoryData.getUnlockedSkillIds()
					.stream()
					.flatMap(skillId -> category.getSkills().getById(skillId).stream())
					.noneMatch(skill -> skill.isRoot)
			) {
				return SkillState.AVAILABLE;
			}
		}

		return SkillState.LOCKED;
	}

	public String getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getDefinitionId() {
		return definitionId;
	}

	public boolean isRoot() {
		return isRoot;
	}
}
