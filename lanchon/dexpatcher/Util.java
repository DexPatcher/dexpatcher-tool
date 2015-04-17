package lanchon.dexpatcher;

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

	public static String getTypeNameFromDescriptor(String descriptor) {
		int l = descriptor.length();
		if (l < 2 || descriptor.charAt(0) != 'L' || descriptor.charAt(l - 1) != ';') {
			throw new RuntimeException("Invalid type descriptor (" + descriptor + ")");
		}
		StringBuilder sb = new StringBuilder(l - 2);
		for (int i = 1; i < l - 1; i++) {
			char c = descriptor.charAt(i);
			if (c == '/') c = '.';
			sb.append(c);
		}
		return sb.toString();
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

	public static int getClassAccessFlags(ClassDef t) {
		int f = t.getAccessFlags();
		for (Annotation a : t.getAnnotations()) {
			if (Tag.TYPE_INNER_CLASS.equals(a.getType())) {
				for (AnnotationElement e : a.getElements()) {
					if (Tag.ELEM_ACCESS_FLAGS.equals(e.getName())) {
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
