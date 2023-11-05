package net.puffish.skillsmod.config;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public class GeneralConfig {
	private final Text title;
	private final IconConfig icon;
	private final Identifier background;
	private final boolean unlockedByDefault;
	private final boolean exclusiveRoot;
	private final int spentPointsLimit;
	private final Identifier playSound;

	private GeneralConfig(Text title, IconConfig icon, Identifier background, boolean unlockedByDefault, boolean exclusiveRoot, int spentPointsLimit, Identifier playSound) {
		this.title = title;
		this.icon = icon;
		this.background = background;
		this.unlockedByDefault = unlockedByDefault;
		this.exclusiveRoot = exclusiveRoot;
		this.spentPointsLimit = spentPointsLimit;
		this.playSound = playSound;
	}

	public static Result<GeneralConfig, Failure> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(GeneralConfig::parse);
	}

	public static Result<GeneralConfig, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTitle = rootObject.get("title")
				.andThen(JsonParseUtils::parseText)
				.ifFailure(failures::add)
				.getSuccess();

		var optIcon = rootObject.get("icon")
				.andThen(IconConfig::parse)
				.ifFailure(failures::add)
				.getSuccess();

		var optBackground = rootObject.get("background")
				.andThen(JsonParseUtils::parseIdentifier)
				.ifFailure(failures::add)
				.getSuccess();

		var optUnlockedByDefault = rootObject.getBoolean("unlocked_by_default")
				.ifFailure(failures::add)
				.getSuccess();

		var exclusiveRoot = rootObject.getBoolean("exclusive_root")
				.getSuccess() // ignore failure because this property is optional
				.orElse(false);

		var spentPointsLimit = rootObject.get("spent_points_limit")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElse(Integer.MAX_VALUE);

		var optPlaySound = rootObject.get("play_sound")
				.andThen(JsonParseUtils::parseIdentifier)
				//.ifFailure(failures::add)
				.getSuccess()
				.orElse(Identifier.of("", ""));

		if (failures.isEmpty()) {
			return Result.success(new GeneralConfig(
					optTitle.orElseThrow(),
					optIcon.orElseThrow(),
					optBackground.orElseThrow(),
					optUnlockedByDefault.orElseThrow(),
					exclusiveRoot,
					spentPointsLimit,
					optPlaySound
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	public Text getTitle() {
		return title;
	}

	public boolean isUnlockedByDefault() {
		return unlockedByDefault;
	}

	public boolean isExclusiveRoot() {
		return exclusiveRoot;
	}

	public IconConfig getIcon() {
		return icon;
	}

	public Identifier getBackground() {
		return background;
	}

	public int getSpentPointsLimit() {
		return spentPointsLimit;
	}

	public Identifier getPlaySound() {
		return playSound;
	}
}
