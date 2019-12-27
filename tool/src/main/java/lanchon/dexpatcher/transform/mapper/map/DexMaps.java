/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper.map;

import lanchon.dexpatcher.core.util.DexUtils;

public class DexMaps {

	public static String mapType(String descriptor, DexMap dexMap) {
		int length = descriptor.length();
		if (length == 0 || descriptor.charAt(0) != '[') return mapElementalType(descriptor, dexMap);
		int start = 1;
		while (start < length && descriptor.charAt(start) == '[') start++;
		String elementalType = descriptor.substring(start);
		String mappedElementalType = mapElementalType(elementalType, dexMap);
		if (mappedElementalType.equals(elementalType)) return descriptor;
		StringBuilder sb = new StringBuilder(start + mappedElementalType.length());
		sb.append(descriptor, 0, start).append(mappedElementalType);
		return sb.toString();
	}

	public static String mapElementalType(String descriptor, DexMap dexMap) {
		if (DexUtils.isClassDescriptor(descriptor)) {
			String mapping = dexMap.getClassMapping(descriptor);
			if (mapping != null) return mapping;
		}
		return descriptor;
	}

	private DexMaps() {}

}
