/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patcher;

import java.util.Collections;
import java.util.Set;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.model.BasicClassDef;
import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.ElementalTypeRewriter;
import lanchon.dexpatcher.core.util.Id;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.core.util.Target;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

import static lanchon.dexpatcher.core.PatcherAnnotation.*;

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
		setupLogPrefix(getItemLabel() + " '" + Label.ofClass(item) + "'");
		setSourceFileClass(patch);
	}

	// Implementation

	@Override
	protected final String getId(ClassDef item) {
		return Id.ofClass(item);
	}

	@Override
	protected String getItemLabel() {
		return "type";
	}

	@Override
	protected int getAccessFlags(ClassDef item) {
		return DexUtils.getClassAccessFlags(item);
	}

	@Override
	protected Action getDefaultAction(String patchId, ClassDef patch) {
		return Action.ADD;
	}

	@Override
	protected void onPrepare(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		if (annotation.getRecursive()) throw invalidElement(Marker.ELEM_RECURSIVE);
		if (annotation.getStaticConstructorAction() == Action.WRAP) {
			throw new PatchException("invalid static constructor action (wrap)");
		}
	}

	@Override
	protected String getTargetId(String patchId, ClassDef patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		String targetClass = annotation.getTargetClass();
		String targetId =
				(target != null) ? Id.fromClassDescriptor(Target.resolveClassDescriptor(patch.getType(), target)) :
				(targetClass != null) ? Id.fromClassDescriptor(targetClass) :
				patchId;
		if (shouldLogTarget(patchId, targetId)) {
			extendLogPrefixWithTargetLabel(Label.fromClassId(targetId));
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
	protected ClassDef onSimpleEdit(ClassDef patch, PatcherAnnotation annotation, ClassDef target, boolean inPlace) {

		ClassDef source;
		Set<? extends Annotation> annotations;
		if (annotation.getContentOnly()) {
			source = target;
			annotations = target.getAnnotations();
			if (!inPlace) patch = renameClass(patch, target.getType());
		} else {
			// Log class access flags before processing members.
			super.onSimpleEdit(patch, annotation, target, inPlace);
			source = patch;
			annotations = annotation.getFilteredAnnotations();
			if (!inPlace) target = renameClass(target, patch.getType());
		}

		return new BasicClassDef(
				source.getType(),
				source.getAccessFlags(),
				source.getSuperclass(),
				source.getInterfaces(),
				source.getSourceFile(),
				annotations,
				Collections.unmodifiableCollection(new FieldSetPatcher(this, annotation)
						.process(target.getFields(), patch.getFields())),
				Collections.unmodifiableCollection(new MethodSetPatcher(this, annotation)
						.process(target.getMethods(), patch.getMethods())));

	}

	@Override
	protected ClassDef onSimpleReplace(ClassDef patch, PatcherAnnotation annotation, ClassDef target, boolean inPlace) {

		ClassDef source;
		Set<? extends Annotation> annotations;
		if (annotation.getContentOnly()) {
			source = target;
			annotations = target.getAnnotations();
			if (!inPlace) patch = renameClass(patch, target.getType());
		} else {
			source = patch;
			annotations = annotation.getFilteredAnnotations();
			//if (!inPlace) target = renameClass(target, patch.getType());
		}

		return new BasicClassDef(
				source.getType(),
				source.getAccessFlags(),
				source.getSuperclass(),
				source.getInterfaces(),
				source.getSourceFile(),
				annotations,
				patch.getStaticFields(),
				patch.getInstanceFields(),
				patch.getDirectMethods(),
				patch.getVirtualMethods());

	}

	// Helpers

	private static ClassDef renameClass(ClassDef classDef, final String to) {
		final String from = classDef.getType();
		RewriterModule rewriterModule = new RewriterModule() {
			@Override
			public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
				return new ElementalTypeRewriter() {
					@Override
					public String rewriteElementalType(String elementalType) {
						return elementalType.equals(from) ? to : elementalType;
					}
				};
			}
		};
		return new DexRewriter(rewriterModule).getClassDefRewriter().rewrite(classDef);
	}

}
