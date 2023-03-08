package net.puffish.skillsmod.config;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.json.JsonElementWrapper;
import net.puffish.skillsmod.json.JsonObjectWrapper;
import net.puffish.skillsmod.utils.JsonParseUtils;
import net.puffish.skillsmod.utils.Result;
import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.ManyErrors;

import java.util.ArrayList;

public class GeneralConfig {
	private final Text title;
	private final IconConfig icon;
	private final Identifier background;
	private final boolean unlockedByDefault;

	private GeneralConfig(Text title, IconConfig icon, Identifier background, boolean unlockedByDefault) {
		this.title = title;
		this.icon = icon;
		this.background = background;
		this.unlockedByDefault = unlockedByDefault;
	}

	public static Result<GeneralConfig, Error> parse(JsonElementWrapper rootElement) {
		return rootElement.getAsObject()
				.andThen(GeneralConfig::parse);
	}

	public static Result<GeneralConfig, Error> parse(JsonObjectWrapper rootObject) {
		var errors = new ArrayList<Error>();

		var optTitle = rootObject.get("title")
				.andThen(JsonParseUtils::parseText)
				.ifFailure(errors::add)
				.getSuccess();

		var optIcon = rootObject.get("icon")
				.andThen(IconConfig::parse)
				.ifFailure(errors::add)
				.getSuccess();

		var optBackground = rootObject.get("background")
				.andThen(JsonParseUtils::parseIdentifier)
				.ifFailure(errors::add)
				.getSuccess();

		var unlockedByDefault = rootObject.getBoolean("unlocked_by_default")
				.getSuccess()
				.orElse(false);

		if (errors.isEmpty()) {
			return Result.success(new GeneralConfig(
					optTitle.orElseThrow(),
					optIcon.orElseThrow(),
					optBackground.orElseThrow(),
					unlockedByDefault
			));
		} else {
			return Result.failure(ManyErrors.ofList(errors));
		}
	}

	public Text getTitle() {
		return title;
	}

	public boolean isUnlockedByDefault() {
		return unlockedByDefault;
	}

	public IconConfig getIcon() {
		return icon;
	}

	public Identifier getBackground() {
		return background;
	}
}
