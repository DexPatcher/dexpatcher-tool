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

import java.util.Collection;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.Id;

import org.jf.dexlib2.iface.Method;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class DirectMethodSetPatcher extends MethodSetPatcher {

	private boolean staticConstructorFound;

	public DirectMethodSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Implementation

	@Override
	protected String getSetItemLabel() {
		return "direct method";
	}

	@Override
	public Collection<Method> process(Iterable<? extends Method> sourceSet, int sourceSetSizeHint,
			Iterable<? extends Method> patchSet, int patchSetSizeHint) {
		staticConstructorFound = false;
		Collection<Method> methods = super.process(sourceSet, sourceSetSizeHint, patchSet, patchSetSizeHint);
		if (staticConstructorAction != null && !staticConstructorFound) {
			log(ERROR, "static constructor not found");
		}
		return methods;
	}

	@Override
	protected Action getDefaultAction(String patchId, Method patch) throws PatchException {
		if (DexUtils.isStaticConstructor(patchId, patch)) {
			staticConstructorFound = true;
			if (staticConstructorAction != null) return staticConstructorAction;
			if (defaultAction == null) {
				Action action = targetExists(Id.STATIC_CONSTRUCTOR) ? Action.APPEND : Action.ADD;
				log(INFO, "implicit " + action.getLabel() + " of static constructor");
				return action;
			}
		} else if (DexUtils.isDefaultConstructor(patchId, patch) && defaultAction == null &&
				!getContext().isConstructorAutoIgnoreDisabled()) {
			if (DexUtils.hasTrivialConstructorImplementation(patch)) {
				log(INFO, "implicit ignore of trivial default constructor");
				return Action.IGNORE;
			}
			throw new PatchException("no action defined for non-trivial default constructor");
		}
		return super.getDefaultAction(patchId, patch);
	}

	// Wrap

	@Override
	protected void onWrap(String patchId, Method patch, PatcherAnnotation annotation) throws PatchException {
		if (DexUtils.isStaticConstructor(patchId, patch) || DexUtils.isInstanceConstructor(patchId, patch)) {
			throw Action.WRAP.invalidAction();
		}
		super.onWrap(patchId, patch, annotation);
	}

	// Prepend and Append

	@Override
	protected void onSplice(String patchId, Method patch, PatcherAnnotation annotation, Action action)
			throws PatchException {
		if (DexUtils.isInstanceConstructor(patchId, patch)) {
			throw action.invalidAction();
		}
		super.onSplice(patchId, patch, annotation, action);
	}

}
