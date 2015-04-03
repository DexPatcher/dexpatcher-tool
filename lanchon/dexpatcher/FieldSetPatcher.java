package lanchon.dexpatcher;

import java.util.Set;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.ImmutableField;
import lanchon.dexpatcher.PatcherAnnotation.ParseException;

import static lanchon.dexpatcher.Logger.Level.*;

public class FieldSetPatcher extends MemberSetPatcher<Field> {

	public FieldSetPatcher(Logger logger, String baseLogPrefix, String logMemberType, PatcherAnnotation annotation) {
		super(logger, baseLogPrefix, logMemberType, annotation);
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
	protected String parsePatcherAnnotation(Field patch, PatcherAnnotation annotation) throws ParseException {
		Action action = annotation.getAction();
		if (action == Action.REPLACE) {
			throw new ParseException("invalid patcher annotation (" + action.getAnnotationClassName() + ")");
		}
		String target = super.parsePatcherAnnotation(patch, annotation);
		return target != null ? Util.getFieldId(patch, target) : null;
	}

	@Override
	protected int getAccessFlags(Field t) {
		return t.getAccessFlags();
	}

	// Handlers

	@Override
	protected Field onAdd(Field patch, PatcherAnnotation annotation) {
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
	protected Field onEdit(Field patch, PatcherAnnotation annotation, Field target) {
		EncodedValue value = (patch.getName().equals(target.getName()) ? target.getInitialValue() : null);
		value = filterInitialValue(patch, value);
		if (AccessFlags.FINAL.isSet(target.getAccessFlags())) {
			log(WARN, "value of final field might be embedded in code");
		}
		Field patched = new ImmutableField(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getType(),
				patch.getAccessFlags(),
				value,
				annotation.getFilteredAnnotations());
		return super.onEdit(patched, annotation, target);
	}

	private EncodedValue filterInitialValue(Field patch, EncodedValue value) {
		if (AccessFlags.STATIC.isSet(patch.getAccessFlags())) {
			// Use the static field initializer values in patch if and
			// only if the static constructor in patch is being used.
			// This makes behavior predictable across compilers.
			if (resolvedStaticConstructorAction == Action.ADD || resolvedStaticConstructorAction == Action.REPLACE) {
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
	protected Field onReplace(Field patch, PatcherAnnotation annotation, Field target) {
		throw new AssertionError("field replace");
	}

}
