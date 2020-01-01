/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec;

public abstract class StringCodec {

	public static final String DEFAULT_CODE_MARKER = "_$$_";

	public static boolean isValidCodeMarker(String marker) {
		return marker.length() >= 2 && marker.startsWith("_") && !marker.startsWith("__") && !marker.endsWith("__");
	}

	protected final String codeMarker;

	public StringCodec(String codeMarker) {
		if (!isValidCodeMarker(codeMarker)) {
			throw new IllegalArgumentException("codeMarker");
		}
		this.codeMarker = codeMarker;
	}

	public String getCodeMarker() {
		return codeMarker;
	}

	public boolean isCodedString(String string) {
		return string.contains(codeMarker);
	}

}
