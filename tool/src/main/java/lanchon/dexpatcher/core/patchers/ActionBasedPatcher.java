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
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.PatchException;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public abstract class ActionBasedPatcher<T, C extends ActionBasedPatcher.ActionContext> extends AbstractPatcher<T> {

	public interface ActionContext {
		Action getAction();
	}

	protected ActionBasedPatcher(Context context) {
		super(context);
	}

	protected ActionBasedPatcher(AbstractPatcher<?> parent) {
		super(parent);
	}

	// Implementation

	@Override
	protected void onPatch(String patchId, T patch) throws PatchException {
		C actionContext = getActionContext(patchId, patch);
		onPrepare(patchId, patch, actionContext);
		Action action = actionContext.getAction();
		if (isLogging(DEBUG)) log(DEBUG, action.getLabel());
		switch (action) {
		case ADD:
			onAdd(patchId, patch, actionContext);
			break;
		case EDIT:
			onEdit(patchId, patch, actionContext);
			break;
		case REPLACE:
			onReplace(patchId, patch, actionContext);
			break;
		case REMOVE:
			onRemove(patchId, patch, actionContext);
			break;
		case IGNORE:
			onIgnore(patchId, patch, actionContext);
			break;
		default:
			throw new AssertionError("Unexpected action");
		}
	}

	// Intermediate Handlers

	protected void onAdd(String patchId, T patch, C actionContext) throws PatchException {
		T patched = onSimpleAdd(patch, actionContext);
		addPatched(patchId, patch, patched);
	}

	protected void onEdit(String patchId, T patch, C actionContext) throws PatchException {
		String targetId = getTargetId(patchId, patch, actionContext);
		boolean inPlaceEdit = patchId.equals(targetId);
		T target = findTarget(targetId, inPlaceEdit);
		T patched = onSimpleEdit(patch, actionContext, target, inPlaceEdit);
		addPatched(patchId, patch, patched);
	}

	protected void onReplace(String patchId, T patch, C actionContext) throws PatchException {
		String targetId = getTargetId(patchId, patch, actionContext);
		T target = findTarget(targetId, false);
		T patched = onSimpleReplace(patch, actionContext, target);
		addPatched(patchId, patch, patched);
	}

	protected void onRemove(String patchId, T patch, C actionContext) throws PatchException {
		String targetId = getTargetId(patchId, patch, actionContext);
		T target = findTarget(targetId, false);
		onSimpleRemove(patch, actionContext, target);
	}

	protected void onIgnore(String patchId, T patch, C actionContext) throws PatchException {}

	// Handlers

	protected abstract C getActionContext(String patchId, T patch) throws PatchException;
	protected void onPrepare(String patchId, T patch, C actionContext) throws PatchException {}
	protected abstract String getTargetId(String patchId, T patch, C actionContext) throws PatchException;

	protected abstract T onSimpleAdd(T patch, C actionContext);
	protected abstract T onSimpleEdit(T patch, C actionContext, T target, boolean inPlaceEdit);
	protected T onSimpleReplace(T patch, C actionContext, T target) { return onSimpleAdd(patch, actionContext); }
	protected void onSimpleRemove(T patch, C actionContext, T target) {}

}
