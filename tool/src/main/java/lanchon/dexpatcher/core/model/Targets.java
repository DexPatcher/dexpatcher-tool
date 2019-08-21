/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lanchon.dexpatcher.core.util.Id;

import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class Targets {
	private HashMap<String, TargetClass> classes;

	public class TargetClass {
		public String target;

		// method id => target name
		public HashMap<String, String> methods;

		// field id => target name
		public HashMap<String, String> fields;

		public TargetClass(String patchTarget) {
			this.target = patchTarget;
			this.methods = new HashMap<>();
			this.fields = new HashMap<>();

		}
	}

	public Targets() {
		this.classes = new HashMap<>();
	}

	public String getRetargetedClassName(String originalType) {
		return getTargetClass(originalType).target;
	}

	public String getRetargetedFieldName(FieldReference field) {
		TargetClass targetClass = getTargetClass(field.getDefiningClass());
		String fieldName = targetClass.fields.get(Id.ofField(field));

		if (fieldName == null) {
			fieldName = field.getName();
		}

		return fieldName;
	}

	public String getRetargetedMethodName(MethodReference method) {
		TargetClass targetClass = getTargetClass(method.getDefiningClass());
		String methodName = targetClass.fields.get(Id.ofMethod(method));

		if (methodName == null) {
			methodName = method.getName();
		}

		return methodName;
	}

	public void addClass(String patchClass) {
		if (this.classes.containsKey(patchClass)) {
			return;
		}

		this.classes.put(patchClass, new TargetClass(patchClass));
	}

	public TargetClass getTargetClass(String patchClass) {
		TargetClass targetClass = this.classes.get(patchClass);

		if (targetClass == null) {
			targetClass = new TargetClass(patchClass);
		}

		return targetClass;
	}
}
