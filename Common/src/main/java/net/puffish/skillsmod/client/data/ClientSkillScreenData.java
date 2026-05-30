package net.puffish.skillsmod.client.data;

import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

	public Stream<ClientCategoryData> streamCategories() {
		return categories.values().stream().sorted(Comparator.comparing(data -> data.getConfig().position()));
	}

	public int getCategoriesCount() {
		return categories.size();
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
