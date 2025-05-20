package net.puffish.skillsmod.server.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
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

	public static PlayerData read(NbtCompound nbt) {
		var categories = new HashMap<Identifier, CategoryData>();

		var categoriesNbt = nbt.getCompoundOrEmpty("categories");
		for (var id : categoriesNbt.getKeys()) {
			var elementNbt = categoriesNbt.get(id);
			if (elementNbt instanceof NbtCompound categoryNbt) {
				categories.put(SkillsMod.convertIdentifier(Identifier.of(id)), CategoryData.read(categoryNbt));
			}
		}

		return new PlayerData(categories);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		var categoriesNbt = new NbtCompound();
		for (var entry : categories.entrySet()) {
			categoriesNbt.put(
					entry.getKey().toString(),
					entry.getValue().writeNbt(new NbtCompound())
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
