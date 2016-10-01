package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class InstanceFieldSetPatcher extends FieldSetPatcher {

	public InstanceFieldSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Implementation

	@Override
	protected void setupLogPrefix(String id, Field patch, Field patched) {
		setupLogPrefix("instance field '" + id + "'");
	}

	@Override
	protected EncodedValue filterInitialValue(Field patch, EncodedValue value) {
		// Instance fields should never have initializer values.
		if (patch.getInitialValue() != null) {
			log(ERROR, "unexpected instance field initializer value in patch");
		}
		return value;
	}

}
