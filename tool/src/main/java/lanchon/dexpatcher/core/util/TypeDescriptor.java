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

public abstract class TypeDescriptor {

	public static String fromClass(Class<?> c) {
		return fromName(c.getName());
	}

	public static String fromName(String name) {
		int l = name.length();
		StringBuilder sb = new StringBuilder(l + 2);
		sb.append('L');
		for (int i = 0; i < l; i++) {
			char c = name.charAt(i);
			if (c == '.') c = '/';
			sb.append(c);
		}
		sb.append(';');
		return sb.toString();
	}

	public static boolean isLong(String descriptor) {
		int l = descriptor.length();
		return l >= 2 && descriptor.charAt(l - 1) == ';' && descriptor.charAt(0) == 'L';
	}

}
