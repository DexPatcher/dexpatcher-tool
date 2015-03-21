package lanchon.dexpatcher;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.immutable.ImmutableField;
import lanchon.dexpatcher.PatcherAnnotation.ParseException;

import static lanchon.dexpatcher.Logger.Level.*;

public class FieldSetPatcher extends MemberSetPatcher<Field> {

	public FieldSetPatcher(Logger logger, String baseLogPrefix, String logMemberType, boolean warnOnImplicitIgnore) {
		super(logger, baseLogPrefix, logMemberType, warnOnImplicitIgnore);
	}

	// Adapters

	@Override
	protected String getId(Field t) {
		return Util.getFieldId(t);
	}

	@Override
	protected PatcherAnnotation getPatcherAnnotation(Field patch) throws ParseException {
		return PatcherAnnotation.parse(patch.getAnnotations());
	}

	@Override
	protected String parsePatcherAnnotation(Field patch, PatcherAnnotation annotation) throws ParseException {
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
		if (annotation == null) {
			return patch;
		} else {
			return new ImmutableField(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getType(),
				patch.getAccessFlags(),
				patch.getInitialValue(),
				annotation.getFilteredAnnotations());
		}
	}

	@Override
	protected Field onEdit(Field patch, PatcherAnnotation annotation, Field target) {

		if (patch.getInitialValue() != null) {
			log(WARN, "ignoring simple field initializer value in patch");
		}

		Field patched = new ImmutableField(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getType(),
				patch.getAccessFlags(),
				target.getInitialValue(),
				annotation.getFilteredAnnotations());

		return super.onEdit(patched, annotation, target);

	}

	@Override
	protected Field onReplace(Field patch, PatcherAnnotation annotation, Field target) {

		if (AccessFlags.STATIC.isSet(patch.getAccessFlags())) {
			if (patch.getInitialValue() == null) {
				log(WARN, "no simple field initializer value found in patch (either the field lacks an initializer or it is embedded in the static class constructor)");
			}
			if (target.getInitialValue() == null) {
				log(WARN, "no simple field initializer value found in target (either the field lacks an initializer or it is embedded in the static class constructor)");
			}
		} else {
			log(ERROR, "cannot replace instance fields (initializers are embedded in the instance constructors); edit the field instead");
		}

		if (AccessFlags.FINAL.isSet(target.getAccessFlags())) {
			log(WARN, "value of final field might be embedded in code");
		}

		return super.onReplace(patch, annotation, target);

	}

}
