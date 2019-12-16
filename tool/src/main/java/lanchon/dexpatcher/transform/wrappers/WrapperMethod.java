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
import java.util.Set;

import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;

public class WrapperMethod extends BaseMethodReference implements Method {

	protected final Method wrappedMethod;

	public WrapperMethod(Method wrappedMethod) {
		this.wrappedMethod = wrappedMethod;
	}

	@Override
	public String getDefiningClass() {
		return wrappedMethod.getDefiningClass();
	}

	@Override
	public String getName() {
		return wrappedMethod.getName();
	}

	@Override
	public List<? extends MethodParameter> getParameters() {
		return wrappedMethod.getParameters();
	}

	@Override
	public List<? extends CharSequence> getParameterTypes() {
		return wrappedMethod.getParameterTypes();
	}

	@Override
	public String getReturnType() {
		return wrappedMethod.getReturnType();
	}

	@Override
	public int getAccessFlags() {
		return wrappedMethod.getAccessFlags();
	}

	@Override
	public Set<? extends Annotation> getAnnotations() {
		return wrappedMethod.getAnnotations();
	}

	@Override
	public MethodImplementation getImplementation() {
		return wrappedMethod.getImplementation();
	}

}
