package lanchon.dexpatcher;

import java.util.ArrayList;
import java.util.Set;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.iface.value.TypeEncodedValue;
import org.jf.dexlib2.immutable.ImmutableAnnotation;

public class PatcherAnnotation {

	@SuppressWarnings("serial")
	public static class ParseException extends Exception {

		public ParseException(String message) {
			super(message);
		}

	}

	public static final String AE_TARGET = "target";
	public static final String AE_TARGET_CLASS = "targetClass";
	public static final String AE_STATIC_CONSTRUCTOR_ACTION = "staticConstructorAction";
	public static final String AE_DEFAULT_ACTION = "defaultAction";

	private static final String CLASS_VOID = Util.getTypeDescriptorFromClass(Void.class);

	// TODO:
	// When this commit ships: https://code.google.com/p/smali/issues/detail?id=237
	// Change to: public static PatcherAnnotation parse(Annotable annotable) throws ParseException {

	public static PatcherAnnotation parse(Set<? extends Annotation> annotations) throws ParseException {

		Annotation annotation = null;
		Action action = null;
		ArrayList<Annotation> filteredAnnotations = new ArrayList<>(annotations.size());
		for (Annotation an : annotations) {
			Action ac = Action.fromAnnotationDescriptor(an.getType());
			if (ac != null) {
				if (action != null) {
					throw new ParseException("conflicting patcher annotations (" +
							action.getAnnotationClassName() + ", " + ac.getAnnotationClassName() + ")");
				}
				action = ac;
				annotation = an;
			} else {
				filteredAnnotations.add(an);
			}
		}

		if (action == null) return null;

		String target = null;
		String targetClass = null;
		Action staticConstructorAction = null;
		Action defaultAction = null;
		for (AnnotationElement element : annotation.getElements()) {
			String name = element.getName();
			EncodedValue value = element.getValue();
			switch (name) {
			case AE_TARGET: {
				if (target != null) break;
				String s = ((StringEncodedValue) value).getValue();
				if (s.length() != 0) target = s;
				continue;
			}
			case AE_TARGET_CLASS: {
				if (targetClass != null) break;
				String s = ((TypeEncodedValue) value).getValue();
				if (!CLASS_VOID.equals(s)) targetClass = s;
				continue;
			}
			case AE_STATIC_CONSTRUCTOR_ACTION: {
				if (staticConstructorAction != null) break;
				String s = ((StringEncodedValue) value).getValue();
				if (s.length() != 0) {
					staticConstructorAction = Action.fromLabel(s);
					if (staticConstructorAction == null) break;
				}
				continue;
			}
			case AE_DEFAULT_ACTION: {
				if (defaultAction != null) break;
				String s = ((StringEncodedValue) value).getValue();
				if (s.length() != 0) {
					defaultAction = Action.fromLabel(s);
					if (defaultAction == null) break;
				}
				continue;
			}
			default:
				break;
			}
			throw new ParseException("invalid patcher annotation element (" + name + ")");
		}

		if (target != null && targetClass != null) {
			throw new ParseException("conflicting patcher annotation elements (" +
					AE_TARGET + ", " + AE_TARGET_CLASS + ")");
		}

		return new PatcherAnnotation(action, target, targetClass, staticConstructorAction,
				defaultAction, ImmutableAnnotation.immutableSetOf(filteredAnnotations));

	}

	private final Action action;
	private final String target;
	private final String targetClass;
	private final Action staticConstructorAction;
	private final Action defaultAction;
	private final Set<? extends Annotation> filteredAnnotations;

	public PatcherAnnotation(Action action, String target, String targetClass,
			Action staticConstructorAction, Action defaultAction,
			Set<? extends Annotation> filteredAnnotations) {
		this.action = action;
		this.target = target;
		this.targetClass = targetClass;
		this.staticConstructorAction = staticConstructorAction;
		this.defaultAction = defaultAction;
		this.filteredAnnotations = filteredAnnotations;
	}

	public PatcherAnnotation(Action action, Set<? extends Annotation> filteredAnnotations) {
		this.action = action;
		this.target = null;
		this.targetClass = null;
		this.staticConstructorAction = null;
		this.defaultAction = null;
		this.filteredAnnotations = filteredAnnotations;
	}

	public Action getAction() {
		return action;
	}

	public String getTarget() {
		return target;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public Action getStaticConstructorAction() {
		return staticConstructorAction;
	}

	public Action getDefaultAction() {
		return defaultAction;
	}

	public Set<? extends Annotation> getFilteredAnnotations() {
		return filteredAnnotations;
	}

}
