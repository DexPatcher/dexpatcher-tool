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

public abstract class BaseDexTransform {

	public final TransformLogger logger;
	private final String logPrefix;

	protected BaseDexTransform(TransformLogger logger, String logPrefix) {
		this.logger = logger;
		this.logPrefix = logPrefix;
		logger.markAsInUse();
	}

	public final StringBuilder getMessageHeader() {
		StringBuilder sb = new StringBuilder();
		if (logPrefix != null) sb.append(logPrefix).append(": ");
		return sb;
	}

}
