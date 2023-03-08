package net.puffish.skillsmod.json;

import com.google.gson.JsonArray;
import net.puffish.skillsmod.utils.Result;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayWrapper extends JsonWrapper {
	private final JsonArray json;

	public JsonArrayWrapper(JsonArray json, JsonPath path) {
		super(path);
		this.json = json;
	}

	public <S, F> Result<List<S>, List<F>> getAsList(JsonListReader<S, F> reader) {
		var exceptions = new ArrayList<F>();
		var list = new ArrayList<S>();

		var tmp = json.asList();
		for (int i = 0; i < tmp.size(); i++) {
			reader.apply(
					i,
					new JsonElementWrapper(tmp.get(i), path.thenArray(i))
			).peek(
					list::add,
					exceptions::add
			);
		}

		if (exceptions.isEmpty()) {
			return Result.success(list);
		} else {
			return Result.failure(exceptions);
		}
	}

	public int getSize() {
		return json.size();
	}

	public JsonArray getJson() {
		return json;
	}
}
