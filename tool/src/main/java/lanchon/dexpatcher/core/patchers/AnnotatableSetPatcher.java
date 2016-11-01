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
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.logger.Logger;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotatable;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

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

	// Access Flags Logging

	private void logAccessFlags(String item, int oldFlags, int newFlags, boolean keepInterface,
			boolean keepImplementation) {
		new AccessFlagLogger(item, oldFlags, newFlags).allFlags(keepInterface, keepImplementation);
	}

	@Override
	protected T onSimpleEdit(T patch, PatcherAnnotation annotation, T target, boolean inPlaceEdit) {
		int oldFlags = getAccessFlags(target);
		int newFlags = getAccessFlags(patch);
		if (inPlaceEdit) {
			String item = "edited " + getSetItemShortLabel();
			logAccessFlags(item, oldFlags, newFlags, true, true);
		} else {
			String item = "renamed " + getSetItemShortLabel();
			logAccessFlags(item, oldFlags, newFlags, false, true);
		}
		return patch;
	}

	@Override
	protected void onEffectiveReplacement(String id, T patch, T patched, T original, boolean inPlaceEdit) {
		// Avoid duplicated messages if not renaming.
		if (!inPlaceEdit) {
			int oldFlags = getAccessFlags(original);
			int newFlags = getAccessFlags(patched);
			String item = "replaced " + getSetItemShortLabel();
			logAccessFlags(item, oldFlags, newFlags, true, false);
		}
	}

	public final class AccessFlagLogger {

		private String item;
		private int oldFlags;
		private int newFlags;

		private AccessFlagLogger(String item, int oldFlags, int newFlags) {
			this.item = item;
			this.oldFlags = oldFlags;
			this.newFlags = newFlags;
		}

		private void log(Logger.Level level, AccessFlags flag, String message) {
			AnnotatableSetPatcher.this.log(level, "'" + flag + "' modifier " + message);
		}

		private void flag(AccessFlags flag, Logger.Level level) {
			flag(flag, level, level);
		}

		private void flag(AccessFlags flag, Logger.Level added, Logger.Level removed) {
			boolean isSet = flag.isSet(newFlags);
			if (isSet != flag.isSet(oldFlags)) {
				Logger.Level level = isSet ? added : removed;
				if (isLogging(level)) log(level, flag, (isSet ? "added to " : "removed from ") + item);
			}
		}

		private void scopeFlags(Logger.Level decreased, Logger.Level notDecreased) {
			AccessFlags newScope = getScope(newFlags);
			AccessFlags oldScope = getScope(oldFlags);
			if (oldScope != null && newScope != null) {
				if (oldScope != newScope) {
					Logger.Level level = (oldScope == PRIVATE || newScope == PUBLIC) ? notDecreased : decreased;
					if (isLogging(level)) log(level, oldScope, "changed to '" + newScope + "' in " + item);
				}
			} else {
				flag(PUBLIC, notDecreased, decreased);
				flag(PRIVATE, decreased, notDecreased);
				flag(PROTECTED, decreased, decreased);
			}
		}

		private AccessFlags getScope(int flags) {
			boolean isPublic = PUBLIC.isSet(flags);
			boolean isPrivate = PRIVATE.isSet(flags);
			boolean isProtected = PROTECTED.isSet(flags);
			int n = (isPublic ? 1 : 0) + (isPrivate ? 1 : 0) + (isProtected ? 1 : 0);
			if (n != 1) return null;
			if (isPublic) return PUBLIC;
			if (isPrivate) return PRIVATE;
			if (isProtected) return PROTECTED;
			throw new AssertionError("Unexpected scope");
		}

		public void allFlags(boolean keepInterface, boolean keepImplementation) {

			// Interface Dependent
			scopeFlags(keepInterface ? WARN : DEBUG, keepInterface ? INFO : DEBUG);
			flag(FINAL, (keepInterface && !PRIVATE.isSet(oldFlags)) ? WARN : INFO, INFO);
			flag(VOLATILE, INFO, keepInterface ? WARN : INFO);
			flag(TRANSIENT, keepInterface ? WARN : INFO);
			flag(VARARGS, INFO);
			flag(CONSTRUCTOR, keepInterface ? WARN : DEBUG);

			// Interface And Implementation Dependent
			flag(STATIC, WARN);
			flag(INTERFACE, WARN);
			flag(ANNOTATION, WARN);
			flag(ENUM, WARN);

			// Implementation Dependent
			flag(SYNCHRONIZED, keepImplementation ? WARN : DEBUG);
			flag(NATIVE, keepImplementation ? WARN : DEBUG);
			flag(ABSTRACT, WARN, keepImplementation ? WARN : INFO);
			flag(STRICTFP, keepImplementation ? WARN : DEBUG);
			flag(DECLARED_SYNCHRONIZED, keepImplementation ? INFO : DEBUG);

			// Extra
			flag(BRIDGE, DEBUG);
			flag(SYNTHETIC, DEBUG);

		}

	}

	// Handlers

	protected abstract String getSetItemLabel();
	protected String getSetItemShortLabel() { return getSetItemLabel(); }
	protected abstract int getAccessFlags(T item);
	protected abstract Action getDefaultAction(String patchId, T patch) throws PatchException;

}
