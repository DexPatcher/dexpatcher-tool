/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patcher;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Member;

import static lanchon.dexpatcher.core.PatcherAnnotation.*;
import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public abstract class MemberSetPatcher<T extends Member> extends AnnotatableSetPatcher<T> {

	protected final Action explicitStaticConstructorAction;
	protected final Action resolvedStaticConstructorAction;     // not null
	protected final Action explicitDefaultAction;
	protected final Action resolvedDefaultAction;               // not null

	public MemberSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent);
		Action defaultAction = Action.NONE;
		Action sca = annotation.getStaticConstructorAction();
		Action da = annotation.getDefaultAction();
		explicitStaticConstructorAction = sca;
		explicitDefaultAction = da;
		resolvedDefaultAction = (da != null) ? da : defaultAction;
		resolvedStaticConstructorAction = (sca != null) ? sca : resolvedDefaultAction;
	}

	// Implementation

	@Override
	protected int getAccessFlags(T item) {
		return item.getAccessFlags();
	}

	@Override
	protected Action getDefaultAction(String patchId, T patch) throws PatchException {
		if (resolvedDefaultAction == Action.NONE) throw new PatchException("no action defined");
		log(INFO, "default " + resolvedDefaultAction.getLabel());
		return resolvedDefaultAction;
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
