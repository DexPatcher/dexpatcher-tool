package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class InstanceFieldSetPatcher extends FieldSetPatcher {

	public InstanceFieldSetPatcher(ClassSetPatcher parent, String logMemberType, PatcherAnnotation annotation) {
		super(parent, logMemberType, annotation);
	}

	// Implementation

	@Override
	protected EncodedValue filterInitialValue(Field patch, EncodedValue value) {
		// Instance fields should never have initializer values.
		if (patch.getInitialValue() != null) {
			log(ERROR, "unexpected instance field initializer value in patch");
		}
		return value;
	}

}
