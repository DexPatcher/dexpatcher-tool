/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.util;

import org.jf.dexlib2.rewriter.Rewriter;

public abstract class ElementalTypeRewriter implements Rewriter<String> {

	@Override
	public String rewrite(String value) {
		int length = value.length();
		if (length == 0 || value.charAt(0) != '[') return rewriteElementalType(value);
		int start = 1;
		while (start < length && value.charAt(start) == '[') start++;
		String elementalType = value.substring(start);
		String rewrittenElementalType = rewriteElementalType(elementalType);
		if (rewrittenElementalType.equals(elementalType)) return value;
		StringBuilder sb = new StringBuilder(start + rewrittenElementalType.length());
		sb.append(value, 0, start).append(rewrittenElementalType);
		return sb.toString();
	}

	public abstract String rewriteElementalType(String elementalType);

}
