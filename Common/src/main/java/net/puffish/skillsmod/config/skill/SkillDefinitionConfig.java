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

public class SkillDefinitionConfig {
	private final String id;
	private final Text title;
	private final Text description;
	private final Text extraDescription;
	private final IconConfig icon;
	private final FrameConfig frame;
	private final float size;
	private final List<SkillRewardConfig> rewards;
	private final int cost;
	private final int requiredSkills;
	private final int requiredPoints;
	private final int requiredSpentPoints;

	private SkillDefinitionConfig(String id, Text title, Text description, Text extraDescription, IconConfig icon, FrameConfig frame, float size, List<SkillRewardConfig> rewards, int cost, int requiredSkills, int requiredPoints, int requiredSpentPoints) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.extraDescription = extraDescription;
		this.icon = icon;
		this.frame = frame;
		this.size = size;
		this.rewards = rewards;
		this.cost = cost;
		this.requiredSkills = requiredSkills;
		this.requiredPoints = requiredPoints;
		this.requiredSpentPoints = requiredSpentPoints;
	}

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
					requiredSpentPoints
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

	public String getId() {
		return id;
	}

	public Text getTitle() {
		return title;
	}

	public Text getDescription() {
		return description;
	}

	public Text getExtraDescription() {
		return extraDescription;
	}

	public FrameConfig getFrame() {
		return frame;
	}

	public float getSize() {
		return size;
	}

	public IconConfig getIcon() {
		return icon;
	}

	public List<SkillRewardConfig> getRewards() {
		return rewards;
	}

	public int getCost() {
		return cost;
	}

	public int getRequiredSkills() {
		return requiredSkills;
	}

	public int getRequiredPoints() {
		return requiredPoints;
	}

	public int getRequiredSpentPoints() {
		return requiredSpentPoints;
	}
}
