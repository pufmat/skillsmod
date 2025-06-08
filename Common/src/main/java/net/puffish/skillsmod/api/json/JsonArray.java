package net.puffish.skillsmod.api.json;

import net.puffish.skillsmod.api.util.Result;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public interface JsonArray {

	Stream<JsonElement> stream();

	JsonElement getAsElement();

	<S, F> Result<List<S>, List<F>> getAsList(BiFunction<Integer, JsonElement, Result<S, F>> function);

	int getSize();

	JsonPath getPath();

	com.google.gson.JsonArray getJson();
}
