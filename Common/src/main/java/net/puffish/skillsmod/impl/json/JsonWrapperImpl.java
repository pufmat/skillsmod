package net.puffish.skillsmod.impl.json;

import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.json.JsonWrapper;

public class JsonWrapperImpl implements JsonWrapper {
	protected final JsonPath path;

	public JsonWrapperImpl(JsonPath path) {
		this.path = path;
	}

	@Override
	public JsonPath getPath() {
		return path;
	}
}
