/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform;

import org.jf.dexlib2.rewriter.RewriterModule;

public abstract class WrapperDexTransform implements DexTransform {

	protected final DexTransform wrappedTransform;

	public WrapperDexTransform(DexTransform wrappedTransform) {
		this.wrappedTransform = wrappedTransform;
	}

	@Override
	public RewriterModule getRewriterModule() {
		RewriterModule wrappedModule = (wrappedTransform != null) ? wrappedTransform.getRewriterModule() :
				new RewriterModule();
		return getRewriterModule(wrappedModule);
	}

	public abstract RewriterModule getRewriterModule(RewriterModule wrappedModule);

}
