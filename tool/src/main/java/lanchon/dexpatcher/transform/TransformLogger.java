/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform;

import java.util.HashSet;

import lanchon.dexpatcher.core.logger.Logger;

public final class TransformLogger {

	private Logger logger;
	private boolean inUse;
	private boolean sync;
	private HashSet<String> loggedMessages;

	public TransformLogger(Logger logger) {
		this.logger = logger;
		if (logger != null) loggedMessages = new HashSet<>();
	}

	public boolean isInUse() {
		return inUse;
	}

	public void markAsInUse() {
		this.inUse = true;
	}

	public boolean getSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public boolean isLogging() {
		return logger != null;
	}

	public boolean isLogging(Logger.Level level) {
		// NOTE: Logger level is assumed to be constant during renaming.
		// This is why the call to logger.isLogging() is not synchronized.
		// NOTE: A null value for level disables logging.
		return logger != null && level != null && logger.isLogging(level);
	}

	public void log(Logger.Level level, String message) {
		if (isLogging(level)) {
			if (sync) {
				synchronized (logger) {
					if (loggedMessages.add(message)) logger.log(level, message);
				}
			} else {
				if (loggedMessages.add(message)) logger.log(level, message);
			}
		}
	}

	public void stopLogging() {
		logger = null;
		loggedMessages = null;
	}

	public TransformLogger cloneIf(boolean condition) {
		return condition ? new TransformLogger(logger) : this;
	}

}
