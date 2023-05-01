package net.puffish.skillsmod.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefixedLogger {
	private final Logger logger;
	private final String prefix;

	public PrefixedLogger(String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.prefix = name;
	}

	private String addPrefix(String str) {
		return "[" + prefix + "] " + str;
	}

	public void info(String str) {
		logger.info(addPrefix(str));
	}

	public void warn(String str) {
		logger.warn(addPrefix(str));
	}

	public void error(String str) {
		logger.error(addPrefix(str));
	}
}
