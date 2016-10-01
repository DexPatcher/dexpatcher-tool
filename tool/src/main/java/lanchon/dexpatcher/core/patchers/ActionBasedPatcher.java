package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.PatchException;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public abstract class ActionBasedPatcher<T, C> extends AbstractPatcher<T> {

	protected ActionBasedPatcher(Context context) {
		super(context);
	}

	protected ActionBasedPatcher(AbstractPatcher<?> parent) {
		super(parent);
	}

	// Implementation

	@Override
	protected void onPatch(String patchId, T patch) throws PatchException {
		C context = getContext(patchId, patch);
		onPrepare(patchId, patch, context);
		Action action = getAction(patchId, patch, context);
		if (isLogging(DEBUG)) log(DEBUG, action.getLabel());
		switch (action) {
		case ADD:
			onAdd(patchId, patch, context);
			break;
		case EDIT:
			onEdit(patchId, patch, context);
			break;
		case REPLACE:
			onReplace(patchId, patch, context);
			break;
		case REMOVE:
			onRemove(patchId, patch, context);
			break;
		case IGNORE:
			onIgnore(patchId, patch, context);
			break;
		default:
			throw new AssertionError("Unexpected action");
		}
	}

	// Intermediate Handlers

	protected void onAdd(String patchId, T patch, C context) throws PatchException {
		T patched = onSimpleAdd(patch, context);
		addPatched(patchId, patch, patched);
	}

	protected void onEdit(String patchId, T patch, C context) throws PatchException {
		String targetId = getTargetId(patchId, patch, context);
		boolean inPlaceEdit = patchId.equals(targetId);
		T target = findTarget(targetId, inPlaceEdit);
		T patched = onSimpleEdit(patch, context, target, inPlaceEdit);
		addPatched(patchId, patch, patched);
	}

	protected void onReplace(String patchId, T patch, C context) throws PatchException {
		String targetId = getTargetId(patchId, patch, context);
		T target = findTarget(targetId, false);
		T patched = onSimpleReplace(patch, context, target);
		addPatched(patchId, patch, patched);
	}

	protected void onRemove(String patchId, T patch, C context) throws PatchException {
		String targetId = getTargetId(patchId, patch, context);
		T target = findTarget(targetId, false);
		onSimpleRemove(patch, context, target);
	}

	protected void onIgnore(String patchId, T patch, C context) throws PatchException {}

	// Handlers

	protected abstract C getContext(String patchId, T patch) throws PatchException;
	protected void onPrepare(String patchId, T patch, C context) throws PatchException {}
	protected abstract Action getAction(String patchId, T patch, C context) throws PatchException;
	protected abstract String getTargetId(String patchId, T patch, C context) throws PatchException;

	protected abstract T onSimpleAdd(T patch, C context);
	protected abstract T onSimpleEdit(T patch, C context, T target, boolean inPlaceEdit);
	protected T onSimpleReplace(T patch, C context, T target) { return onSimpleAdd(patch, context); }
	protected void onSimpleRemove(T patch, C context, T target) {}

}
