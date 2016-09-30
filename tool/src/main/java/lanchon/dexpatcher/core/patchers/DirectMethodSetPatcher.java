package lanchon.dexpatcher.core.patchers;

import java.util.Collection;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Method;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class DirectMethodSetPatcher extends MethodSetPatcher {

	private boolean staticConstructorFound;

	public DirectMethodSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Adapters

	@Override
	protected void setupLogPrefix(String id, Method patch, Method patched) {
		setupLogPrefix("direct method '" + id + "'");
		setSourceFileMethod(patch);
	}

	// Implementation

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
	protected Action getDefaultAction(String patchId, Method patch) {
		if (Marker.SIGN_STATIC_CONSTRUCTOR.equals(patchId)) {
			staticConstructorFound = true;
			if (staticConstructorAction != null) return staticConstructorAction;
		}
		return super.getDefaultAction(patchId, patch);
	}

}
