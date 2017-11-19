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

import java.util.List;

import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.Util;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;

public abstract class Ids {

	public static final String STATIC_CONSTRUCTOR = Marker.NAME_STATIC_CONSTRUCTOR + "..V";
	public static final String DEFAULT_CONSTRUCTOR = Marker.NAME_INSTANCE_CONSTRUCTOR + "..V";

	public static String getTypeId(ClassDef classDef) {
		return classDef.getType();
	}

	public static String getTypeIdFromName(String name) {
		return Util.getTypeDescriptorFromName(name);
	}

	public static String getFieldId(Field field) {
		return getFieldId(field, field.getName());
	}

	public static String getFieldId(Field field, String name) {
		return name + '.' + field.getType();
	}

	public static String getMethodId(Method method) {
		return getMethodId(method, method.getName());
	}

	public static String getMethodId(Method method, String name) {
		return getMethodId(method.getParameters(), method.getReturnType(), name);
	}

	public static String getMethodId(List<? extends MethodParameter> parameters, String returnType, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('.');
		for (MethodParameter p : parameters) sb.append(p.getType());
		sb.append('.').append(returnType);
		return sb.toString();
	}

}
