/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
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
	private int logLevelOrdinal;
	private int isLoggingOrdinal;
	private int[] counts;

	public Logger() {
		setLogLevel(DEBUG);
		clearMessageCounts();
	}

	public final void log(Level level, String message) {
		log(level, message, null);
	}

	public final void log(Level level, String message, Throwable throwable) {
		if (message == null) throw new NullPointerException("message");
		int levelOrdinal = level.ordinal();
		counts[levelOrdinal]++;
		if (levelOrdinal >= logLevelOrdinal) doLog(level, message, throwable);
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
		logLevelOrdinal = logLevel.ordinal();
		isLoggingOrdinal = Math.min(logLevelOrdinal, WARN.ordinal());
	}

	public final boolean isLogging(Level level) {
		return level.ordinal() >= isLoggingOrdinal;
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
