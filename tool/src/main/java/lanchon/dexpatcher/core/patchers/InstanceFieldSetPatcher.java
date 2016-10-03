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
	protected String getSetItemLabel() {
		return "instance field";
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
