/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper;

import lanchon.dexpatcher.transform.DexTransform;
import lanchon.dexpatcher.transform.WrapperDexTransform;
import lanchon.dexpatcher.transform.mapper.map.DexMap;

import org.jf.dexlib2.rewriter.RewriterModule;

public class DexMapper extends WrapperDexTransform {

	protected final DexMap dexMap;
	protected final String annotationPackage;

	public DexMapper(DexTransform wrappedTransform, DexMap dexMap, String annotationPackage) {
		super(wrappedTransform);
		this.dexMap = dexMap;
		this.annotationPackage = annotationPackage;
	}

	@Override
	public RewriterModule getRewriterModule(RewriterModule wrappedModule) {
		return PatchRewriterModule.of(new DexMapperModule(wrappedModule, dexMap), annotationPackage);
	}

}
