package net.puffish.skillsmod.api.json;

import net.puffish.skillsmod.api.utils.Result;

import java.util.function.BiFunction;

public interface JsonListReader<S, F> extends BiFunction<Integer, JsonElementWrapper, Result<S, F>> {

}
