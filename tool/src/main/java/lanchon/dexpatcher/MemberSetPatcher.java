package lanchon.dexpatcher;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Member;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class MemberSetPatcher<T extends Member> extends AnnotatableSetPatcher<T> {

	private final String logMemberType;
	private final Action defaultAction;
	protected final Action staticConstructorAction;
	protected final Action resolvedStaticConstructorAction;

	public MemberSetPatcher(ClassSetPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent);
		this.logMemberType = logMemberType;
		defaultAction = annotation.getDefaultAction();
		staticConstructorAction = annotation.getStaticConstructorAction();
		resolvedStaticConstructorAction = (staticConstructorAction != null ? staticConstructorAction : defaultAction);
	}

	// Adapters

	@Override
	protected String getLogPrefix(String id, T t) {
		return logMemberType + " '" + id + "'";
	}

	@Override
	protected String getTargetLogPrefix(String targetId, PatcherAnnotation annotation) {
		// TODO: Show changes in method arguments even if target is explicit.
		String target = annotation.getTarget();
		if (target == null) target = targetId;
		return "target '" + target + "'";
	}

	// Handlers

	@Override
	protected Action getDefaultAction(String patchId, T patch) {
		if (defaultAction != null) {
			log(INFO, "default " + defaultAction.getLabel());
			return defaultAction;
		} else {
			log(ERROR, "no default action defined");
			return Action.IGNORE;
		}
	}

	@Override
	protected void onPrepare(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		if (annotation.getTargetClass() != null) PatcherAnnotation.throwInvalidElement(Tag.ELEM_TARGET_CLASS);
		if (annotation.getStaticConstructorAction() != null) PatcherAnnotation.throwInvalidElement(Tag.ELEM_STATIC_CONSTRUCTOR_ACTION);
		if (annotation.getDefaultAction() != null) PatcherAnnotation.throwInvalidElement(Tag.ELEM_DEFAULT_ACTION);
		if (annotation.getOnlyEditMembers()) PatcherAnnotation.throwInvalidElement(Tag.ELEM_ONLY_EDIT_MEMBERS);
		if (annotation.getRecursive()) PatcherAnnotation.throwInvalidElement(Tag.ELEM_RECURSIVE);
	}

	@Override
	protected T onSimpleEdit(T patch, PatcherAnnotation annotation, T target, boolean renaming) {
		int flags1 = patch.getAccessFlags();
		int flags2 = target.getAccessFlags();
		// Avoid duplicated messages if not renaming.
		if (renaming) {
			String message = "'%s' modifier mismatch in targeted and edited members";
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, ENUM, DECLARED_SYNCHRONIZED }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, STRICTFP }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, BRIDGE, SYNTHETIC, CONSTRUCTOR }, message);
		} else {
			String message = "'%s' modifier mismatch in original and edited versions";
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, ENUM, CONSTRUCTOR, DECLARED_SYNCHRONIZED }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, STRICTFP }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { BRIDGE, SYNTHETIC }, message);
		}
		return patch;
	}

	@Override
	protected void onEffectiveReplacement(String id, T patched, T original, boolean editedInPlace) {
		// Avoid duplicated messages if not renaming.
		if (!editedInPlace) {
			int flags1 = patched.getAccessFlags();
			int flags2 = original.getAccessFlags();
			String message = "'%s' modifier mismatch in original and replacement members";
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC,  ABSTRACT, ENUM, CONSTRUCTOR }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, FINAL, VOLATILE, TRANSIENT, VARARGS }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { SYNCHRONIZED, BRIDGE, NATIVE, STRICTFP, SYNTHETIC, DECLARED_SYNCHRONIZED }, message);
		}
	}

}
