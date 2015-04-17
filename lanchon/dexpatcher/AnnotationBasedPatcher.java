package lanchon.dexpatcher;

import java.util.Set;

import org.jf.dexlib2.iface.Annotation;

import static lanchon.dexpatcher.Logger.Level.*;

public abstract class AnnotationBasedPatcher<T> extends AbstractPatcher<T>{

	protected AnnotationBasedPatcher(Logger logger, String baseLogPrefix) {
		super(logger, baseLogPrefix);
	}

	protected AnnotationBasedPatcher(AbstractPatcher<?> parent) {
		super(parent);
	}

	protected final T setupTarget(String patchId, String targetId, PatcherAnnotation annotation) throws PatchException {
		if (!targetId.equals(patchId)) {
			extendLogPrefix(getTargetLogPrefix(targetId, annotation));
		}
		return findTarget(targetId);
	}

	// Adapters

	// TODO:
	// When this commit ships: https://code.google.com/p/smali/issues/detail?id=237
	// Eliminate: protected abstract Set<? extends Annotation> getAnnotations(T patch);

	protected abstract Set<? extends Annotation> getAnnotations(T patch);
	protected abstract String getTargetLogPrefix(String targetId, PatcherAnnotation annotation);

	// Handlers

	@Override
	protected void onPatch(String patchId, T patch) throws PatchException {

		Set<? extends Annotation> rawAnnotations = getAnnotations(patch);
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

	// New Handlers

	protected abstract Action getDefaultAction(String patchId, T patch) throws PatchException;
	protected void onPrepare(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {}

	protected abstract void onAdd(String patchId, T patch, PatcherAnnotation annotation) throws PatchException;
	protected abstract void onEdit(String patchId, T patch, PatcherAnnotation annotation) throws PatchException;
	protected abstract void onReplace(String patchId, T patch, PatcherAnnotation annotation) throws PatchException;
	protected abstract void onRemove(String patchId, T patch, PatcherAnnotation annotation) throws PatchException;
	protected void onIgnore(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {}

}
