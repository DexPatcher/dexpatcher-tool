package lanchon.dexpatcher.core.patchers;

import java.util.ArrayList;
import java.util.List;

import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.Util;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.debug.LineNumber;
import org.jf.dexlib2.iface.debug.SetSourceFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;

public abstract class MethodSetPatcher extends MemberSetPatcher<Method> {

	private Method sourceFileMethod;
	private int sourceFileLine;

	public MethodSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Debug Info

	protected void setSourceFileMethod(Method sourceFileMethod) {
		this.sourceFileMethod = sourceFileMethod;
		sourceFileLine = 0;
	}

	@Override
	protected int getSourceFileLine() {
		// Parse debug information lazily.
		if (sourceFileMethod != null) {
			MethodImplementation mi = sourceFileMethod.getImplementation();
			if (mi != null) {
				for (DebugItem di : mi.getDebugItems()) {
					if (di instanceof LineNumber) {
						sourceFileLine = ((LineNumber) di).getLineNumber();
						break;
					}
					if (di instanceof SetSourceFile) {
						// TODO: Support this type of debug item.
						break;
					}
				}
			}
			sourceFileMethod = null;
		}
		return sourceFileLine;
	}

	// Implementation

	@Override
	protected final String getId(Method item) {
		return Util.getMethodId(item);
	}

	@Override
	protected void setupLogPrefix(String id, Method item, Method patch, Method patched) {
		super.setupLogPrefix(id, item, patch, patched);
		setSourceFileMethod(patch);
	}

	@Override
	protected String getTargetId(String patchId, Method patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		String targetId;
		if (isTaggedByParameter(patch)) {
			ArrayList<MethodParameter> parameters = new ArrayList<MethodParameter>(patch.getParameters());
			parameters.remove(parameters.size() - 1);
			target = (target != null ? target : patch.getName());
			targetId = Util.getMethodId(parameters, patch.getReturnType(), target);
		}
		else {
			targetId = target != null ? Util.getMethodId(patch, target) : patchId;
		}
		setTargetLogPrefix(patchId, targetId, annotation);
		return targetId;
	}

	private boolean isTaggedByParameter(Method patch) {
		List<? extends MethodParameter> parameters = patch.getParameters();
		int size = parameters.size();
		if (size == 0) return false;
		return getContext().getTagTypeDescriptor().equals(parameters.get(size - 1).getType());
	}

	@Override
	protected Method onSimpleAdd(Method patch, PatcherAnnotation annotation) {
		if (patch.getAnnotations() == annotation.getFilteredAnnotations()) {
			return patch;	// avoid creating a new object unless necessary
		}
		return new ImmutableMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				patch.getAccessFlags(),
				annotation.getFilteredAnnotations(),
				patch.getImplementation());
	}

	@Override
	protected Method onSimpleEdit(Method patch, PatcherAnnotation annotation, Method target, boolean inPlaceEdit) {

		//String message = "updating '%s' modifier in edited member to match its target";
		//AccessFlags[] flagArray = new AccessFlags[] { CONSTRUCTOR };
		//int flagMask = AccessFlags.CONSTRUCTOR.getValue();
		//int patchFlags = patch.getAccessFlags();
		//int targetFlags = target.getAccessFlags();
		//checkAccessFlags(INFO, patchFlags, targetFlags, flagArray, message);
		//int flags = (patchFlags & ~flagMask) | (targetFlags & flagMask); 

		int flags = patch.getAccessFlags();

		MethodImplementation implementation = target.getImplementation();
		if (isTaggedByParameter(patch)) {
			implementation = new ImmutableMethodImplementation(
					implementation.getRegisterCount() + 1,
					implementation.getInstructions(),
					implementation.getTryBlocks(),
					implementation.getDebugItems());
		}

		Method patched = new ImmutableMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				flags,
				annotation.getFilteredAnnotations(),
				implementation);

		return super.onSimpleEdit(patched, annotation, target, inPlaceEdit);

	}

}
