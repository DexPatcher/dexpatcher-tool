package lanchon.dexpatcher;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.immutable.ImmutableMethod;

import lanchon.dexpatcher.PatcherAnnotation.ParseException;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public class MethodSetPatcher extends MemberSetPatcher<Method> {

	public MethodSetPatcher(Logger logger, String baseLogPrefix, String logMemberType, boolean warnOnImplicitIgnore) {
		super(logger, baseLogPrefix, logMemberType, warnOnImplicitIgnore);
	}

	// Adapters

	@Override
	protected String getId(Method t) {
		return Util.getMethodId(t);
	}

	@Override
	protected PatcherAnnotation getPatcherAnnotation(Method patch) throws ParseException {
		return PatcherAnnotation.parse(patch.getAnnotations());
	}

	@Override
	protected String parsePatcherAnnotation(Method patch, PatcherAnnotation annotation) throws ParseException {
		String target = super.parsePatcherAnnotation(patch, annotation);
		return target != null ? Util.getMethodId(patch, target) : null;
	}

	@Override
	protected int getAccessFlags(Method t) {
		return t.getAccessFlags();
	}

	// Handlers

	@Override
	protected Method onAdd(Method patch, PatcherAnnotation annotation) {
		if (annotation == null) {
			return patch;
		} else {
			return new ImmutableMethod(
					patch.getDefiningClass(),
					patch.getName(),
					patch.getParameters(),
					patch.getReturnType(),
					patch.getAccessFlags(),
					annotation.getFilteredAnnotations(),
					patch.getImplementation());
		}
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

		Method patched = new ImmutableMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				flags,
				annotation.getFilteredAnnotations(),
				target.getImplementation());

		return super.onEdit(patched, annotation, target);

	}

}
