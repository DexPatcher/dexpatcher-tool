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

import java.util.HashMap;

import lanchon.dexpatcher.core.util.TypeName;

public class ActionParser {

	public static final String DEFAULT_ANNOTATION_PACKAGE = "lanchon.dexpatcher.annotation";

	private final String annotationPackage;
	private final HashMap<String, Action> actionMap;

	public ActionParser(String annotationPackage) {
		this.annotationPackage = annotationPackage;
		Action[] actions = Action.values();
		int sizeFactor = 4;
		actionMap = new HashMap<>(sizeFactor * actions.length);
		for (Action action : actions) {
			Marker marker = action.getMarker();
			if (marker != null) actionMap.put(getMarkerTypeDescriptor(marker), action);
		}
	}

	public String getAnnotationPackage() {
		return annotationPackage;
	}

	private String getMarkerTypeDescriptor(Marker marker) {
		String className = marker.getClassName();
		if (annotationPackage.length() != 0) className = annotationPackage + '.' + className;
		return TypeName.toClassDescriptor(className);
	}

	public Action getActionFromMarkerTypeDescriptor(String descriptor) {
		return actionMap.get(descriptor);
	}

}
