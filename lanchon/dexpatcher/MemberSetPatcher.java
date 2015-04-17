package lanchon.dexpatcher;

import org.jf.dexlib2.AccessFlags;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class MemberSetPatcher<T> extends SimplePatcher<T> {

	private final String logMemberType;
	private final Action defaultAction;
	protected final Action staticConstructorAction;
	protected final Action resolvedStaticConstructorAction;

	public MemberSetPatcher(DexPatcher parent, String logMemberType, PatcherAnnotation annotation) {
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
		return "target '" + annotation.getTarget() + "'";
	}

	// TODO:
	// When this commit ships: https://code.google.com/p/smali/issues/detail?id=237
	// Eliminate: protected abstract int getAccessFlags(T t);

	protected abstract int getAccessFlags(T t);

	// Handlers

	@Override
	protected Action getDefaultAction(String patchId, T patch) {
		if (defaultAction != null) {
			log(INFO, "default action (" + defaultAction.getLabel() + ")");
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
		String message = "'%s' modifier mismatch in targeted and edited members";
		int flags1 = getAccessFlags(patch);
		int flags2 = getAccessFlags(target);
		// Avoid duplicated messages if not renaming.
		if (renaming) {
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, STRICTFP,
					ENUM, DECLARED_SYNCHRONIZED }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { FINAL, SYNCHRONIZED, VOLATILE, BRIDGE,
					TRANSIENT, SYNTHETIC }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, CONSTRUCTOR }, message);
		} else {
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { NATIVE, DECLARED_SYNCHRONIZED }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { SYNCHRONIZED }, message);
		}
		return patch;
	}

	@Override
	protected void onEffectiveReplacement(String id, T patched, T original, boolean editedInPlace) {
		String message = "'%s' modifier mismatch in original and replacement members";
		int flags1 = getAccessFlags(patched);
		int flags2 = getAccessFlags(original);
		if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
				new AccessFlags[] { STATIC, FINAL, VOLATILE, TRANSIENT, VARARGS,
				ABSTRACT, STRICTFP, ENUM, CONSTRUCTOR }, message);
		// Avoid duplicated messages if not renaming.
		if (editedInPlace) {
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, BRIDGE, SYNTHETIC }, message);
			// Ignored flags: SYNCHRONIZED, NATIVE, DECLARED_SYNCHRONIZED
		} else {
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, SYNCHRONIZED,
					BRIDGE, NATIVE, SYNTHETIC, DECLARED_SYNCHRONIZED }, message);
		}
	}

}
