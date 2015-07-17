package lanchon.dexpatcher;

import java.util.ArrayList;
import java.util.List;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;

public class MethodSetPatcher extends MemberSetPatcher<Method> {

	public MethodSetPatcher(ClassSetPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent, logMemberType, annotation);
	}

	// Adapters

	@Override
	protected String getId(Method t) {
		return Util.getMethodId(t);
	}

	// Handlers

	@Override
	protected String getTargetId(String patchId, Method patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		if (isTaggedByParameter(patch)) {
			ArrayList<MethodParameter> parameters = new ArrayList<MethodParameter>(patch.getParameters());
			parameters.remove(parameters.size() - 1);
			target = (target != null ? target : patch.getName());
			return Util.getMethodId(parameters, patch.getReturnType(), target);
		}
		else {
			return target != null ? Util.getMethodId(patch, target) : patchId;
		}
	}

	private boolean isTaggedByParameter(Method patch) {
		List<? extends MethodParameter> parameters = patch.getParameters();
		int size = parameters.size();
		if (size == 0) return false;
		return Tag.TYPE_TAG.equals(parameters.get(size - 1).getType());
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
	protected Method onSimpleEdit(Method patch, PatcherAnnotation annotation, Method target, boolean renaming) {

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

		return super.onSimpleEdit(patched, annotation, target, renaming);

	}

}
