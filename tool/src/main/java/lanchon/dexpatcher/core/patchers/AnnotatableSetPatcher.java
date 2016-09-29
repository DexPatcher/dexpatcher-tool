package lanchon.dexpatcher.core.patchers;

import java.util.Set;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Annotatable;
import org.jf.dexlib2.iface.Annotation;

public abstract class AnnotatableSetPatcher<T extends Annotatable> extends ActionBasedPatcher<T, PatcherAnnotation> {

	protected AnnotatableSetPatcher(Logger logger, String baseLogPrefix) {
		super(logger, baseLogPrefix);
	}

	protected AnnotatableSetPatcher(AbstractPatcher<?> parent) {
		super(parent);
	}

	protected final boolean shouldLogTarget(String patchId, String targetId) {
		return !patchId.equals(targetId);
	}

	// Implementation

	@Override
	protected PatcherAnnotation getContext(String patchId, T patch) throws PatchException {
		Set<? extends Annotation> rawAnnotations = patch.getAnnotations();
		PatcherAnnotation annotation = PatcherAnnotation.parse(rawAnnotations);
		if (annotation == null) annotation = new PatcherAnnotation(getDefaultAction(patchId, patch), rawAnnotations);
		return annotation;
	}

	@Override
	protected Action getAction(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		return annotation.getAction();
	}

	// Handlers

	protected abstract Action getDefaultAction(String patchId, T patch) throws PatchException;

}
