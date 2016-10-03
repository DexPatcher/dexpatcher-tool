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

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public class StaticFieldSetPatcher extends FieldSetPatcher {

	public StaticFieldSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Implementation

	@Override
	protected String getSetItemLabel() {
		return "static field";
	}

	@Override
	protected void onSimpleRemove(Field patch, PatcherAnnotation annotation, Field target) {
		if (FINAL.isSet(target.getAccessFlags())) {
			log(WARN, "original value of final static field can be embedded in code");
		}
	}

	@Override
	protected EncodedValue filterInitialValue(Field patch, EncodedValue value) {
		// Use the static field initializer values in patch if and
		// only if the static constructor in patch is being used.
		// This makes behavior predictable across compilers.
		if (resolvedStaticConstructorAction == null) {
			log(ERROR, "must define an action for the static constructor of the class");
		} else if (resolvedStaticConstructorAction == Action.ADD || resolvedStaticConstructorAction == Action.REPLACE) {
			value = patch.getInitialValue();
		} else {
			log(WARN, "static field will not be initialized as specified in patch because the static constructor code in patch is being ignored");
		}
		return value;
	}

}
