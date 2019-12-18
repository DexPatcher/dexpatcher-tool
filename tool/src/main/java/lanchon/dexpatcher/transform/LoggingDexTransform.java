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
import lanchon.dexpatcher.core.util.Label;

public abstract class LoggingDexTransform extends DexTransform {

	private static final boolean LOG_REWRITTEN_TYPES = false;

	protected abstract class MemberContext {

		protected final String definingClass;

		public MemberContext(String definingClass) {
			this.definingClass = definingClass;
		}

		public StringBuilder getMessageHeader() {
			StringBuilder sb = getBaseMessageHeader();
			if (definingClass != null) {
				sb.append("type '").append(Label.fromClassDescriptor(definingClass));
				if (LOG_REWRITTEN_TYPES) {
					String rewrittenDefiningClass = getRewrittenDefiningClass();
					if (!rewrittenDefiningClass.equals(definingClass)) {
						sb.append("' -> '").append(Label.fromClassDescriptor(rewrittenDefiningClass));
					}
				}
				sb.append("': ");
			}
			return sb;
		}

		protected abstract String getRewrittenDefiningClass();

	}

	private Logger logger;
	private String logPrefix;

	private HashSet<String> loggedMessages = new HashSet<>();

	protected LoggingDexTransform(Logger logger, String logPrefix) {
		this.logger = logger;
		this.logPrefix = logPrefix;
	}

	public final StringBuilder getBaseMessageHeader() {
		StringBuilder sb = new StringBuilder();
		if (logPrefix != null) sb.append(logPrefix).append(": ");
		return sb;
	}

	public final boolean isLogging(Logger.Level level) {
		// NOTE: Logger level is assumed to be constant during renaming.
		// This is why the call to logger.isLogging() is not synchronized.
		// NOTE: A null value for level disables logging.
		return level != null && logger != null && logger.isLogging(level);
	}

	public final void log(Logger.Level level, String message) {
		if (isLogging(level)) {
			synchronized (this) {
				if (loggedMessages.add(message)) logger.log(level, message);
			}
		}
	}

	@Override
	public void stopLogging(Transform dex) {
		logger = null;
		logPrefix = null;
		loggedMessages = null;
		super.stopLogging(dex);
	}

}
