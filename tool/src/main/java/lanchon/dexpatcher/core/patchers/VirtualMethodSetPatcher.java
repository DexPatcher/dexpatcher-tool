package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.PatcherAnnotation;

public class VirtualMethodSetPatcher extends MethodSetPatcher {

	public VirtualMethodSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Implementation

    @Override
    protected String getSetItemLabel() {
        return "virtual method";
    }

}
