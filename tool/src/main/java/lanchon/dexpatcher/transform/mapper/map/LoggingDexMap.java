/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper.map;

import java.util.Locale;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.transform.MemberLogger;
import lanchon.dexpatcher.transform.TransformLogger;

import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class LoggingDexMap extends MemberLogger implements DexMap {

	protected final DexMap wrappedDexMap;
	protected final String message;
	protected final Logger.Level infoLevel;

	public LoggingDexMap(DexMap wrappedDexMap, TransformLogger logger, String logPrefix, Logger.Level infoLevel) {
		this(wrappedDexMap, "mapped to '%s'", logger, logPrefix, infoLevel);
	}

	public LoggingDexMap(DexMap wrappedDexMap, boolean isInverseMap, TransformLogger logger, String logPrefix,
			Logger.Level infoLevel) {
		this(wrappedDexMap, isInverseMap ? "unmapped to '%s'" : "mapped to '%s'", logger, logPrefix, infoLevel);
	}

	public LoggingDexMap(DexMap wrappedDexMap, String message, TransformLogger logger, String logPrefix,
			Logger.Level infoLevel) {
		super(logger, logPrefix);
		this.wrappedDexMap = wrappedDexMap;
		this.message = message;
		this.infoLevel = infoLevel;
	}

	@Override
	protected String getTransformedDefiningClass(String definingClass) {
		return wrappedDexMap.getClassMapping(definingClass);
	}

	@Override
	public String getClassMapping(String descriptor) {
		String mapping = wrappedDexMap.getClassMapping(descriptor);
		if (mapping != null && logger.isLogging(infoLevel)) {
			StringBuilder sb = getMessageHeaderForClass(descriptor);
			sb.append(String.format(Locale.ROOT, message, Label.fromClassDescriptor(mapping)));
			logger.log(infoLevel, sb.toString());
		}
		return mapping;
	}

	@Override
	public String getFieldMapping(FieldReference field) {
		String mapping = wrappedDexMap.getFieldMapping(field);
		if (mapping != null && logger.isLogging(infoLevel)) {
			StringBuilder sb = getMessageHeaderForField(field);
			sb.append(String.format(Locale.ROOT, message, Label.ofTargetMember(mapping)));
			logger.log(infoLevel, sb.toString());
		}
		return mapping;
	}

	@Override
	public String getMethodMapping(MethodReference method) {
		String mapping = wrappedDexMap.getMethodMapping(method);
		if (mapping != null && logger.isLogging(infoLevel)) {
			StringBuilder sb = getMessageHeaderForMethod(method);
			sb.append(String.format(Locale.ROOT, message, Label.ofTargetMember(mapping)));
			logger.log(infoLevel, sb.toString());
		}
		return mapping;
	}

}
