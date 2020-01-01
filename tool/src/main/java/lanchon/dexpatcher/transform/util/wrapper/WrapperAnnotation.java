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

import org.jf.dexlib2.base.BaseAnnotation;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;

public class WrapperAnnotation extends BaseAnnotation {

	protected final Annotation wrappedAnnotation;

	public WrapperAnnotation(Annotation wrappedAnnotation) {
		this.wrappedAnnotation = wrappedAnnotation;
	}

	@Override
	public int getVisibility() {
		return wrappedAnnotation.getVisibility();
	}

	@Override
	public String getType() {
		return wrappedAnnotation.getType();
	}

	@Override
	public Set<? extends AnnotationElement> getElements() {
		return wrappedAnnotation.getElements();
	}

}