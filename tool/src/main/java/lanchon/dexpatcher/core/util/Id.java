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

import lanchon.dexpatcher.core.Marker;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class Id {

	public static final String STATIC_CONSTRUCTOR = Marker.NAME_STATIC_CONSTRUCTOR + "..V";

	public static String ofClass(ClassDef classDef) {
		return ofClass(classDef.getType());
	}

	public static String ofClass(String descriptor) {
		return descriptor;
	}

	public static String toClassDescriptor(String id) {
		return id;
	}

	public static String ofField(FieldReference field) {
		return ofField(field.getType(), field.getName());
	}

	public static String ofField(FieldReference field, String name) {
		return ofField(field.getType(), name);
	}

	public static String ofField(String type, String name) {
		return name + '.' + type;
	}

	public static String ofMethod(MethodReference method) {
		return ofMethod(method.getParameterTypes(), method.getReturnType(), method.getName());
	}

	public static String ofMethod(MethodReference method, String name) {
		return ofMethod(method.getParameterTypes(), method.getReturnType(), name);
	}

	public static String ofMethod(Iterable<? extends CharSequence> parameterTypes, String returnType, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('.');
		for (CharSequence parameterType : parameterTypes) sb.append(parameterType);
		sb.append('.').append(returnType);
		return sb.toString();
	}

	private Id() {}

}
