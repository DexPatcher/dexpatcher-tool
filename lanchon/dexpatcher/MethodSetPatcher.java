package lanchon.dexpatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;

import lanchon.dexpatcher.PatcherAnnotation.ParseException;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public class MethodSetPatcher extends MemberSetPatcher<Method> {

	public MethodSetPatcher(Logger logger, String baseLogPrefix, String logMemberType, PatcherAnnotation annotation) {
		super(logger, baseLogPrefix, logMemberType, annotation);
	}

	// Adapters

	@Override
	protected String getId(Method t) {
		return Util.getMethodId(t);
	}

	@Override
	protected Set<? extends Annotation> getAnnotations(Method patch) {
		return patch.getAnnotations();
	}

	@Override
	protected String parsePatcherAnnotation(Method patch, PatcherAnnotation annotation) throws ParseException {
		String target = super.parsePatcherAnnotation(patch, annotation);
		if (isTaggedByParameter(patch)) {
			ArrayList<MethodParameter> parameters = new ArrayList<MethodParameter>(patch.getParameters());
			parameters.remove(parameters.size() - 1);
			target = (target != null ? target : patch.getName());
			return Util.getMethodId(parameters, patch.getReturnType(), target);
		}
		else return target != null ? Util.getMethodId(patch, target) : null;
	}

	private boolean isTaggedByParameter(Method patch) {
		List<? extends MethodParameter> parameters = patch.getParameters();
		int size = parameters.size();
		if (size == 0) return false;
		return Tag.TYPE_TAG.equals(parameters.get(size - 1).getType());
	}

	@Override
	protected int getAccessFlags(Method t) {
		return t.getAccessFlags();
	}

	// Handlers

	@Override
	protected Method onAdd(Method patch, PatcherAnnotation annotation) {
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

	@SuppressWarnings("unused")
	@Override
	protected Method onEdit(Method patch, PatcherAnnotation annotation, Method target) {

		int flags;
		if (false) {
			String message = "updating '%s' modifier in edited member to match its target";
			AccessFlags[] flagArray = new AccessFlags[] { CONSTRUCTOR };
			int flagMask = AccessFlags.CONSTRUCTOR.getValue();
			int patchFlags = patch.getAccessFlags();
			int targetFlags = target.getAccessFlags();
			checkAccessFlags(INFO, patchFlags, targetFlags, flagArray, message);
			flags = (patchFlags & ~flagMask) | (targetFlags & flagMask); 
		}
		else flags = patch.getAccessFlags();

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

		return super.onEdit(patched, annotation, target);

	}

}
