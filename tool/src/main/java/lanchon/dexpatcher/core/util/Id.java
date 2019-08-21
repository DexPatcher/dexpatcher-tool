/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.util;

import java.util.ArrayList;
import java.util.List;

import lanchon.dexpatcher.core.Marker;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class Id {

	public static final String STATIC_CONSTRUCTOR = Marker.NAME_STATIC_CONSTRUCTOR + "..V";
	public static final String DEFAULT_CONSTRUCTOR = Marker.NAME_INSTANCE_CONSTRUCTOR + "..V";

	public static String ofClass(ClassDef classDef) {
		return fromClassDescriptor(classDef.getType());
	}

	public static String fromClassDescriptor(String descriptor) {
		return descriptor;
	}

	public static String toClassDescriptor(String id) {
		return id;
	}

	public static String ofField(Field field) {
		return ofField(field, field.getName());
	}

	public static String ofField(Field field, String name) {
		return ofField(field.getType(), name);
	}

	public static String ofField(FieldReference field) {
		return ofField(field.getType(), field.getName());
	}

	public static String ofField(String type, String name) {
		return name + '.' + type;
	}

	public static String ofMethod(Method method) {
		return ofMethod(method, method.getName());
	}

	public static String ofMethod(Method method, String name) {
		return ofMethod(method.getParameters(), method.getReturnType(), name);
	}

	public static String ofMethod(List<? extends MethodParameter> parameters, String returnType, String name) {
		List<String> stringParameters = new ArrayList<>();

		for (MethodParameter p : parameters) {
			stringParameters.add(p.getType());
		}

		return ofMethodInternal(stringParameters, returnType, name);
	}

	public static String ofMethod(MethodReference method) {
		return ofMethodInternal(method.getParameterTypes(), method.getReturnType(), method.getName());
	}

	private static String ofMethodInternal(List<? extends CharSequence> parameters, String returnType, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('.');
		for (CharSequence p : parameters) sb.append(p);
		sb.append('.').append(returnType);
		return sb.toString();
	}

	private Id() {}

}
