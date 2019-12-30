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

import com.google.common.collect.ImmutableBiMap;

public class TypeName {

	private static final ImmutableBiMap<String, String> fieldTypeToNameMap = ImmutableBiMap.<String, String>builder()
			.put("Z", "boolean")
			.put("C", "char")
			.put("B", "byte")
			.put("S", "short")
			.put("I", "int")
			.put("J", "long")
			.put("F", "float")
			.put("D", "double")
			.build();

	private static final ImmutableBiMap<String, String> returnTypeToNameMap = ImmutableBiMap.<String, String>builder()
			.putAll(fieldTypeToNameMap)
			.put(Marker.TYPE_VOID, Marker.NAME_VOID)
			.build();

	private static final ImmutableBiMap<String, String> nameToFieldTypeMap = fieldTypeToNameMap.inverse();

	public static String fromClassDescriptor(String descriptor) throws InvalidTypeDescriptorException {
		if (!DexUtils.isClassDescriptor(descriptor)) {
			throw new InvalidTypeDescriptorException("class", descriptor);
		}
		int length = descriptor.length();
		StringBuilder sb = new StringBuilder(length - 2);
		length--;
		for (int i = 1; i < length; i++) {
			char c = descriptor.charAt(i);
			sb.append(c == '/' ? '.' : c);
		}
		String name = sb.toString();
		if (returnTypeToNameMap.containsValue(name)) name = '.' + name;
		return name;
	}

	public static String toClassDescriptor(String name) {
		int length = name.length();
		int start = name.startsWith(".") ? 1 : 0;
		StringBuilder sb = new StringBuilder(length - start + 2);
		sb.append('L');
		for (int i = start; i < length; i++) {
			char c = name.charAt(i);
			sb.append(c == '.' ? '/' : c);
		}
		sb.append(';');
		return sb.toString();
	}

	public static String fromFieldDescriptor(String descriptor) throws InvalidTypeDescriptorException {
		try {
			if (descriptor.length() == 1) {
				String name = fieldTypeToNameMap.get(descriptor);
				if (name != null) return name;
			} else if (descriptor.startsWith("[")) {
				return fromFieldDescriptor(descriptor.substring(1)) + "[]";
			}
			return fromClassDescriptor(descriptor);
		} catch (InvalidTypeDescriptorException e) {
			throw new InvalidTypeDescriptorException("field", descriptor);
		}
	}

	public static String toFieldDescriptor(String name) {
		if (name.endsWith("[]")) {
			return "[" + toFieldDescriptor(name.substring(0, name.length() - 2));
		}
		String type = nameToFieldTypeMap.get(name);
		return type != null ? type : toClassDescriptor(name);
	}

	public static String fromReturnDescriptor(String descriptor) throws InvalidTypeDescriptorException {
		try {
			return Marker.TYPE_VOID.equals(descriptor) ? Marker.NAME_VOID : fromFieldDescriptor(descriptor);
		} catch (InvalidTypeDescriptorException e) {
			throw new InvalidTypeDescriptorException("return", descriptor);
		}
	}

	public static String toReturnDescriptor(String name) {
		return Marker.NAME_VOID.equals(name) ? Marker.TYPE_VOID : toFieldDescriptor(name);
	}

	private TypeName() {}

}
