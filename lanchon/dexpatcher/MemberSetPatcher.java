package lanchon.dexpatcher;

import org.jf.dexlib2.AccessFlags;
import lanchon.dexpatcher.PatcherAnnotation.ParseException;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class MemberSetPatcher<T> extends AbstractPatcher<T> {

	private final String logMemberType;
	private final boolean warnOnImplicitIgnore;

	public MemberSetPatcher(Logger logger, String baseLogPrefix, String logMemberType, boolean warnOnImplicitIgnore) {
		super(logger, baseLogPrefix);
		this.logMemberType = logMemberType;
		this.warnOnImplicitIgnore = warnOnImplicitIgnore;
	}

	// Adapters

	@Override
	protected String parsePatcherAnnotation(T patch, PatcherAnnotation annotation) throws ParseException {
		if (annotation.getTargetClass() != null) {
			throw new ParseException("invalid patcher annotation element (" + PatcherAnnotation.AE_TARGET_CLASS + ")");
		}
		if (annotation.getWarnOnImplicitIgnore()) {
			throw new ParseException("invalid patcher annotation element (" + PatcherAnnotation.AE_WARN_ON_IMPLICIT_IGNORE + ")");
		}
		return annotation.getTarget();
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
	protected Action onUnspecifiedAction(T patch, PatcherAnnotation annotation) {
		if (warnOnImplicitIgnore) log(WARN, "implicitly ignored");
		return Action.IGNORE;
	}

	// TODO:
	// When this commit ships: https://code.google.com/p/smali/issues/detail?id=237
	// Change getId(t) to t.getName().

	@Override
	protected T onEdit(T patch, PatcherAnnotation annotation, T target) {
		if (!getId(patch).equals(getId(target))) {		// avoid duplicated messages if not renaming
			String message = "'%s' modifier mismatch in targeted and edited members";
			int flags1 = getAccessFlags(patch);
			int flags2 = getAccessFlags(target);
			checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, VARARGS,
					NATIVE, ABSTRACT, STRICTFP, ENUM, DECLARED_SYNCHRONIZED }, message);
			checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { BRIDGE, SYNTHETIC, CONSTRUCTOR }, message);
			// Ignored flags: PUBLIC, PRIVATE, PROTECTED
		}
		return patch;
	}

	@Override
	protected void onEffectiveReplacement(T patched, T original) {
		String message = "'%s' modifier mismatch in original and replacement members";
		int flags1 = getAccessFlags(patched);
		int flags2 = getAccessFlags(original);
		checkAccessFlags(WARN, flags1, flags2,
				new AccessFlags[] { STATIC, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, VARARGS,
				NATIVE, ABSTRACT, STRICTFP, ENUM, CONSTRUCTOR, DECLARED_SYNCHRONIZED }, message);
		checkAccessFlags(INFO, flags1, flags2,
				new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, BRIDGE, SYNTHETIC }, message);
	}

}
