package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Method;

public class VirtualMethodSetPatcher extends MethodSetPatcher {

	public VirtualMethodSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Implementation

	@Override
	protected void setupLogPrefix(String id, Method patch, Method patched) {
		setupLogPrefix("virtual method '" + id + "'");
		setSourceFileMethod(patch);
	}

}
