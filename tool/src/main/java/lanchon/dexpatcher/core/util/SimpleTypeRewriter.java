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

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

public class SimpleTypeRewriter {

	public static RewriterModule getModule(final String fromDescriptor, final String toDescriptor) {
		return new RewriterModule() {
			@Override
			public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
				return new ElementalTypeRewriter() {
					@Override
					public String rewriteElementalType(String elementalType) {
						return elementalType.equals(fromDescriptor) ? toDescriptor : elementalType;
					}
				};
			}
		};
	}

	public static ClassDef renameClass(ClassDef classDef, String toClassDescriptor) {
		RewriterModule rewriterModule = getModule(classDef.getType(), toClassDescriptor);
		return new DexRewriter(rewriterModule).getClassDefRewriter().rewrite(classDef);
	}

}
