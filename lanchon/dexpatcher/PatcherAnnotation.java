package lanchon.dexpatcher;

import java.util.ArrayList;
import java.util.Set;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.BooleanEncodedValue;
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
	public static final String AE_WARN_ON_IMPLICIT_IGNORE = "warnOnImplicitIgnore";

	private static final String CL_VOID = Util.getTypeDescriptorFromClass(Void.class);

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
					throw new ParseException("conflicting patcher annotations (" + action.getAnnotationClass().getSimpleName() +
							", " + ac.getAnnotationClass().getSimpleName() + ")");
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
		boolean warnOnImplicitIgnore = false;
		for (AnnotationElement element : annotation.getElements()) {
			String name = element.getName();
			EncodedValue value = element.getValue();
			switch (name) {
			case AE_TARGET:
				if (target != null) break;
				String t = ((StringEncodedValue) value).getValue();
				if (t.length() != 0) target = t;
				continue;
			case AE_TARGET_CLASS:
				if (targetClass != null) break;
				String tc = ((TypeEncodedValue) value).getValue();
				if (!CL_VOID.equals(tc)) targetClass = tc;
				continue;
			case AE_WARN_ON_IMPLICIT_IGNORE:
				if (warnOnImplicitIgnore) break;
				warnOnImplicitIgnore = ((BooleanEncodedValue) value).getValue();
				continue;
			default:
				break;
			}
			throw new ParseException("invalid patcher annotation element (" + name + ")");
		}

		if (target != null && targetClass != null) {
			throw new ParseException("conflicting patcher annotation elements (" + AE_TARGET + ", " + AE_TARGET_CLASS + ")");
		}

		Set<? extends Annotation> filteredAnnotationSet = ImmutableAnnotation.immutableSetOf(filteredAnnotations);
		return new PatcherAnnotation(action, target, targetClass, warnOnImplicitIgnore, filteredAnnotationSet);

	}

	//private final Annotation annotation;
	private final Action action;
	private final String target;
	private final String targetClass;
	private final boolean warnOnImplicitIgnore;
	private final Set<? extends Annotation> filteredAnnotations;

	private PatcherAnnotation(Action action, String target, String targetClass, boolean warnOnImplicitIgnore,
			Set<? extends Annotation> filteredAnnotations) {
		this.action = action;
		this.target = target;
		this.targetClass = targetClass;
		this.warnOnImplicitIgnore = warnOnImplicitIgnore;
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

	public boolean getWarnOnImplicitIgnore() {
		return warnOnImplicitIgnore;
	}

	public Set<? extends Annotation> getFilteredAnnotations() {
		return filteredAnnotations;
	}

}
