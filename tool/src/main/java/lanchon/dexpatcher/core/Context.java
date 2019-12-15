/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lanchon.dexpatcher.core.logger.BasicLogger;
import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.util.TypeName;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Context {

	public static final Logger.Level DEFAULT_LOG_LEVEL = WARN;
	public static final String DEFAULT_ANNOTATION_PACKAGE = "lanchon.dexpatcher.annotation";

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
	private final String annotationPackage;
	private final boolean constructorAutoIgnoreDisabled;
	private final String sourceCodeRoot;

	private Map<String, Action> actionMap;

	private Context(Logger logger, String annotationPackage, boolean constructorAutoIgnoreDisabled,
			String sourceCodeRoot) {
		this.logger = logger;
		this.annotationPackage = annotationPackage;
		this.constructorAutoIgnoreDisabled = constructorAutoIgnoreDisabled;

		if (sourceCodeRoot != null && sourceCodeRoot.length() > 0 && !sourceCodeRoot.endsWith(File.separator)) {
			sourceCodeRoot += File.separator;
		}
		this.sourceCodeRoot = sourceCodeRoot;

		Action[] actions = Action.values();
		int sizeFactor = 4;
		actionMap = new HashMap<>(sizeFactor * actions.length);
		for (Action action : actions) {
			Marker marker = action.getMarker();
			if (marker != null) actionMap.put(getMarkerTypeDescriptor(marker), action);
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public String getAnnotationPackage() {
		return annotationPackage;
	}

	public boolean isConstructorAutoIgnoreDisabled() {
		return constructorAutoIgnoreDisabled;
	}

	public String getSourceCodeRoot() {
		return sourceCodeRoot;
	}

	// Extras

	private String getMarkerTypeDescriptor(Marker marker) {
		return TypeName.toClassDescriptor(annotationPackage + "." + marker.getClassName());
	}

	public Action getActionFromMarkerTypeDescriptor(String descriptor) {
		return actionMap.get(descriptor);
	}

}
