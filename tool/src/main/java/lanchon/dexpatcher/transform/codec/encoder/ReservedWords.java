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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ReservedWords {

	// Based on the Java 13 JLS.

	private static final Set<String> RESERVED_WORDS;

	static {
		String[] keywords = new String[] {
				"abstract",   "continue",   "for",          "new",         "switch",
				"assert",     "default",    "if",           "package",     "synchronized",
				"boolean",    "do",         "goto",         "private",     "this",
				"break",      "double",     "implements",   "protected",   "throw",
				"byte",       "else",       "import",       "public",      "throws",
				"case",       "enum",       "instanceof",   "return",      "transient",
				"catch",      "extends",    "int",          "short",       "try",
				"char",       "final",      "interface",    "static",      "void",
				"class",      "finally",    "long",         "strictfp",    "volatile",
				"const",      "float",      "native",       "super",       "while",
				"_"
		};
		String[] literals = new String[] {
				"false",      "true",       "null"
		};
		String[] reservedTypeNames = new String[] {
				"var"
		};
		String[] restrictedKeywords = new String[] {
				"exports",    "open",       "provides",     "to",          "uses",
				"module",     "opens",      "requires",     "transitive",  "with"
		};
		RESERVED_WORDS = new HashSet<>();
		RESERVED_WORDS.addAll(Arrays.asList(keywords));
		RESERVED_WORDS.addAll(Arrays.asList(literals));
		RESERVED_WORDS.addAll(Arrays.asList(reservedTypeNames));
		//RESERVED_WORDS.addAll(Arrays.asList(restrictedKeywords));
	}

	public static boolean isReserved(String word) {
		return RESERVED_WORDS.contains(word);
	}

	private ReservedWords() {}

}
