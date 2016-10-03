/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patchers;

import java.io.File;
import java.util.Set;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Annotatable;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;

public abstract class AnnotatableSetPatcher<T extends Annotatable> extends ActionBasedPatcher<T, PatcherAnnotation> {

	private ClassDef sourceFileClass;
	private String sourceFileName;

	protected AnnotatableSetPatcher(Context context) {
		super(context);
	}

	protected AnnotatableSetPatcher(AnnotatableSetPatcher<?> parent) {
		super(parent);
		sourceFileClass = parent.sourceFileClass;
		sourceFileName = parent.sourceFileName;
	}

	// Debug Info

	protected void setSourceFileClass(ClassDef sourceFileClass) {
		this.sourceFileClass = sourceFileClass;
		sourceFileName = null;
	}

	protected String getSourceFileName() {
		// Parse debug information lazily.
		if (sourceFileClass != null) {
			sourceFileName = sourceFileClass.getSourceFile();
			if (sourceFileName != null && getContext().getSourceCodeRoot() != null) {
				String type = sourceFileClass.getType();
				int i = type.lastIndexOf('/');
				if (i >= 1) {
					String path = type.substring(1, i + 1).replace('/', File.separatorChar);
					sourceFileName = path + sourceFileName;
				}
			}
			sourceFileClass = null;
		}
		return sourceFileName;
	}

	protected int getSourceFileLine() {
		return 0;
	}

	// Logging

	protected void log(Logger.Level level, String message) {
		if (isLogging(level)) {
			String name = getSourceFileName();
			if (name != null) {
				int line = getSourceFileLine();
				String root = getContext().getSourceCodeRoot();
				if (root != null) {
					String ls = System.lineSeparator();
					message += ls + '\t' + root + name + ":" + line;
				} else {
					message = "(" + name + ":" + line + "): " + message;
				}
			}
			super.log(level, message);
		}
	}

	protected final boolean shouldLogTarget(String patchId, String targetId) {
		return !patchId.equals(targetId);
	}

	protected final void extendLogPrefixWithTargetLabel(String targetLabel) {
		extendLogPrefix("target '" + targetLabel + "'");
	}

	// Implementation

	@Override
	protected PatcherAnnotation getContext(String patchId, T patch) throws PatchException {
		Set<? extends Annotation> rawAnnotations = patch.getAnnotations();
		PatcherAnnotation annotation = PatcherAnnotation.parse(getContext(), rawAnnotations);
		if (annotation == null) annotation = new PatcherAnnotation(getDefaultAction(patchId, patch), rawAnnotations);
		return annotation;
	}

	@Override
	protected Action getAction(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		return annotation.getAction();
	}

	// Handlers

	protected abstract String getSetItemLabel();
	protected abstract Action getDefaultAction(String patchId, T patch) throws PatchException;

}
