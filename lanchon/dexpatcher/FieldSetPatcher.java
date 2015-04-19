package lanchon.dexpatcher;

import java.util.Set;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.ImmutableField;

import static lanchon.dexpatcher.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public class FieldSetPatcher extends MemberSetPatcher<Field> {

	public FieldSetPatcher(ClassSetPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent, logMemberType, annotation);
	}

	// Adapters

	@Override
	protected String getId(Field t) {
		return Util.getFieldId(t);
	}

	@Override
	protected Set<? extends Annotation> getAnnotations(Field patch) {
		return patch.getAnnotations();
	}

	@Override
	protected int getAccessFlags(Field t) {
		return t.getAccessFlags();
	}

	// Handlers

	@Override
	protected void onPrepare(String patchId, Field patch, PatcherAnnotation annotation) throws PatchException {
		Action action = annotation.getAction();
		if (action == Action.REPLACE) PatcherAnnotation.throwInvalidAnnotation(Tag.REPLACE);
		super.onPrepare(patchId, patch, annotation);
	}

	@Override
	protected String getTargetId(String patchId, Field patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		return target != null ? Util.getFieldId(patch, target) : patchId;
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
	protected Field onSimpleEdit(Field patch, PatcherAnnotation annotation, Field target, boolean renaming) {
		// Use the static field initializer value in source only
		// if not renaming, given that the static constructor in
		// source would only initialize it if not renamed.
		// This makes behavior predictable across compilers.
		EncodedValue value = renaming ? null : target.getInitialValue();
		value = filterInitialValue(patch, value);
		if (FINAL.isSet(target.getAccessFlags())) {
			log(WARN, "value of final field might be embedded in code");
		}
		Field patched = new ImmutableField(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getType(),
				patch.getAccessFlags(),
				value,
				annotation.getFilteredAnnotations());
		return super.onSimpleEdit(patched, annotation, target, renaming);
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
				log(WARN, "field will not be initialized as specified in patch because the static constructor code in patch is being ignored");
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

}
