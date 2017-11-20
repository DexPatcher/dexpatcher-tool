/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.logger;

import java.io.Closeable;
import java.io.Flushable;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public abstract class Logger implements Flushable, Closeable {

	public enum Level {

		DEBUG("debug"),
		INFO("info"),
		WARN("warning"),
		ERROR("error"),
		FATAL("fatal"),
		NONE("none");

		private final String label;

		Level(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

	private Level logLevel;
	private int[] counts;

	public Logger() {
		this.logLevel = DEBUG;
		clearMessageCounts();
	}

	public final void log(Level level, String message) {
		log(level, message, null);
	}

	public final void log(Level level, String message, Throwable throwable) {
		if (message == null) throw new NullPointerException("message");
		counts[level.ordinal()]++;
		if (isLogging(level)) doLog(level, message, throwable);
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	public final boolean isLogging(Level level) {
		return level.ordinal() >= logLevel.ordinal();
	}

	public int getMessageCount(Level level) {
		return counts[level.ordinal()];
	}

	public void clearMessageCounts() {
		counts = new int[Level.values().length];
	}

	public boolean hasNotLoggedErrors() {
		int errors = getMessageCount(FATAL) + getMessageCount(ERROR);
		return errors == 0;
	}

	public void logErrorAndWarningCounts() {
		int errors = getMessageCount(FATAL) + getMessageCount(ERROR);
		int warnings = getMessageCount(WARN);
		//if (errors != 0 || warnings != 0) {
			log(NONE, errors + " error(s), " + warnings + " warning(s)");
		//}
	}

	protected abstract void doLog(Level level, String message, Throwable throwable);

	public abstract void flush();
	public abstract void close();

}
