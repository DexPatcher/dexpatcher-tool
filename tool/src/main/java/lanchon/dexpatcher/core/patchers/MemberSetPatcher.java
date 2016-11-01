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

import org.jf.dexlib2.iface.Member;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

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
	protected int getAccessFlags(T item) {
		return item.getAccessFlags();
	}

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

}
