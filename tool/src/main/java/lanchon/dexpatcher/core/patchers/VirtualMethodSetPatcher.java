package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.PatcherAnnotation;

public class VirtualMethodSetPatcher extends MethodSetPatcher {

	public VirtualMethodSetPatcher(ClassSetPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent, logMemberType, annotation);
	}

}
