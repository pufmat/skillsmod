package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
	private final Map<Identifier, CategoryData> categories;

	private PlayerData(Map<Identifier, CategoryData> categories) {
		this.categories = categories;
	}

	public static PlayerData empty() {
		return new PlayerData(new HashMap<>());
	}

	public static PlayerData read(CompoundTag nbt) {
		var categories = new HashMap<Identifier, CategoryData>();

		var categoriesNbt = nbt.getCompoundOrEmpty("categories");
		for (var id : categoriesNbt.keySet()) {
			var elementNbt = categoriesNbt.get(id);
			if (elementNbt instanceof CompoundTag categoryNbt) {
				categories.put(SkillsMod.convertIdentifier(Identifier.parse(id)), CategoryData.read(categoryNbt));
			}
		}

		return new PlayerData(categories);
	}

	public CompoundTag writeNbt(CompoundTag nbt) {
		var categoriesNbt = new CompoundTag();
		for (var entry : categories.entrySet()) {
			categoriesNbt.put(
					entry.getKey().toString(),
					entry.getValue().writeNbt(new CompoundTag())
			);
		}
		nbt.put("categories", categoriesNbt);

		return nbt;
	}

	public boolean isCategoryUnlocked(CategoryConfig category) {
		var categoryData = categories.get(category.id());
		if (categoryData != null) {
			return categoryData.isUnlocked();
		}
		return category.general().unlockedByDefault();
	}

	public CategoryData getOrCreateCategoryData(CategoryConfig category) {
		return categories.computeIfAbsent(category.id(), key -> CategoryData.create(category.general()));
	}

	public void removeCategoryData(CategoryConfig category) {
		categories.remove(category.id());
	}
}
