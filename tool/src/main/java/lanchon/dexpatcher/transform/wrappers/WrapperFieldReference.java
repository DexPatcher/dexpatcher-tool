/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.wrappers;

import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.iface.reference.FieldReference;

public class WrapperFieldReference extends BaseFieldReference {

	protected final FieldReference wrappedFieldReference;

	public WrapperFieldReference(FieldReference wrappedFieldReference) {
		this.wrappedFieldReference = wrappedFieldReference;
	}

	@Override
	public String getDefiningClass() {
		return wrappedFieldReference.getDefiningClass();
	}

	@Override
	public String getName() {
		return wrappedFieldReference.getName();
	}

	@Override
	public String getType() {
		return wrappedFieldReference.getType();
	}

}
