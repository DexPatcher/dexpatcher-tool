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

import java.util.Locale;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.transform.DexTransform;
import lanchon.dexpatcher.transform.TransformLogger;
import lanchon.dexpatcher.transform.mapper.map.DexMap;

import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.rewriter.RewriterModule;

public class DexMapper extends DexTransform implements DexMap {

	protected final DexTransform wrappedTransform;
	protected final DexMap dexMap;
	protected final String message;
	protected final String annotationPackage;
	protected final Logger.Level infoLevel;

	public DexMapper(DexTransform wrappedTransform, DexMap dexMap, String annotationPackage, TransformLogger logger,
			String logPrefix, Logger.Level infoLevel) {
		this(wrappedTransform, dexMap, "mapped to '%s'", annotationPackage, logger, logPrefix, infoLevel);
	}

	public DexMapper(DexTransform wrappedTransform, DexMap dexMap, boolean isInverseMap, String annotationPackage,
			TransformLogger logger, String logPrefix, Logger.Level infoLevel) {
		this(wrappedTransform, dexMap, isInverseMap ? "unmapped to '%s'" : "mapped to '%s'", annotationPackage,
				logger, logPrefix, infoLevel);
	}

	public DexMapper(DexTransform wrappedTransform, DexMap dexMap, String message, String annotationPackage,
			TransformLogger logger, String logPrefix, Logger.Level infoLevel) {
		super(logger, logPrefix);
		this.wrappedTransform = wrappedTransform;
		this.dexMap = dexMap;
		this.message = message;
		this.annotationPackage = annotationPackage;
		this.infoLevel = infoLevel;
	}

	@Override
	public RewriterModule getRewriterModule() {
		RewriterModule wrappedModule = (wrappedTransform) != null ? wrappedTransform.getRewriterModule() :
				new RewriterModule();
		return PatchRewriterModule.of(new DexMapperModule(wrappedModule, this), annotationPackage);
	}

	@Override
	protected String getTransformedDefiningClass(String definingClass) {
		return dexMap.getClassMapping(definingClass);
	}

	@Override
	public String getClassMapping(String descriptor) {
		String mapping = dexMap.getClassMapping(descriptor);
		if (mapping != null && logger.isLogging(infoLevel)) {
			StringBuilder sb = getMessageHeaderForClass(descriptor);
			sb.append(String.format(Locale.ROOT, message, Label.fromClassDescriptor(mapping)));
			logger.log(infoLevel, sb.toString());
		}
		return mapping;
	}

	@Override
	public String getFieldMapping(FieldReference field) {
		String mapping = dexMap.getFieldMapping(field);
		if (mapping != null && logger.isLogging(infoLevel)) {
			StringBuilder sb = getMessageHeaderForField(field);
			sb.append(String.format(Locale.ROOT, message, Label.ofTargetMember(mapping)));
			logger.log(infoLevel, sb.toString());
		}
		return mapping;
	}

	@Override
	public String getMethodMapping(MethodReference method) {
		String mapping = dexMap.getMethodMapping(method);
		if (mapping != null && logger.isLogging(infoLevel)) {
			StringBuilder sb = getMessageHeaderForMethod(method);
			sb.append(String.format(Locale.ROOT, message, Label.ofTargetMember(mapping)));
			logger.log(infoLevel, sb.toString());
		}
		return mapping;
	}

}
