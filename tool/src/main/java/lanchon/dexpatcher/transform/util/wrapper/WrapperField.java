/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.util.wrapper;

import java.util.Set;

import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;

public class WrapperField extends BaseFieldReference implements Field {

	protected final Field wrappedField;

	public WrapperField(Field wrappedField) {
		this.wrappedField = wrappedField;
	}

	@Override
	public String getDefiningClass() {
		return wrappedField.getDefiningClass();
	}

	@Override
	public String getName() {
		return wrappedField.getName();
	}

	@Override
	public String getType() {
		return wrappedField.getType();
	}

	@Override
	public int getAccessFlags() {
		return wrappedField.getAccessFlags();
	}

	@Override
	public EncodedValue getInitialValue() {
		return wrappedField.getInitialValue();
	}

	@Override
	public Set<? extends Annotation> getAnnotations() {
		return wrappedField.getAnnotations();
	}

}