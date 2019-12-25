/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.util;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class Label {

	public static String fromClassDescriptor(String descriptor) {
		try {
			return TypeName.fromClassDescriptor(descriptor);
		} catch (InvalidTypeDescriptorException e) {
			return "[class-type:" + descriptor + "]";
		}
	}

	public static String fromFieldDescriptor(String descriptor) {
		try {
			return TypeName.fromFieldDescriptor(descriptor);
		} catch (InvalidTypeDescriptorException e) {
			return "[field-type:" + descriptor + "]";
		}
	}

	public static String fromReturnDescriptor(String descriptor) {
		try {
			return TypeName.fromReturnDescriptor(descriptor);
		} catch (InvalidTypeDescriptorException e) {
			return "[return-type:" + descriptor + "]";
		}
	}

	public static String ofClass(ClassDef classDef) {
		return fromClassDescriptor(classDef.getType());
	}

	public static String fromClassId(String id) {
		return fromClassDescriptor(Id.toClassDescriptor(id));
	}

	public static String ofTargetMember(String name) {
		return name;
	}

	public static String ofField(FieldReference field) {
		return ofField(field, field.getName());
	}

	public static String ofField(FieldReference field, String name) {
		return name + ':' + fromFieldDescriptor(field.getType());
	}

	public static String ofMethod(MethodReference method) {
		return ofMethod(method, method.getName());
	}

	public static String ofMethod(MethodReference method, String name) {
		return ofMethod(method.getParameterTypes(), method.getReturnType(), name);
	}

	public static String ofMethod(Iterable<? extends CharSequence> parameterTypes, String returnType, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('(');
		boolean first = true;
		for (CharSequence parameterType : parameterTypes) {
			if (!first) sb.append(", ");
			sb.append(fromFieldDescriptor(parameterType.toString()));
			first = false;
		}
		sb.append("):").append(fromReturnDescriptor(returnType));
		return sb.toString();
	}

	private Label() {}

}
