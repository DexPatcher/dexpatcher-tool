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

public abstract class FieldSetPatcher extends MemberSetPatcher<Field> {

	public FieldSetPatcher(ClassSetPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent, logMemberType, annotation);
	}

	// Adapters

	@Override
	protected final String getId(Field t) {
		return Util.getFieldId(t);
	}

	// Implementation

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

	@Override
	protected Field onSimpleReplace(Field patch, PatcherAnnotation annotation, Field target) {
		throw new AssertionError("Replace field");
	}

	// Handlers

	protected abstract EncodedValue filterInitialValue(Field patch, EncodedValue value);

}
