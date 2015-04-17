package lanchon.dexpatcher;

import java.util.Collection;

import org.jf.dexlib2.iface.Method;

import static lanchon.dexpatcher.Logger.Level.*;

public class DirectMethodSetPatcher extends MethodSetPatcher {

	private boolean staticConstructorFound;

	public DirectMethodSetPatcher(DexPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent, logMemberType, annotation);
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

	// Handlers

	@Override
	protected Action getDefaultAction(Method patch) {
		if ("<clinit>".equals(patch.getName()) &&		// performance optimization
				"<clinit>()V".equals(getId(patch))) {
			staticConstructorFound = true;
			if (staticConstructorAction != null) return staticConstructorAction;
		}
		return super.getDefaultAction(patch);
	}

}
