/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.model.BasicField;
import lanchon.dexpatcher.core.util.Id;
import lanchon.dexpatcher.core.util.Label;

import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.util.FieldUtil;

import static lanchon.dexpatcher.core.PatcherAnnotation.*;
import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public class FieldSetPatcher extends MemberSetPatcher<Field> {

	public FieldSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Logging

	@Override
	protected void setupLogPrefix(String id, Field item, Field patch, Field patched) {
		setupLogPrefix(getSetItemLabel() + " '" + Label.ofField(item) + "'");
	}

	// Implementation

	@Override
	protected final String getId(Field item) {
		return Id.ofField(item);
	}

	@Override
	protected String getSetItemLabel() {
		return "field";
	}

	@Override
	protected String getSetItemShortLabel() {
		return "field";
	}

	@Override
	protected void onPrepare(String patchId, Field patch, PatcherAnnotation annotation) throws PatchException {
		if (annotation.getAction() == Action.REPLACE) throw invalidAnnotation(Marker.REPLACE);
		super.onPrepare(patchId, patch, annotation);
	}

	@Override
	protected String getTargetId(String patchId, Field patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		String targetId = (target != null ? Id.ofField(patch, target) : patchId);
		if (shouldLogTarget(patchId, targetId)) {
			extendLogPrefixWithTargetLabel(Label.shortLabelOfMember(target));
		}
		return targetId;
	}

	@Override
	protected Field onSimpleAdd(Field patch, PatcherAnnotation annotation) {

		EncodedValue value = filterInitialValue(patch, null);

		return new BasicField(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getType(),
				patch.getAccessFlags(),
				value,
				annotation.getFilteredAnnotations());

	}

	@Override
	protected Field onSimpleEdit(Field patch, PatcherAnnotation annotation, Field target, boolean inPlace) {

		// Use the static field initializer value in source only
		// if not renaming, given that the static constructor in
		// source would only initialize it if not renamed.
		// This makes behavior predictable across compilers.
		EncodedValue value = inPlace ? target.getInitialValue() : null;
		value = filterInitialValue(patch, value);

		onSimpleRemove(patch, annotation, target);

		Field patched = new BasicField(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getType(),
				patch.getAccessFlags(),
				value,
				annotation.getFilteredAnnotations());

		return super.onSimpleEdit(patched, annotation, target, inPlace);

	}

	@Override
	protected Field onSimpleReplace(Field patch, PatcherAnnotation annotation, Field target, boolean inPlace) {
		throw new AssertionError("Replace field");
	}

	@Override
	protected void onSimpleRemove(Field patch, PatcherAnnotation annotation, Field target) {
		if (FieldUtil.isStatic(target)) {
			if (FINAL.isSet(target.getAccessFlags())) {
				log(WARN, "original value of final static field is likely to be embedded in code");
			}
		}
	}

	// Helpers

	private EncodedValue filterInitialValue(Field patch, EncodedValue value){
		if (FieldUtil.isStatic(patch)) {
			// Use the static field initializer values in patch if and
			// only if the static constructor code in patch is being used.
			// This makes behavior more predictable across compilers.
			Action action = resolvedStaticConstructorAction;
			if (action != null && action.ignoresCode()) {
				log(WARN, "static field will not be initialized as specified in patch because code of static constructor of class is being discarded");
				return value;
			} else {
				EncodedValue patchValue = patch.getInitialValue();
				return (patchValue != null) ? patchValue : value;
			}
		} else {
			// Instance fields should never have initializer values.
			EncodedValue patchValue = patch.getInitialValue();
			if (patchValue != null) {
				log(ERROR, "unexpected instance field initializer value in patch");
			}
			return (patchValue != null) ? patchValue : value;
		}
	}

}
