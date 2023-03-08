package net.puffish.skillsmod.json;

import net.puffish.skillsmod.utils.Result;

import java.util.function.BiFunction;

public interface JsonMapReader<S, F> extends BiFunction<String, JsonElementWrapper, Result<S, F>> {

}
