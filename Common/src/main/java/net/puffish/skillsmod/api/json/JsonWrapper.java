package net.puffish.skillsmod.api.json;

public class JsonWrapper {
	protected final JsonPath path;

	public JsonWrapper(JsonPath path) {
		this.path = path;
	}

	public JsonPath getPath() {
		return path;
	}
}
