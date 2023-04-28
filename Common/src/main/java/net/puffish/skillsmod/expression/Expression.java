package net.puffish.skillsmod.expression;

import java.util.Map;

public interface Expression<T> {
	T eval(Map<String, T> variables);
}
