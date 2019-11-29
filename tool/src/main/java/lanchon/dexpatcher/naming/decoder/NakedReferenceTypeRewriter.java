/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.naming.decoder;

import org.jf.dexlib2.rewriter.TypeRewriter;

// Naked reference type '<type>' (eg: 'java/lang/String') corresponds to type descriptor 'L<type>;'.
public abstract class NakedReferenceTypeRewriter extends TypeRewriter {

	@Override
	public String rewrite(String value) {
		int end = value.length() - 1;
		if (end < 0 || value.charAt(end) != ';') return value;
		int start = 0;
		char c;
		while ((c = value.charAt(start)) == '[') start++;
		if (c != 'L') return value;
		start++;
		String nakedType = value.substring(start, end);
		String rewrittenNakedType = rewriteNakedReferenceType(nakedType);
		if (rewrittenNakedType.equals(nakedType)) return value;
		StringBuilder sb = new StringBuilder(start + rewrittenNakedType.length() + 1);
		sb.append(value, 0, start).append(rewrittenNakedType).append(';');
		return sb.toString();
	}

	public abstract String rewriteNakedReferenceType(String nakedReferenceType);

}
