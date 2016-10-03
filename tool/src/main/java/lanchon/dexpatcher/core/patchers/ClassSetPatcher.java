package lanchon.dexpatcher.core.patchers;

import java.util.Collections;
import java.util.Set;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.Util;
import lanchon.dexpatcher.core.model.BasicClassDef;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

// TODO: Warn about changes in superclass and interfaces.

public class ClassSetPatcher extends AnnotatableSetPatcher<ClassDef> {

	public ClassSetPatcher(Context context) {
		super(context);
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
	protected void setupLogPrefix(String id, ClassDef item, ClassDef patch, ClassDef patched) {
		setupLogPrefix(getSetItemLabel() + " '" + Util.getTypeLabel(item) + "'");
		setSourceFileClass(patch);
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
		if (patch.getAnnotations() == annotation.getFilteredAnnotations()) {
			return patch;	// avoid creating a new object unless necessary
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
		Set<? extends Annotation> annotations;
		if (annotation.getOnlyEditMembers()) {
			source = target;
			annotations = target.getAnnotations();
		} else {
			source = patch;
			annotations = annotation.getFilteredAnnotations();
		}

		return new BasicClassDef(
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
