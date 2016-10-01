package lanchon.dexpatcher.core.patchers;

import java.util.Collection;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.Util;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.ImmutableClassDef;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

// TODO: Warn about changes in superclass and interfaces.

public class ClassSetPatcher extends AnnotatableSetPatcher<ClassDef> {

	public ClassSetPatcher(Context context) {
		super(context);
	}

	// Adapters

	@Override
	protected final String getId(ClassDef t) {
		return t.getType();
	}

	@Override
	protected void setupLogPrefix(String id, ClassDef patch, ClassDef patched) {
		setupLogPrefix("type '" + Util.getTypeNameFromDescriptor(id) + "'");
		setSourceFileClass(patch);
	}

	// Implementation

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
			if (Util.isTypeDescriptor(target)) {
				targetId = target;
			} else {
				String base = Util.getTypeNameFromDescriptor(patchId);
				targetId = Util.getTypeDescriptorFromName(Util.resolveTypeName(target, base));
			}
		} else if (targetClass != null) {
			targetId = targetClass;
		} else {
			targetId = patchId;
		}
		setTargetLogPrefix(patchId, targetId);
		return targetId;
	}

	protected final void setTargetLogPrefix(String patchId, String targetId) {
		if (shouldLogTarget(patchId, targetId)) {
			extendLogPrefix("target '" + Util.getTypeNameFromDescriptor(targetId) + "'");
		}
	}

	@Override
	protected ClassDef onSimpleAdd(ClassDef patch, PatcherAnnotation annotation) {
		if (patch.getAnnotations() == annotation.getFilteredAnnotations()) {
			return patch;	// avoid creating a new object unless necessary
		}
		return new ImmutableClassDef(
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

		if (!annotation.getOnlyEditMembers()) {
			int flags1 = Util.getClassAccessFlags(patch);
			int flags2 = Util.getClassAccessFlags(target);
			// Avoid duplicated messages if not renaming.
			if (!inPlaceEdit) {
				String message = "'%s' modifier mismatch in targeted and edited types";
				if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
						new AccessFlags[] { STATIC, FINAL, INTERFACE, ABSTRACT, ANNOTATION, ENUM }, message);
				if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
						new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, SYNTHETIC }, message);
			} else {
				String message = "'%s' modifier mismatch in original and edited versions";
				if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
						new AccessFlags[] { STATIC, FINAL, INTERFACE, ABSTRACT, ANNOTATION, ENUM }, message);
				if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
						new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED }, message);
				if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
						new AccessFlags[] { SYNTHETIC }, message);
			}
		}

		ClassDef source;
		Collection<? extends Annotation> annotations;
		if (annotation.getOnlyEditMembers()) {
			source = target;
			annotations = target.getAnnotations();
		} else {
			source = patch;
			annotations = annotation.getFilteredAnnotations();
		}

		return new ImmutableClassDef(
				patch.getType(),
				source.getAccessFlags(),
				source.getSuperclass(),
				source.getInterfaces(),
				source.getSourceFile(),
				annotations,
				new StaticFieldSetPatcher(this, annotation)
						.process(target.getStaticFields(), patch.getStaticFields()),
				new InstanceFieldSetPatcher(this, annotation)
						.process(target.getInstanceFields(), patch.getInstanceFields()),
				new DirectMethodSetPatcher(this, annotation)
						.process(target.getDirectMethods(), patch.getDirectMethods()),
				new VirtualMethodSetPatcher(this, annotation)
						.process(target.getVirtualMethods(), patch.getVirtualMethods()));

	}

	@Override
	protected void onEffectiveReplacement(String id, ClassDef patch, ClassDef patched, ClassDef original, boolean inPlaceEdit) {
		// Avoid duplicated messages if not renaming.
		if (!inPlaceEdit) {
			int flags1 = Util.getClassAccessFlags(patched);
			int flags2 = Util.getClassAccessFlags(original);
			String message = "'%s' modifier mismatch in original and replacement types";
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, FINAL, INTERFACE, ABSTRACT, ANNOTATION, ENUM }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { SYNTHETIC }, message);
		}
	}

}
