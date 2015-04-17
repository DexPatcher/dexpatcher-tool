package lanchon.dexpatcher;

import java.util.Collection;
import java.util.Set;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;

import lanchon.dexpatcher.PatcherAnnotation.ParseException;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

// TODO:
// Warn about changes in superclass and interfaces.

public class DexPatcher extends AbstractPatcher<ClassDef> {

	public DexPatcher(Logger logger) {
		super(logger, null);
	}

	public DexFile run(DexFile sourceDex, DexFile patchDex) {
		Set<? extends ClassDef> sourceClasses = sourceDex.getClasses();
		Set<? extends ClassDef> patchClasses = patchDex.getClasses();
		return new ImmutableDexFile(process(sourceClasses, sourceClasses.size(), patchClasses, patchClasses.size()));
	}

	// Adapters

	@Override
	protected String getId(ClassDef t) {
		return t.getType();
	}

	@Override
	protected Set<? extends Annotation> getAnnotations(ClassDef patch) {
		return patch.getAnnotations();
	}

	@Override
	protected String parsePatcherAnnotation(ClassDef patch, PatcherAnnotation annotation) throws ParseException {
		String target = annotation.getTarget();
		String targetClass = annotation.getTargetClass();
		String targetId;
		if (target != null) {
			int l = target.length();					// target cannot be the empty string
			if (target.charAt(l - 1) != ';') {			// if target is not a type descriptor
				if (target.indexOf('.') == -1) {		// if target is not a fully qualified name
					String base = Util.getTypeNameFromDescriptor(patch.getType());
					int i = base.lastIndexOf('.');
					if (target.indexOf('$') == -1) {	// if target is not a qualified nested type
						i = Math.max(i, base.lastIndexOf('$'));
					}
					if (i != -1) target = base.substring(0, i + 1) + target;
				}
				target = Util.getTypeDescriptorFromName(target);
			}
			targetId = target;
		} else {
			targetId = targetClass;				
		}
		return targetId;
	}

	@Override
	protected String getLogPrefix(ClassDef patch) {
		return "type '" + Util.getTypeNameFromDescriptor(patch.getType()) + "'";
	}

	@Override
	protected String getLogTargetPrefix(PatcherAnnotation annotation, String targetId) {
		return "target '" + Util.getTypeNameFromDescriptor(targetId) + "'";
	}

	// Handlers

	@Override
	protected Action getDefaultAction(ClassDef patch) {
		return Action.ADD;
	}

	@Override
	protected ClassDef onAdd(ClassDef patch, PatcherAnnotation annotation) {
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
	protected ClassDef onEdit(ClassDef patch, PatcherAnnotation annotation, ClassDef target) {

		if (!patch.getType().equals(target.getType())) {		// avoid duplicated messages if not renaming
			String message = "'%s' modifier mismatch in targeted and edited types";
			int flags1 = Util.getClassAccessFlags(patch);
			int flags2 = Util.getClassAccessFlags(target);
			if (logger.isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, FINAL, INTERFACE, ABSTRACT, ANNOTATION, ENUM }, message);
			if (logger.isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { SYNTHETIC }, message);
			if (logger.isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED }, message);
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
				source.getType(),
				source.getAccessFlags(),
				source.getSuperclass(),
				source.getInterfaces(),
				source.getSourceFile(),
				annotations,
				new FieldSetPatcher(logger, getLogPrefix(), "static field", annotation)
						.process(target.getStaticFields(), patch.getStaticFields()),
				new FieldSetPatcher(logger, getLogPrefix(), "instance field", annotation)
						.process(target.getInstanceFields(), patch.getInstanceFields()),
				new DirectMethodSetPatcher(logger, getLogPrefix(), "direct method", annotation)
						.process(target.getDirectMethods(), patch.getDirectMethods()),
				new MethodSetPatcher(logger, getLogPrefix(), "virtual method", annotation)
						.process(target.getVirtualMethods(), patch.getVirtualMethods()));

	}

	@Override
	protected void onEffectiveReplacement(ClassDef patched, ClassDef original) {
		String message = "'%s' modifier mismatch in original and replacement types";
		int flags1 = Util.getClassAccessFlags(patched);
		int flags2 = Util.getClassAccessFlags(original);
		if (logger.isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
				new AccessFlags[] { STATIC, FINAL, INTERFACE, ABSTRACT, ANNOTATION, ENUM }, message);
		if (logger.isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
				new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, SYNTHETIC }, message);
	}

}
