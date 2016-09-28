package lanchon.dexpatcher;

import java.util.Set;

import org.jf.dexlib2.iface.Annotatable;
import org.jf.dexlib2.iface.Annotation;

import static lanchon.dexpatcher.Logger.Level.*;

public abstract class AnnotatableSetPatcher<T extends Annotatable> extends AbstractPatcher<T> {

	protected AnnotatableSetPatcher(Logger logger, String baseLogPrefix) {
		super(logger, baseLogPrefix);
	}

	protected AnnotatableSetPatcher(AbstractPatcher<?> parent) {
		super(parent);
	}

	protected final void extendLogPrefix(String patchId, String targetId, PatcherAnnotation annotation) {
		if (!patchId.equals(targetId)) extendLogPrefix(getTargetLogPrefix(targetId, annotation));
	}

	// Implementation

	@Override
	protected void onPatch(String patchId, T patch) throws PatchException {

		Set<? extends Annotation> rawAnnotations = patch.getAnnotations();
		PatcherAnnotation annotation = PatcherAnnotation.parse(rawAnnotations);
		if (annotation == null) annotation = new PatcherAnnotation(getDefaultAction(patchId, patch), rawAnnotations);

		onPrepare(patchId, patch, annotation);

		Action action = annotation.getAction();
		if (isLogging(DEBUG)) log(DEBUG, action.getLabel());
		switch (action) {
		case ADD:
			onAdd(patchId, patch, annotation);
			break;
		case EDIT:
			onEdit(patchId, patch, annotation);
			break;
		case REPLACE:
			onReplace(patchId, patch, annotation);
			break;
		case REMOVE:
			onRemove(patchId, patch, annotation);
			break;
		case IGNORE:
			onIgnore(patchId, patch, annotation);
			break;
		default:
			throw new AssertionError("Unexpected action");
		}

	}

	// Intermediate Handlers

	protected void onAdd(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		T patched = onSimpleAdd(patch, annotation);
		addPatched(patchId, patch, patched);
	}

	protected void onEdit(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		String targetId = getTargetId(patchId, patch, annotation);
		extendLogPrefix(patchId, targetId, annotation);
		boolean renaming = !patchId.equals(targetId);
		T target = findTarget(targetId, !renaming);
		T patched = onSimpleEdit(patch, annotation, target, renaming);
		addPatched(patchId, patch, patched);
	}

	protected void onReplace(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		String targetId = getTargetId(patchId, patch, annotation);
		extendLogPrefix(patchId, targetId, annotation);
		T target = findTarget(targetId, false);
		T patched = onSimpleReplace(patch, annotation, target);
		addPatched(patchId, patch, patched);
	}

	protected void onRemove(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		String targetId = getTargetId(patchId, patch, annotation);
		extendLogPrefix(patchId, targetId, annotation);
		T target = findTarget(targetId, false);
		onSimpleRemove(patch, annotation, target);
	}

	protected void onIgnore(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {}

	// Adapters

	protected abstract String getTargetLogPrefix(String targetId, PatcherAnnotation annotation);

	// Handlers

	protected abstract Action getDefaultAction(String patchId, T patch) throws PatchException;
	protected void onPrepare(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {}

	protected abstract String getTargetId(String patchId, T patch, PatcherAnnotation annotation);

	protected abstract T onSimpleAdd(T patch, PatcherAnnotation annotation);
	protected abstract T onSimpleEdit(T patch, PatcherAnnotation annotation, T target, boolean renaming);
	protected T onSimpleReplace(T patch, PatcherAnnotation annotation, T target) { return onSimpleAdd(patch, annotation); }
	protected void onSimpleRemove(T patch, PatcherAnnotation annotation, T target) {}

}
