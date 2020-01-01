/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import java.io.File;

import lanchon.dexpatcher.core.logger.BasicLogger;
import lanchon.dexpatcher.core.logger.Logger;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Context {

	public static final String DEFAULT_ANNOTATION_PACKAGE = "lanchon.dexpatcher.annotation";
	public static final Logger.Level DEFAULT_LOG_LEVEL = WARN;

	public static class Builder {

		private final Logger logger;
		private String annotationPackage = DEFAULT_ANNOTATION_PACKAGE;
		private boolean constructorAutoIgnoreDisabled;
		private String sourceCodeRoot;

		public Builder() {
			this(DEFAULT_LOG_LEVEL);
		}

		public Builder(Logger.Level logLevel) {
			this(new BasicLogger());
			logger.setLogLevel(logLevel);
		}

		public Builder(Logger logger) {
			this.logger = logger;
		}

		public Builder setAnnotationPackage(String value) {
			annotationPackage = value;
			return this;
		}

		public Builder setConstructorAutoIgnoreDisabled(boolean value) {
			constructorAutoIgnoreDisabled = value;
			return this;
		}

		public Builder setSourceCodeRoot(String value) {
			sourceCodeRoot = value;
			return this;
		}

		public Context build() {
			return new Context(logger, annotationPackage, constructorAutoIgnoreDisabled, sourceCodeRoot);
		}

	}

	private final Logger logger;
	private final ActionParser actionParser;
	private final boolean constructorAutoIgnoreDisabled;
	private final String sourceCodeRoot;

	private Context(Logger logger, String annotationPackage, boolean constructorAutoIgnoreDisabled,
			String sourceCodeRoot) {
		this.logger = logger;
		actionParser = new ActionParser(annotationPackage);
		this.constructorAutoIgnoreDisabled = constructorAutoIgnoreDisabled;
		if (sourceCodeRoot != null && !sourceCodeRoot.isEmpty() && !sourceCodeRoot.endsWith(File.separator)) {
			sourceCodeRoot += File.separator;
		}
		this.sourceCodeRoot = sourceCodeRoot;
	}

	public Logger getLogger() {
		return logger;
	}

	public ActionParser getActionParser() {
		return actionParser;
	}

	public boolean isConstructorAutoIgnoreDisabled() {
		return constructorAutoIgnoreDisabled;
	}

	public String getSourceCodeRoot() {
		return sourceCodeRoot;
	}

}
