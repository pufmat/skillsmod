package net.puffish.skillsmod.config.skill;

import net.minecraft.text.Text;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.util.DisposeContext;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.ArrayList;
import java.util.List;

public record SkillDefinitionConfig(
		String id,
		Text title,
		Text description,
		Text extraDescription,
		IconConfig icon,
		FrameConfig frame,
		float size,
		List<SkillRewardConfig> rewards,
		int cost,
		int requiredSkills,
		int requiredPoints,
		int requiredSpentPoints,
		int requiredExclusions
) {

	public static Result<SkillDefinitionConfig, Problem> parse(String id, JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(id, rootObject, context), context)
		);
	}

	public static Result<SkillDefinitionConfig, Problem> parse(String id, JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTitle = rootObject.get("title")
				.andThen(BuiltinJson::parseText)
				.ifFailure(problems::add)
				.getSuccess();

		var description = rootObject.get("description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> BuiltinJson.parseText(descriptionElement)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(Text::empty);

		var extraDescription = rootObject.get("extra_description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> BuiltinJson.parseText(descriptionElement)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(Text::empty);

		var optIcon = rootObject.get("icon")
				.andThen(element -> IconConfig.parse(element, context))
				.ifFailure(problems::add)
				.getSuccess();

		var frame = rootObject.get("frame")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> FrameConfig.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(FrameConfig::createDefault);

		var size = rootObject.get("size")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsFloat()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1f);

		var rewards = rootObject.getArray("rewards")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(array -> array.getAsList((i, element) -> SkillRewardConfig.parse(element, context)).mapFailure(Problem::combine)
						.ifFailure(problems::add)
						.getSuccess())
				.orElseGet(List::of);

		var cost = rootObject.get("cost")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1);

		var requiredSkills = rootObject.get("required_skills")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1);

		var requiredPoints = rootObject.get("required_points")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(0);

		var requiredSpentPoints = rootObject.get("required_spent_points")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(0);

		var requiredExclusions = rootObject.get("required_exclusions")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1);

		// this field is generated be the editor, access it to avoid unused field error
		rootObject.get("metadata");

		if (problems.isEmpty()) {
			return Result.success(new SkillDefinitionConfig(
					id,
					optTitle.orElseThrow(),
					description,
					extraDescription,
					optIcon.orElseThrow(),
					frame,
					size,
					rewards,
					cost,
					requiredSkills,
					requiredPoints,
					requiredSpentPoints,
					requiredExclusions
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public void dispose(DisposeContext context) {
		for (var reward : rewards) {
			reward.dispose(context);
		}
	}

}
