package net.puffish.skillsmod.api.json;

import net.puffish.skillsmod.api.utils.Result;

import java.util.function.BiFunction;

public interface JsonMapReader<S, F> extends BiFunction<String, JsonElementWrapper, Result<S, F>> {

}
