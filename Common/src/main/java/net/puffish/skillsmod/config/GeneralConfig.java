package net.puffish.skillsmod.config;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.failure.Failure;
import net.puffish.skillsmod.utils.failure.ManyFailures;

import java.util.ArrayList;

public class GeneralConfig {
	private final Text title;
	private final IconConfig icon;
	private final Identifier background;
	private final boolean unlockedByDefault;
	private final boolean exclusiveRoot;

	private GeneralConfig(Text title, IconConfig icon, Identifier background, boolean unlockedByDefault, boolean exclusiveRoot) {
		this.title = title;
		this.icon = icon;
		this.background = background;
		this.unlockedByDefault = unlockedByDefault;
		this.exclusiveRoot = exclusiveRoot;
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

		var optExclusiveRoot = rootObject.getBoolean("exclusive_root")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new GeneralConfig(
					optTitle.orElseThrow(),
					optIcon.orElseThrow(),
					optBackground.orElseThrow(),
					optUnlockedByDefault.orElseThrow(),
					optExclusiveRoot.orElseThrow()
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
}
