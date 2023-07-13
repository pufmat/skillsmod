package net.puffish.skillsmod.config.skill;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.puffish.skillsmod.config.ConfigContext;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.error.ManyErrors;
import net.puffish.skillsmod.utils.Result;

import java.util.ArrayList;
import java.util.List;

public class SkillDefinitionConfig {
	private final String id;
	private final Text title;
	private final Text description;
	private final IconConfig icon;
	private final AdvancementFrame frame;
	private final List<SkillRewardConfig> rewards;
	private final int cost;

	private SkillDefinitionConfig(String id, Text title, Text description, IconConfig icon, AdvancementFrame frame, List<SkillRewardConfig> rewards, int cost) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.icon = icon;
		this.frame = frame;
		this.rewards = rewards;
		this.cost = cost;
	}

	public static Result<SkillDefinitionConfig, Error> parse(String id, JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> SkillDefinitionConfig.parse(id, rootObject, context));
	}

	public static Result<SkillDefinitionConfig, Error> parse(String id, JsonObjectWrapper rootObject, ConfigContext context) {
		var errors = new ArrayList<Error>();

		var optTitle = rootObject.get("title")
				.andThen(JsonParseUtils::parseText)
				.ifFailure(errors::add)
				.getSuccess();

		var optDescription = rootObject.get("description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> JsonParseUtils.parseText(descriptionElement)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElseGet(Text::empty);

		var optIcon = rootObject.get("icon")
				.andThen(IconConfig::parse)
				.ifFailure(errors::add)
				.getSuccess();

		var frame = rootObject.get("frame")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(frameElement -> JsonParseUtils.parseFrame(frameElement)
						.ifFailure(errors::add)
						.getSuccess()
				)
				.orElse(AdvancementFrame.TASK);

		var rewards = rootObject.getArray("rewards")
				.andThen(array -> array.getAsList((i, element) -> SkillRewardConfig.parse(element, context)).mapFailure(ManyErrors::ofList))
				.ifFailure(errors::add)
				.getSuccess()
				.orElseGet(List::of);

		var cost = rootObject.getInt("cost")
				.getSuccess() // ignore failure because this property is optional
				.orElse(1);

		if (errors.isEmpty()) {
			return Result.success(new SkillDefinitionConfig(
					id,
					optTitle.orElseThrow(),
					optDescription,
					optIcon.orElseThrow(),
					frame,
					rewards,
					cost
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
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

	public AdvancementFrame getFrame() {
		return frame;
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
}
