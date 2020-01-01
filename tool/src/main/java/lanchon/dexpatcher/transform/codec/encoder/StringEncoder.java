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

import java.util.Locale;

public final class StringEncoder {

	private final boolean encodeNonAscii;
	private final boolean encodeNonAsciiLatin1;

	private final boolean disableAsciiLatin1Escapes;
	private final boolean disableCodePointEscapes;

	public StringEncoder(boolean encodeNonAscii, boolean encodeNonAsciiLatin1, boolean disableAsciiLatin1Escapes,
			boolean disableCodePointEscapes) {
		this.encodeNonAscii = encodeNonAscii;
		this.encodeNonAsciiLatin1 = encodeNonAsciiLatin1;
		this.disableAsciiLatin1Escapes = disableAsciiLatin1Escapes;
		this.disableCodePointEscapes = disableCodePointEscapes;
	}

	public StringEncoder(StringEncoderConfiguration flags) {
		this.encodeNonAscii = flags.encodeNonAscii;
		this.encodeNonAsciiLatin1 = flags.encodeNonAsciiLatin1;
		this.disableAsciiLatin1Escapes = flags.disableAsciiLatin1Escapes;
		this.disableCodePointEscapes = flags.disableCodePointEscapes;
	}

	public boolean shouldEncode(int codePoint) {
		if (codePoint < 0) throw new IllegalArgumentException("codePoint");
		return Character.isISOControl(codePoint) ||
				(encodeNonAscii && codePoint > 0x7F) ||
				(encodeNonAsciiLatin1 && codePoint > 0xFF) ||
				!Character.isJavaIdentifierPart(codePoint) ||
				Character.isIdentifierIgnorable(codePoint);
	}

	public boolean shouldEncode(String s) {
		int length = s.length();
		int i = 0;
		while (i < length) {
			int codePoint = s.codePointAt(i);
			if (shouldEncode(codePoint)) return true;
			i += Character.charCount(codePoint);
		}
		return false;
	}

	public StringBuilder encode(StringBuilder sb, int codePoint) {
		if (codePoint <= 0xFF && !disableAsciiLatin1Escapes) {
			sb.append("$a").append(String.format(Locale.ROOT, "%02X", codePoint));
		} else if (Character.isBmpCodePoint(codePoint)) {
			sb.append("$u").append(String.format(Locale.ROOT, "%04X", codePoint));
		} else if (Character.isSupplementaryCodePoint(codePoint)) {
			if (!disableCodePointEscapes) {
				sb.append("$p").append(String.format(Locale.ROOT, "%06X", codePoint));
			} else {
				sb.append("$u").append(String.format(Locale.ROOT, "%04X", (int) Character.highSurrogate(codePoint)));
				sb.append("$u").append(String.format(Locale.ROOT, "%04X", (int) Character.lowSurrogate(codePoint)));
			}
		} else {
			throw new IllegalArgumentException("codePoint");
		}
		return sb;
	}

	public StringBuilder encode(StringBuilder sb, String s) {
		int length = s.length();
		int i = 0;
		while (i < length) {
			int codePoint = s.codePointAt(i);
			if (codePoint == '$') {
				sb.append("$S");
			} else if (codePoint == '_') {
				sb.append("$U");
			} else if (shouldEncode(codePoint)) {
				encode(sb, codePoint);
			} else {
				sb.appendCodePoint(codePoint);
			}
			i += Character.charCount(codePoint);
		}
		return sb;
	}

}
