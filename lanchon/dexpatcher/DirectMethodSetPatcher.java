package lanchon.dexpatcher;

import java.util.Collection;

import lanchon.dexpatcher.annotation.DexTarget;

import org.jf.dexlib2.iface.Method;

import static lanchon.dexpatcher.Logger.Level.*;

public class DirectMethodSetPatcher extends MethodSetPatcher {

	private final Action staticConstructorAction;
	private boolean staticConstructorFound;

	public DirectMethodSetPatcher(Logger logger, String baseLogPrefix, String logMemberType, PatcherAnnotation annotation) {
		super(logger, baseLogPrefix, logMemberType, annotation);
		staticConstructorAction = annotation.getStaticConstructorAction();
	}

	@Override
	public Collection<Method> run(Iterable<? extends Method> sourceSet, int sourceSetSizeHint,
			Iterable<? extends Method> patchSet, int patchSetSizeHint) {
		staticConstructorFound = false;
		Collection<Method> methods = super.run(sourceSet, sourceSetSizeHint, patchSet, patchSetSizeHint);
		if (staticConstructorAction != null && !staticConstructorFound) {
			log(ERROR, "static constructor not found");
		}
		return methods;
	}

	// Handlers

	@Override
	protected PatcherAnnotation getDefaultAnnotation(Method patch) {
		if (staticConstructorAction != null &&
				DexTarget.STATIC_CONSTRUCTOR.equals(patch.getName()) &&
				"()V".equals(Util.getMethodId(patch, ""))) {
			staticConstructorFound = true;
			return new PatcherAnnotation(staticConstructorAction, patch.getAnnotations());
		}
		return super.getDefaultAnnotation(patch);
	}

}
