/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper;

import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.ElementalTypeRewriter;
import lanchon.dexpatcher.transform.mapper.map.DexMap;
import lanchon.dexpatcher.transform.util.wrapper.WrapperFieldReference;
import lanchon.dexpatcher.transform.util.wrapper.WrapperMethodReference;
import lanchon.dexpatcher.transform.util.wrapper.WrapperRewriterModule;

import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

public class DexMapperModule extends WrapperRewriterModule<RewriterModule> {

	protected final DexMap dexMap;

	public DexMapperModule(DexMap dexMap) {
		this(dexMap, new RewriterModule());
	}

	public DexMapperModule(DexMap dexMap, RewriterModule wrappedModule) {
		super(wrappedModule);
		this.dexMap = dexMap;
	}

	@Override
	public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
		final Rewriter<String> wrappedTypeRewriter = wrappedModule.getTypeRewriter(rewriters);
		return new ElementalTypeRewriter() {
			@Override
			public String rewriteElementalType(String type) {
				if (DexUtils.isClassDescriptor(type)) {
					String mapping = dexMap.getClassMapping(type);
					if (mapping != null) return mapping;
				}
				// WARNING: This routes the elemental type to the wrapped rewriter.
				return wrappedTypeRewriter.rewrite(type);
			}
		};
	}

	@Override
	public Rewriter<FieldReference> getFieldReferenceRewriter(Rewriters rewriters) {
		final Rewriter<FieldReference> wrappedFieldReferenceRewriter =
				wrappedModule.getFieldReferenceRewriter(rewriters);
		return new Rewriter<FieldReference>() {
			@Override
			public FieldReference rewrite(FieldReference field) {
				FieldReference rewrittenField = wrappedFieldReferenceRewriter.rewrite(field);
				final String mapping = dexMap.getFieldMapping(field);
				if (mapping != null) {
					return new WrapperFieldReference(rewrittenField) {
						@Override
						public String getName() {
							return mapping;
						}
					};
				}
				return rewrittenField;
			}
		};
	}

	@Override
	public Rewriter<MethodReference> getMethodReferenceRewriter(Rewriters rewriters) {
		final Rewriter<MethodReference> wrappedMethodReferenceRewriter =
				wrappedModule.getMethodReferenceRewriter(rewriters);
		return new Rewriter<MethodReference>() {
			@Override
			public MethodReference rewrite(MethodReference method) {
				MethodReference rewrittenMethod = wrappedMethodReferenceRewriter.rewrite(method);
				final String mapping = dexMap.getMethodMapping(method);
				if (mapping != null) {
					return new WrapperMethodReference(rewrittenMethod) {
						@Override
						public String getName() {
							return mapping;
						}
					};
				}
				return rewrittenMethod;
			}
		};
	}

}
