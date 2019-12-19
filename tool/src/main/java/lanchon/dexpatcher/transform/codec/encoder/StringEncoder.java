/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec.encoder;

import lanchon.dexpatcher.transform.codec.StringCodec;

public final class StringEncoder extends StringCodec {

	protected final String escapedCodeMarker;

	public StringEncoder(String codeMarker) {
		super(codeMarker);
		// TODO: Replace with escaping function when implemented.
		escapedCodeMarker = codeMarker.replace("$", "$S").replace("_", "$U");
	}

	public String encodeString(String string) {
		return string != null ? string.replace(codeMarker, escapedCodeMarker) : null;
	}

}
