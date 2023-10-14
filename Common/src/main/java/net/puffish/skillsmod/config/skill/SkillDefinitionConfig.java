package net.puffish.skillsmod.config.skill;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.List;

public class SkillDefinitionConfig {
	private final String id;
	private final Text title;
	private final Text description;
	private final IconConfig icon;
	private final FrameConfig frame;
	private final float size;
	private final List<SkillRewardConfig> rewards;
	private final int cost;
	private final int requiredPoints;
	private final int requiredSpentPoints;

	private SkillDefinitionConfig(String id, Text title, Text description, IconConfig icon, FrameConfig frame, float size, List<SkillRewardConfig> rewards, int cost, int requiredPoints, int requiredSpentPoints) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.icon = icon;
		this.frame = frame;
		this.size = size;
		this.rewards = rewards;
		this.cost = cost;
		this.requiredPoints = requiredPoints;
		this.requiredSpentPoints = requiredSpentPoints;
	}

	public static Result<SkillDefinitionConfig, Failure> parse(String id, JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> SkillDefinitionConfig.parse(id, rootObject, context));
	}

	public static Result<SkillDefinitionConfig, Failure> parse(String id, JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optTitle = rootObject.get("title")
				.andThen(JsonParseUtils::parseText)
				.ifFailure(failures::add)
				.getSuccess();

		var optDescription = rootObject.get("description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> JsonParseUtils.parseText(descriptionElement)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(Text::empty);

		var optIcon = rootObject.get("icon")
				.andThen(IconConfig::parse)
				.ifFailure(failures::add)
				.getSuccess();

		var frame = rootObject.get("frame")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(frameElement -> JsonParseUtils.parseFrame(frameElement)
						.mapSuccess(FrameConfig::fromAdvancementFrame)
						.orElse(failure -> FrameConfig.parse(frameElement))
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElseGet(() -> FrameConfig.fromAdvancementFrame(AdvancementFrame.TASK));

		var size = rootObject.getFloat("size")
				.getSuccess() // ignore failure because this property is optional
				.orElse(1f);

		var rewards = rootObject.getArray("rewards")
				.andThen(array -> array.getAsList((i, element) -> SkillRewardConfig.parse(element, context)).mapFailure(ManyFailures::ofList))
				.ifFailure(failures::add)
				.getSuccess()
				.orElseGet(List::of);

		var cost = rootObject.getInt("cost")
				.getSuccess() // ignore failure because this property is optional
				.orElse(1);

		var requiredPoints = rootObject.getInt("required_points")
				.getSuccess() // ignore failure because this property is optional
				.orElse(0);

		var requiredSpentPoints = rootObject.getInt("required_spent_points")
				.getSuccess() // ignore failure because this property is optional
				.orElse(0);

		if (failures.isEmpty()) {
			return Result.success(new SkillDefinitionConfig(
					id,
					optTitle.orElseThrow(),
					optDescription,
					optIcon.orElseThrow(),
					frame,
					size,
					rewards,
					cost,
					requiredPoints,
					requiredSpentPoints
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public void dispose(MinecraftServer server) {
		for (var reward : rewards) {
			reward.dispose(server);
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

	public int getRequiredPoints() {
		return requiredPoints;
	}

	public int getRequiredSpentPoints() {
		return requiredSpentPoints;
	}
}
