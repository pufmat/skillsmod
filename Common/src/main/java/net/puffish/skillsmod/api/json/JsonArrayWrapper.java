package net.puffish.skillsmod.api.json;

import com.google.gson.JsonArray;
import net.puffish.skillsmod.api.utils.Result;

import java.util.List;

public interface JsonArrayWrapper extends JsonWrapper {
	<S, F> Result<List<S>, List<F>> getAsList(JsonListReader<S, F> reader);

	int getSize();

	JsonArray getJson();
}
