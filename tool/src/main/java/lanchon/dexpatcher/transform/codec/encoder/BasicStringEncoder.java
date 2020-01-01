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

import lanchon.dexpatcher.transform.codec.StringCodec;

public final class BasicStringEncoder extends StringCodec {

	protected final String encodedCodeMarker;

	public BasicStringEncoder(String codeMarker) {
		super(codeMarker);
		encodedCodeMarker = "_" + codeMarker + "$U__" + codeMarker.substring(1);
	}

	public BasicStringEncoder(String codeMarker, String encodedCodeMarker) {
		super(codeMarker);
		this.encodedCodeMarker = encodedCodeMarker;
	}

	public String encodeString(String string) {
		return string != null ? string.replace(codeMarker, encodedCodeMarker) : null;
	}

}
