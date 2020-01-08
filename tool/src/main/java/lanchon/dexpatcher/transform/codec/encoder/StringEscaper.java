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

public final class StringEscaper {

	private final boolean escapeNonAscii;
	private final boolean escapeNonAsciiLatin1;

	private final boolean disableAsciiLatin1Escapes;
	private final boolean disableCodePointEscapes;

	public StringEscaper(StringEscaperConfiguration configuration) {
		this.escapeNonAscii = configuration.escapeNonAscii;
		this.escapeNonAsciiLatin1 = configuration.escapeNonAsciiLatin1;
		this.disableAsciiLatin1Escapes = configuration.disableAsciiLatin1Escapes;
		this.disableCodePointEscapes = configuration.disableCodePointEscapes;
	}

	public boolean shouldEscape(int codePoint) {
		if (codePoint < 0) throw new IllegalArgumentException("codePoint");
		return Character.isISOControl(codePoint) ||
				(escapeNonAscii && codePoint > 0x7F) ||
				(escapeNonAsciiLatin1 && codePoint > 0xFF) ||
				!Character.isJavaIdentifierPart(codePoint) ||
				Character.isIdentifierIgnorable(codePoint);
	}

	public boolean shouldEscape(String s) {
		int length = s.length();
		int i = 0;
		while (i < length) {
			int codePoint = s.codePointAt(i);
			if (shouldEscape(codePoint)) return true;
			i += Character.charCount(codePoint);
		}
		return false;
	}

	public StringBuilder escape(StringBuilder sb, int codePoint) {
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

	public StringBuilder escape(StringBuilder sb, String s) {
		int length = s.length();
		int i = 0;
		while (i < length) {
			int codePoint = s.codePointAt(i);
			if (codePoint == '$') {
				sb.append("$S");
			} else if (codePoint == '_') {
				sb.append("$U");
			} else if (shouldEscape(codePoint)) {
				escape(sb, codePoint);
			} else {
				sb.appendCodePoint(codePoint);
			}
			i += Character.charCount(codePoint);
		}
		return sb;
	}

}
