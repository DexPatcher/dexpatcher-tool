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

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Member;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class MemberSetPatcher<T extends Member> extends AnnotatableSetPatcher<T> {

	private final Action defaultAction;
	protected final Action staticConstructorAction;
	protected final Action resolvedStaticConstructorAction;

	public MemberSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent);
		defaultAction = annotation.getDefaultAction();
		staticConstructorAction = annotation.getStaticConstructorAction();
		resolvedStaticConstructorAction = (staticConstructorAction != null ? staticConstructorAction : defaultAction);
	}

	// Implementation

	@Override
	protected Action getDefaultAction(String patchId, T patch) {
		if (defaultAction != null) {
			log(INFO, "default " + defaultAction.getLabel());
			return defaultAction;
		} else {
			log(ERROR, "no default action defined");
			return Action.IGNORE;
		}
	}

	@Override
	protected void onPrepare(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		if (annotation.getTargetClass() != null) PatcherAnnotation.throwInvalidElement(Marker.ELEM_TARGET_CLASS);
		if (annotation.getStaticConstructorAction() != null) PatcherAnnotation.throwInvalidElement(Marker.ELEM_STATIC_CONSTRUCTOR_ACTION);
		if (annotation.getDefaultAction() != null) PatcherAnnotation.throwInvalidElement(Marker.ELEM_DEFAULT_ACTION);
		if (annotation.getOnlyEditMembers()) PatcherAnnotation.throwInvalidElement(Marker.ELEM_ONLY_EDIT_MEMBERS);
		if (annotation.getRecursive()) PatcherAnnotation.throwInvalidElement(Marker.ELEM_RECURSIVE);
	}

	@Override
	protected T onSimpleEdit(T patch, PatcherAnnotation annotation, T target, boolean inPlaceEdit) {
		int oldFlags = target.getAccessFlags();
		int newFlags = patch.getAccessFlags();
		if (!inPlaceEdit) {
			String item = "renamed " + getSetItemShortLabel();
			if (isLogging(WARN)) logAccessFlags(WARN, oldFlags, newFlags,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, ENUM, DECLARED_SYNCHRONIZED }, item);
			if (isLogging(INFO)) logAccessFlags(INFO, oldFlags, newFlags,
					new AccessFlags[] { FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, STRICTFP }, item);
			if (isLogging(DEBUG)) logAccessFlags(DEBUG, oldFlags, newFlags,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, BRIDGE, SYNTHETIC, CONSTRUCTOR }, item);
		} else {
			String item = "edited " + getSetItemShortLabel();
			if (isLogging(WARN)) logAccessFlags(WARN, oldFlags, newFlags,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, ENUM, CONSTRUCTOR, DECLARED_SYNCHRONIZED }, item);
			if (isLogging(INFO)) logAccessFlags(INFO, oldFlags, newFlags,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, STRICTFP }, item);
			if (isLogging(DEBUG)) logAccessFlags(DEBUG, oldFlags, newFlags,
					new AccessFlags[] { BRIDGE, SYNTHETIC }, item);
		}
		return patch;
	}

	@Override
	protected void onEffectiveReplacement(String id, T patch, T patched, T original, boolean inPlaceEdit) {
		// Avoid duplicated messages if not renaming.
		if (!inPlaceEdit) {
			int oldFlags = original.getAccessFlags();
			int newFlags = patched.getAccessFlags();
			String item = "replaced " + getSetItemShortLabel();
			if (isLogging(WARN)) logAccessFlags(WARN, oldFlags, newFlags,
					new AccessFlags[] { STATIC,  ABSTRACT, ENUM, CONSTRUCTOR }, item);
			if (isLogging(INFO)) logAccessFlags(INFO, oldFlags, newFlags,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, FINAL, VOLATILE, TRANSIENT, VARARGS }, item);
			if (isLogging(DEBUG)) logAccessFlags(DEBUG, oldFlags, newFlags,
					new AccessFlags[] { SYNCHRONIZED, BRIDGE, NATIVE, STRICTFP, SYNTHETIC, DECLARED_SYNCHRONIZED }, item);
		}
	}

	// Handlers

	protected abstract String getSetItemShortLabel();

}
