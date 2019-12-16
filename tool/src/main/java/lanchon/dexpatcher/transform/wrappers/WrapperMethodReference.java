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

import java.util.List;

import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class WrapperMethodReference extends BaseMethodReference {

	protected final MethodReference wrappedMethodReference;

	public WrapperMethodReference(MethodReference wrappedMethodReference) {
		this.wrappedMethodReference = wrappedMethodReference;
	}

	@Override
	public String getDefiningClass() {
		return wrappedMethodReference.getDefiningClass();
	}

	@Override
	public String getName() {
		return wrappedMethodReference.getName();
	}

	@Override
	public List<? extends CharSequence> getParameterTypes() {
		return wrappedMethodReference.getParameterTypes();
	}

	@Override
	public String getReturnType() {
		return wrappedMethodReference.getReturnType();
	}

}
