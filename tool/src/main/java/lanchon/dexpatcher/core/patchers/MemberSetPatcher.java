/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
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

import static lanchon.dexpatcher.core.PatcherAnnotation.*;
import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public abstract class MemberSetPatcher<T extends Member> extends AnnotatableSetPatcher<T> {

	protected final Action staticConstructorAction;
	protected final Action defaultAction;
	protected final Action resolvedStaticConstructorAction;

	public MemberSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent);
		staticConstructorAction = annotation.getStaticConstructorAction();
		defaultAction = annotation.getDefaultAction();
		resolvedStaticConstructorAction = (staticConstructorAction != null ? staticConstructorAction : defaultAction);
	}

	// Implementation

	@Override
	protected int getAccessFlags(T item) {
		return item.getAccessFlags();
	}

	@Override
	protected Action getDefaultAction(String patchId, T patch) throws PatchException {
		if (defaultAction == null) throw new PatchException("no action defined");
		log(INFO, "default " + defaultAction.getLabel());
		return defaultAction;
	}

	@Override
	protected void onPrepare(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		if (annotation.getTargetClass() != null) throw invalidElement(Marker.ELEM_TARGET_CLASS);
		if (annotation.getStaticConstructorAction() != null) throw invalidElement(Marker.ELEM_STATIC_CONSTRUCTOR_ACTION);
		if (annotation.getDefaultAction() != null) throw invalidElement(Marker.ELEM_DEFAULT_ACTION);
		if (annotation.getContentOnly()) throw invalidElement(Marker.ELEM_CONTENT_ONLY);
		if (annotation.getRecursive()) throw invalidElement(Marker.ELEM_RECURSIVE);
	}

}
