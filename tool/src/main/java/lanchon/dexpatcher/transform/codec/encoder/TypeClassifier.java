/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec.encoder;

import java.util.regex.Pattern;

public final class TypeClassifier {

	private final Pattern obfuscatedBinaryTypeNamePattern;
	private final Pattern obfuscatedClassNamePattern;

	public TypeClassifier(Pattern obfuscatedBinaryTypeNamePattern, Pattern obfuscatedClassNamePattern) {
		this.obfuscatedBinaryTypeNamePattern = obfuscatedBinaryTypeNamePattern;
		this.obfuscatedClassNamePattern = obfuscatedClassNamePattern;
	}

	public boolean isObfuscatedType(String type, boolean defaultValue) {
		//if (!DexUtils.isClassDescriptor(type)) throw new AssertionError("Invalid class descriptor");
		if (obfuscatedBinaryTypeNamePattern != null) {
			if (obfuscatedBinaryTypeNamePattern.matcher(type).region(1, type.length() - 1).matches()) return true;
			defaultValue = false;
		}
		if (obfuscatedClassNamePattern != null) {
			int packageEnd = type.lastIndexOf('/');
			int nameStart = (packageEnd < 0) ? 1 : packageEnd + 1;
			String name = type.substring(nameStart, type.length() - 1);
			return obfuscatedClassNamePattern.matcher(name).matches();
		}
		return defaultValue;
	}

}
