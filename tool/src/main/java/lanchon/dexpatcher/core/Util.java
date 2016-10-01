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
		// TODO: Catch invalid type descriptor exceptions in client code.
		if (descriptor.length() == 0) throwInvalidTypeDescriptor(descriptor);
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
			default:  throw throwInvalidTypeDescriptor(descriptor);
		}
	}

	public static String getLongTypeNameFromDescriptor(String descriptor) {
		if (!isLongTypeDescriptor(descriptor)) throwInvalidTypeDescriptor(descriptor);
		int l = descriptor.length();
		StringBuilder sb = new StringBuilder(l - 2);
		for (int i = 1; i < l - 1; i++) {
			char c = descriptor.charAt(i);
			sb.append(c == '/' ? '.' : c);
		}
		return sb.toString();
	}

	private static RuntimeException throwInvalidTypeDescriptor(String descriptor) {
		throw new RuntimeException("Invalid type descriptor (" + descriptor + ")");
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

	// Access Flags

	public static int getClassAccessFlags(ClassDef t) {
		int f = t.getAccessFlags();
		for (Annotation a : t.getAnnotations()) {
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

}
