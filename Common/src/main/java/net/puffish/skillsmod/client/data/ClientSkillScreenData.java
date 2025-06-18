package net.puffish.skillsmod.client.data;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ClientSkillScreenData {
	private final Map<Identifier, ClientCategoryData> categories = new LinkedHashMap<>();

	private int offset = 0;

	public void putCategory(Identifier categoryId, ClientCategoryData categoryData) {
		categories.put(categoryId, categoryData);
	}

	public void removeCategory(Identifier categoryId) {
		categories.remove(categoryId);
	}

	public void clearCategories() {
		categories.clear();
	}

	public Optional<ClientCategoryData> getCategory(Identifier categoryId) {
		return Optional.ofNullable(categories.get(categoryId));
	}

	public Collection<ClientCategoryData> getCategories() {
		return categories.values();
	}

	public int getOffset() {
		return offset;
	}

	public void incrementOffset() {
		this.offset++;
	}

	public void decrementOffset() {
		this.offset--;
	}
}
