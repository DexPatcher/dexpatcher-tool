package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.Util;

import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.ImmutableField;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public class FieldSetPatcher extends MemberSetPatcher<Field> {

	public FieldSetPatcher(ClassSetPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent, logMemberType, annotation);
	}

	// Adapters

	@Override
	protected final String getId(Field t) {
		return Util.getFieldId(t);
	}

	// Handlers

	@Override
	protected void onPrepare(String patchId, Field patch, PatcherAnnotation annotation) throws PatchException {
		Action action = annotation.getAction();
		if (action == Action.REPLACE) PatcherAnnotation.throwInvalidAnnotation(Marker.REPLACE);
		super.onPrepare(patchId, patch, annotation);
	}

	@Override
	protected String getTargetId(String patchId, Field patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		String targetId = target != null ? Util.getFieldId(patch, target) : patchId;
		setTargetLogPrefix(patchId, targetId, annotation);
		return targetId;

	}

	@Override
	protected Field onSimpleAdd(Field patch, PatcherAnnotation annotation) {
		EncodedValue value = filterInitialValue(patch, null);
		return new ImmutableField(
			patch.getDefiningClass(),
			patch.getName(),
			patch.getType(),
			patch.getAccessFlags(),
			value,
			annotation.getFilteredAnnotations());
	}

	@Override
	protected Field onSimpleEdit(Field patch, PatcherAnnotation annotation, Field target, boolean inPlaceEdit) {
		// Use the static field initializer value in source only
		// if not renaming, given that the static constructor in
		// source would only initialize it if not renamed.
		// This makes behavior predictable across compilers.
		EncodedValue value = inPlaceEdit ? target.getInitialValue() : null;
		value = filterInitialValue(patch, value);
		onSimpleRemove(patch, annotation, target);
		Field patched = new ImmutableField(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getType(),
				patch.getAccessFlags(),
				value,
				annotation.getFilteredAnnotations());
		return super.onSimpleEdit(patched, annotation, target, inPlaceEdit);
	}

	private EncodedValue filterInitialValue(Field patch, EncodedValue value) {
		if (STATIC.isSet(patch.getAccessFlags())) {
			// Use the static field initializer values in patch if and
			// only if the static constructor in patch is being used.
			// This makes behavior predictable across compilers.
			if (resolvedStaticConstructorAction == null) {
				log(ERROR, "must define an action for the static constructor of the class");
			} else if (resolvedStaticConstructorAction == Action.ADD || resolvedStaticConstructorAction == Action.REPLACE) {
				value = patch.getInitialValue();
			} else {
				log(WARN, "static field will not be initialized as specified in patch because the static constructor code in patch is being ignored");
			}
		} else {
			// Instance fields should never have initializer values.
			if (patch.getInitialValue() != null) {
				log(ERROR, "unexpected instance field initializer value in patch");
			}
		}
		return value;
	}

	@Override
	protected Field onSimpleReplace(Field patch, PatcherAnnotation annotation, Field target) {
		throw new AssertionError("Replace field");
	}

	@Override
	protected void onSimpleRemove(Field patch, PatcherAnnotation annotation, Field target) {
		int flags = target.getAccessFlags();
		if (STATIC.isSet(flags) && FINAL.isSet(flags)) {
			log(WARN, "original value of final static field can be embedded in code");
		}
	}

}
