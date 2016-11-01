/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patchers;

import java.util.Collections;
import java.util.Set;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.Util;
import lanchon.dexpatcher.core.model.BasicClassDef;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;

// TODO: Warn about changes in superclass and interfaces.

public class ClassSetPatcher extends AnnotatableSetPatcher<ClassDef> {

	public ClassSetPatcher(Context context) {
		super(context);
	}

	// Logging

	@Override
	protected void clearLogPrefix() {
		super.clearLogPrefix();
		setSourceFileClass(null);
	}

	@Override
	protected void setupLogPrefix(String id, ClassDef item, ClassDef patch, ClassDef patched) {
		setupLogPrefix(getSetItemLabel() + " '" + Util.getTypeLabel(item) + "'");
		setSourceFileClass(patch);
	}

	// Implementation

	@Override
	protected final String getId(ClassDef item) {
		return Util.getTypeId(item);
	}

	@Override
	protected String getSetItemLabel() {
		return "type";
	}

	@Override
	protected int getAccessFlags(ClassDef item) {
		return Util.getClassAccessFlags(item);
	}

	@Override
	protected Action getDefaultAction(String patchId, ClassDef patch) {
		return Action.ADD;
	}

	@Override
	protected void onPrepare(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		if (annotation.getRecursive()) PatcherAnnotation.throwInvalidElement(Marker.ELEM_RECURSIVE);
	}

	@Override
	protected String getTargetId(String patchId, ClassDef patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		String targetClass = annotation.getTargetClass();
		String targetId;
		if (target != null) {
			if (Util.isLongTypeDescriptor(target)) {
				targetId = target;
			} else {
				String base = Util.getLongTypeNameFromDescriptor(patch.getType());
				targetId = Util.getTypeIdFromName(Util.resolveTypeName(target, base));
			}
		} else if (targetClass != null) {
			targetId = targetClass;
		} else {
			targetId = patchId;
		}
		if (shouldLogTarget(patchId, targetId)) {
			extendLogPrefixWithTargetLabel(Util.getTypeLabelFromId(targetId));
		}
		return targetId;
	}

	@Override
	protected ClassDef onSimpleAdd(ClassDef patch, PatcherAnnotation annotation) {

		// Avoid creating a new object if not necessary.
		if (patch.getAnnotations() == annotation.getFilteredAnnotations()) {
			return patch;
		}

		return new BasicClassDef(
				patch.getType(),
				patch.getAccessFlags(),
				patch.getSuperclass(),
				patch.getInterfaces(),
				patch.getSourceFile(),
				annotation.getFilteredAnnotations(),
				patch.getStaticFields(),
				patch.getInstanceFields(),
				patch.getDirectMethods(),
				patch.getVirtualMethods());

	}

	@Override
	protected ClassDef onSimpleEdit(ClassDef patch, PatcherAnnotation annotation, ClassDef target, boolean inPlaceEdit) {

		// Log class access flags before processing members.
		if (!annotation.getOnlyEditMembers()) {
			super.onSimpleEdit(patch, annotation, target, inPlaceEdit);
		}

		ClassDef source;
		Set<? extends Annotation> annotations;
		if (annotation.getOnlyEditMembers()) {
			source = target;
			annotations = target.getAnnotations();
		} else {
			source = patch;
			annotations = annotation.getFilteredAnnotations();
		}

		ClassDef patched = new BasicClassDef(
				patch.getType(),
				source.getAccessFlags(),
				source.getSuperclass(),
				source.getInterfaces(),
				source.getSourceFile(),
				annotations,
				Collections.unmodifiableCollection(new StaticFieldSetPatcher(this, annotation)
						.process(target.getStaticFields(), patch.getStaticFields())),
				Collections.unmodifiableCollection(new InstanceFieldSetPatcher(this, annotation)
						.process(target.getInstanceFields(), patch.getInstanceFields())),
				Collections.unmodifiableCollection(new DirectMethodSetPatcher(this, annotation)
						.process(target.getDirectMethods(), patch.getDirectMethods())),
				Collections.unmodifiableCollection(new VirtualMethodSetPatcher(this, annotation)
						.process(target.getVirtualMethods(), patch.getVirtualMethods())));

		//return super.onSimpleEdit(patched, annotation, target, inPlaceEdit);
		return patched;

	}

}
