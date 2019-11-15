/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.util;

import java.util.List;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;

public class Label {

	public static String ofClass(ClassDef classDef) {
		return fromClassDescriptor(classDef.getType());
	}

	public static String fromClassId(String id) {
		return fromClassDescriptor(Id.toClassDescriptor(id));
	}

	private static String fromClassDescriptor(String descriptor) {
		return TypeName.fromClassDescriptor(descriptor);
	}

	public static String ofTargetMember(String name) {
		return name;
	}

	public static String ofField(Field field) {
		return ofField(field, field.getName());
	}

	public static String ofField(Field field, String name) {
		return name + ':' + TypeName.fromFieldDescriptor(field.getType());
	}

	public static String ofMethod(Method method) {
		return ofMethod(method, method.getName());
	}

	public static String ofMethod(Method method, String name) {
		return ofMethod(method.getParameters(), method.getReturnType(), name);
	}

	public static String ofMethod(List<? extends MethodParameter> parameters, String returnType, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('(');
		boolean first = true;
		for (MethodParameter p : parameters) {
			if (!first) sb.append(", ");
			sb.append(TypeName.fromFieldDescriptor(p.getType()));
			first = false;
		}
		sb.append("):").append(TypeName.fromReturnDescriptor(returnType));
		return sb.toString();
	}

	private Label() {}

}
