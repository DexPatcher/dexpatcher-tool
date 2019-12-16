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

public class TypeName {

	public static String fromClassDescriptor(String descriptor) {
		// TODO: Catch invalid type descriptor exceptions in client code.
		if (!DexUtils.isClassDescriptor(descriptor)) throw invalidTypeDescriptor(descriptor);
		int l = descriptor.length();
		StringBuilder sb = new StringBuilder(l - 2);
		for (int i = 1; i < l - 1; i++) {
			char c = descriptor.charAt(i);
			sb.append(c == '/' ? '.' : c);
		}
		return sb.toString();
	}

	public static String toClassDescriptor(String name) {
		int l = name.length();
		StringBuilder sb = new StringBuilder(l + 2);
		sb.append('L');
		for (int i = 0; i < l; i++) {
			char c = name.charAt(i);
			sb.append(c == '.' ? '/' : c);
		}
		sb.append(';');
		return sb.toString();
	}

	public static String fromFieldDescriptor(String descriptor) {
		if (descriptor.length() == 0) throw invalidTypeDescriptor(descriptor);
		switch (descriptor.charAt(0)) {
			case '[': return fromFieldDescriptor(descriptor.substring(1)) + "[]";
			case 'L': return fromClassDescriptor(descriptor);
			case 'Z': return "boolean";
			case 'B': return "byte";
			case 'S': return "short";
			case 'C': return "char";
			case 'I': return "int";
			case 'J': return "long";
			case 'F': return "float";
			case 'D': return "double";
			default:  throw invalidTypeDescriptor(descriptor);
		}
	}

	public static String fromReturnDescriptor(String descriptor) {
		// Void is only valid for return types.
		return "V".equals(descriptor) ? "void" : fromFieldDescriptor(descriptor);
	}

	private static RuntimeException invalidTypeDescriptor(String descriptor) {
		return new RuntimeException("Invalid type descriptor (" + descriptor + ")");
	}

	private TypeName() {}

}
