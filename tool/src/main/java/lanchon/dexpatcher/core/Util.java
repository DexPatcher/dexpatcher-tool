/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import lanchon.dexpatcher.core.util.Id;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.IntEncodedValue;

import static org.jf.dexlib2.AccessFlags.*;

public abstract class Util {

	// Access Flags

	public static int getClassAccessFlags(ClassDef classDef) {
		int f = classDef.getAccessFlags();
		for (Annotation a : classDef.getAnnotations()) {
			if (Marker.TYPE_INNER_CLASS.equals(a.getType())) {
				for (AnnotationElement e : a.getElements()) {
					if (Marker.ELEM_ACCESS_FLAGS.equals(e.getName())) {
						EncodedValue v = e.getValue();
						if (v instanceof IntEncodedValue) {
							f |= ((IntEncodedValue) v).getValue();
						}
					}
				}
			}
		}
		return f;
	}

	// Constructors

	public static boolean isStaticConstructor(String methodId, Method method) {
		int bits = CONSTRUCTOR.getValue() | STATIC.getValue();
		int mask = bits;
		return (method.getAccessFlags() & mask) == bits &&
				Id.STATIC_CONSTRUCTOR.equals(methodId);
	}

	public static boolean isInstanceConstructor(String methodId, Method method) {
		int bits = CONSTRUCTOR.getValue();
		int mask = bits | STATIC.getValue();
		return (method.getAccessFlags() & mask) == bits &&
				Marker.NAME_INSTANCE_CONSTRUCTOR.equals(method.getName());
	}

	public static boolean isDefaultConstructor(String methodId, Method method) {
		int bits = CONSTRUCTOR.getValue();
		int mask = bits | STATIC.getValue();
		return (method.getAccessFlags() & mask) == bits &&
				Id.DEFAULT_CONSTRUCTOR.equals(methodId);
	}

}
