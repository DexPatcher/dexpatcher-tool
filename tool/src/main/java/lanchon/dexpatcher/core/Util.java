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

import java.util.List;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.IntEncodedValue;
import org.jf.dexlib2.util.TypeUtils;

import static org.jf.dexlib2.AccessFlags.*;

public abstract class Util {

	// Type Descriptors

	public static String getTypeDescriptorFromClass(Class<?> c) {
		return getTypeDescriptorFromName(c.getName());
	}

	public static String getTypeDescriptorFromName(String name) {
		int l = name.length();
		StringBuilder sb = new StringBuilder(l + 2);
		sb.append('L');
		for (int i = 0; i < l; i++) {
			char c = name.charAt(i);
			if (c == '.') c = '/';
			sb.append(c);
		}
		sb.append(';');
		return sb.toString();
	}

	public static boolean isLongTypeDescriptor(String descriptor) {
		int l = descriptor.length();
		return l >= 2 && descriptor.charAt(l - 1) == ';' && descriptor.charAt(0) == 'L';
	}

	// Type Names

	public static String getTypeNameFromDescriptor(String descriptor) {
		// Void is only valid for return types.
		return "V".equals(descriptor) ? "void" : getFieldTypeNameFromDescriptor(descriptor);
	}

	public static String getFieldTypeNameFromDescriptor(String descriptor) {
		if (descriptor.length() == 0) throw invalidTypeDescriptor(descriptor);
		switch (descriptor.charAt(0)) {
			case '[': return getTypeNameFromDescriptor(descriptor.substring(1)) + "[]";
			case 'L': return getLongTypeNameFromDescriptor(descriptor);
			case 'Z': return "boolean";
			case 'B': return "byte";
			case 'S': return "short";
			case 'C': return "char";
			case 'I': return "int";
			case 'J': return "long";
			case 'F': return "float";
			case 'D': return "double";
			default:  throw invalidTypeDescriptor(descriptor);
		}
	}

	public static String getLongTypeNameFromDescriptor(String descriptor) {
		// TODO: Catch invalid type descriptor exceptions in client code.
		if (!isLongTypeDescriptor(descriptor)) throw invalidTypeDescriptor(descriptor);
		int l = descriptor.length();
		StringBuilder sb = new StringBuilder(l - 2);
		for (int i = 1; i < l - 1; i++) {
			char c = descriptor.charAt(i);
			sb.append(c == '/' ? '.' : c);
		}
		return sb.toString();
	}

	private static RuntimeException invalidTypeDescriptor(String descriptor) {
		return new RuntimeException("Invalid type descriptor (" + descriptor + ")");
	}

	public static String resolveTypeName(String name, String base) {
		if (name.indexOf('.') == -1) {			// if name is not a fully qualified name
			int i = base.lastIndexOf('.');
			if (name.indexOf('$') == -1) {		// if name is not a qualified nested type
				i = Math.max(i, base.lastIndexOf('$'));
			}
			if (i != -1) name = base.substring(0, i + 1) + name;
		}
		return name;
	}

	// IDs

	public static String getTypeId(ClassDef classDef) {
		return classDef.getType();
	}

	public static String getTypeIdFromName(String name) {
		return getTypeDescriptorFromName(name);
	}

	public static String getFieldId(Field field) {
		return getFieldId(field, field.getName());
	}

	public static String getFieldId(Field field, String name) {
		return name + ':' + field.getType();
	}

	public static String getMethodId(Method method) {
		return getMethodId(method, method.getName());
	}

	public static String getMethodId(Method method, String name) {
		return getMethodId(method.getParameters(), method.getReturnType(), name);
	}

	public static String getMethodId(List<? extends MethodParameter> parameters, String returnType, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('(');
		for (MethodParameter p : parameters) sb.append(p.getType());
		sb.append(')').append(returnType);
		return sb.toString();
	}

	// Labels

	public static String getTypeLabel(ClassDef classDef) {
		return getLongTypeNameFromDescriptor(classDef.getType());
	}

	public static String getTypeLabelFromId(String id) {
		return getLongTypeNameFromDescriptor(id);
	}

	public static String getMemberShortLabel(String name) {
		return name;
	}

	public static String getFieldLabel(Field field) {
		return getFieldLabel(field, field.getName());
	}

	public static String getFieldLabel(Field field, String name) {
		return name + ':' + getFieldTypeNameFromDescriptor(field.getType());
	}

	public static String getMethodLabel(Method method) {
		return getMethodLabel(method, method.getName());
	}

	public static String getMethodLabel(Method method, String name) {
		return getMethodLabel(method.getParameters(), method.getReturnType(), name);
	}

	public static String getMethodLabel(List<? extends MethodParameter> parameters, String returnType, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('(');
		boolean first = true;
		for (MethodParameter p : parameters) {
			if (!first) sb.append(", ");
			sb.append(getFieldTypeNameFromDescriptor(p.getType()));
			first = false;
		}
		sb.append("):").append(getTypeNameFromDescriptor(returnType));
		return sb.toString();
	}

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
		return Marker.SIGN_STATIC_CONSTRUCTOR.equals(methodId) && STATIC.isSet(method.getAccessFlags());
	}

	public static boolean isInstanceConstructor(String methodId, Method method) {
		return Marker.NAME_INSTANCE_CONSTRUCTOR.equals(method.getName()) && CONSTRUCTOR.isSet(method.getAccessFlags());
	}

	public static boolean isDefaultConstructor(String methodId, Method method) {
		return Marker.SIGN_DEFAULT_CONSTRUCTOR.equals(methodId) && CONSTRUCTOR.isSet(method.getAccessFlags());
	}

}
