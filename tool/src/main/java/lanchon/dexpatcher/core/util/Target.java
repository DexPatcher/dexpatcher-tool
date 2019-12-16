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

public class Target {

	public static String resolveClassDescriptor(String baseDescriptor, String target) {
		// Precondition: target is a non-zero length string.
		if (!DexUtils.isClassDescriptor(target)) {
			String baseName = TypeName.fromClassDescriptor(baseDescriptor);
			target = TypeName.toClassDescriptor(resolveClassName(baseName, target));
		}
		return target;
	}

	private static String resolveClassName(String baseName, String target) {
		int targetDot = target.indexOf('.');
		if (targetDot < 0) {
			// If target is not fully qualified:
			int baseNameEnd = baseName.lastIndexOf('.');
			if (target.indexOf('$') < 0) {
				// If target is not a qualified nested type:
				baseNameEnd = Math.max(baseNameEnd, baseName.lastIndexOf('$'));
			}
			if (baseNameEnd >= 0) {
				target = baseName.substring(0, baseNameEnd + 1) + target;
			}
		} else if (targetDot == 0) {
			// If fully qualified target starts with '.':
			target = target.substring(1);
		}
		return target;
	}

	public static String resolvePackageDescriptor(String target) {
		// Precondition: target is a non-zero length string.
		if (!DexUtils.isClassDescriptor(target)) {
			if (target.startsWith(".")) target = target.substring(1);
			target = (target.length() == 0) ? Marker.NAME_PACKAGE_INFO : target + '.' + Marker.NAME_PACKAGE_INFO;
			target = TypeName.toClassDescriptor(target);
		}
		return target;
	}

	private Target() {}

}
