package lanchon.dexpatcher;

import org.jf.dexlib2.AccessFlags;

import lanchon.dexpatcher.PatcherAnnotation.ParseException;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class MemberSetPatcher<T> extends AbstractPatcher<T> {

	private final String logMemberType;
	private final Action defaultAction;
	protected final Action staticConstructorAction;
	protected final Action resolvedStaticConstructorAction;

	public MemberSetPatcher(Logger logger, String baseLogPrefix, String logMemberType, PatcherAnnotation annotation) {
		super(logger, baseLogPrefix);
		this.logMemberType = logMemberType;
		defaultAction = annotation.getDefaultAction();
		staticConstructorAction = annotation.getStaticConstructorAction();
		resolvedStaticConstructorAction = (staticConstructorAction != null ? staticConstructorAction : defaultAction);
	}

	// Adapters

	@Override
	protected String parsePatcherAnnotation(T patch, PatcherAnnotation annotation) throws ParseException {
		if (annotation.getTargetClass() != null) onInvalidElement(PatcherAnnotation.AE_TARGET_CLASS);
		if (annotation.getStaticConstructorAction() != null) onInvalidElement(PatcherAnnotation.AE_STATIC_CONSTRUCTOR_ACTION);
		if (annotation.getDefaultAction() != null) onInvalidElement(PatcherAnnotation.AE_DEFAULT_ACTION);
		if (annotation.getOnlyEditMembers()) onInvalidElement(PatcherAnnotation.AE_ONLY_EDIT_MEMBERS);
		return annotation.getTarget();
	}

	private void onInvalidElement(String e) throws ParseException {
		throw new ParseException("invalid patcher annotation element (" + e + ")");
	}

	@Override
	protected String getLogPrefix(T patch) {
		return logMemberType + " '" + getId(patch) + "'";
	}

	@Override
	protected String getLogTargetPrefix(PatcherAnnotation annotation, String targetId) {
		return "target '" + annotation.getTarget() + "'";
	}

	// TODO:
	// When this commit ships: https://code.google.com/p/smali/issues/detail?id=237
	// Eliminate: protected abstract int getAccessFlags(T t);

	protected abstract int getAccessFlags(T t);

	// Handlers

	@Override
	protected PatcherAnnotation getDefaultAnnotation(T patch) {
		if (defaultAction != null) {
			log(INFO, "default action (" + defaultAction.getLabel() + ")");
			return new PatcherAnnotation(defaultAction, getAnnotations(patch));
		} else {
			log(ERROR, "no default action defined");
			return new PatcherAnnotation(Action.IGNORE, getAnnotations(patch));
		}
	}

	// TODO:
	// When this commit ships: https://code.google.com/p/smali/issues/detail?id=237
	// Change getId(t) to t.getName().

	@Override
	protected T onEdit(T patch, PatcherAnnotation annotation, T target) {
		String message = "'%s' modifier mismatch in targeted and edited members";
		int flags1 = getAccessFlags(patch);
		int flags2 = getAccessFlags(target);
		if (getId(patch).equals(getId(target))) {
			// Avoid duplicated messages if not renaming.
			if (logger.isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { NATIVE, DECLARED_SYNCHRONIZED }, message);
			if (logger.isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { SYNCHRONIZED }, message);
		} else {
			if (logger.isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, STRICTFP,
					ENUM, DECLARED_SYNCHRONIZED }, message);
			if (logger.isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { FINAL, SYNCHRONIZED, VOLATILE, BRIDGE,
					TRANSIENT, SYNTHETIC }, message);
			if (logger.isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, CONSTRUCTOR }, message);
		}
		return patch;
	}

	@Override
	protected void onEffectiveReplacement(T patched, T original) {
		String message = "'%s' modifier mismatch in original and replacement members";
		int flags1 = getAccessFlags(patched);
		int flags2 = getAccessFlags(original);
		if (logger.isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
				new AccessFlags[] { STATIC, FINAL, VOLATILE, TRANSIENT, VARARGS,
				ABSTRACT, STRICTFP, ENUM, CONSTRUCTOR }, message);
		if (logger.isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
				new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, BRIDGE, SYNTHETIC }, message);
		// Avoid duplicated messages if not renaming.
		//if (logger.isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
		//		new AccessFlags[] { SYNCHRONIZED, NATIVE, DECLARED_SYNCHRONIZED }, message);
	}

}
