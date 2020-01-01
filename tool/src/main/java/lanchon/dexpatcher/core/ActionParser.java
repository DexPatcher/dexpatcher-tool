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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lanchon.dexpatcher.core.util.TypeName;

public class ActionParser {

	private final String annotationPackage;
	private final Map<String, Action> actionMap;

	public ActionParser(String annotationPackage) {
		this.annotationPackage = annotationPackage;
		if (annotationPackage == null) {
			actionMap = Collections.emptyMap();
		} else {
			Action[] actions = Action.values();
			int sizeFactor = 4;
			actionMap = new HashMap<>(sizeFactor * actions.length);
			for (Action action : actions) {
				String actionTypeDescriptor = getTypeDescriptor(action);
				if (actionTypeDescriptor != null) actionMap.put(actionTypeDescriptor, action);
			}
		}
	}

	public String getAnnotationPackage() {
		return annotationPackage;
	}

	public boolean isDisabled() {
		return annotationPackage == null;
	}

	private String getTypeDescriptor(Action action) {
		String className = action.getClassName();
		if (className == null) return null;
		if (!annotationPackage.isEmpty()) className = annotationPackage + '.' + className;
		return TypeName.toClassDescriptor(className);
	}

	public Action parseTypeDescriptor(String typeDescriptor) {
		return actionMap.get(typeDescriptor);
	}

}
