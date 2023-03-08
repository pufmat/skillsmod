package net.puffish.skillsmod.json;

import net.puffish.skillsmod.utils.Result;

import java.util.function.BiFunction;

public interface JsonListReader<S, F> extends BiFunction<Integer, JsonElementWrapper, Result<S, F>> {

}
