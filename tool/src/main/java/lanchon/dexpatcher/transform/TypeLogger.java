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

import lanchon.dexpatcher.core.util.Label;

public abstract class TypeLogger {

	public final TransformLogger logger;
	private final String logPrefix;

	protected TypeLogger(TransformLogger logger, String logPrefix) {
		this.logger = logger;
		this.logPrefix = logPrefix;
		logger.markAsInUse();
	}

	public final StringBuilder getMessageHeader() {
		StringBuilder sb = new StringBuilder();
		if (logPrefix != null) sb.append(logPrefix).append(": ");
		return sb;
	}

	public final StringBuilder getMessageHeaderForClass(String descriptor) {
		StringBuilder sb = getMessageHeader();
		sb.append("type '").append(Label.fromClassDescriptor(descriptor)).append("': ");
		return sb;
	}

}
