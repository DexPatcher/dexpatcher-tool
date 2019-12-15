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
			String actionTypeDescriptor = getTypeDescriptorFromAction(action);
			if (actionTypeDescriptor != null) actionMap.put(actionTypeDescriptor, action);
		}
	}

	public String getAnnotationPackage() {
		return annotationPackage;
	}

	private String getTypeDescriptorFromAction(Action action) {
		String className = action.getClassName();
		if (className == null) return null;
		if (annotationPackage.length() != 0) className = annotationPackage + '.' + className;
		return TypeName.toClassDescriptor(className);
	}

	public Action getActionFromTypeDescriptor(String typeDescriptor) {
		return actionMap.get(typeDescriptor);
	}

}
