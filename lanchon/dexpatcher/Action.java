package lanchon.dexpatcher;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import lanchon.dexpatcher.annotation.DexAdd;
import lanchon.dexpatcher.annotation.DexEdit;
import lanchon.dexpatcher.annotation.DexIgnore;
import lanchon.dexpatcher.annotation.DexRemove;
import lanchon.dexpatcher.annotation.DexReplace;

public enum Action {
	ADD(DexAdd.class, "add"),
	EDIT(DexEdit.class, "edit"),
	REPLACE(DexReplace.class, "replace"),
	REMOVE(DexRemove.class, "remove"),
	IGNORE(DexIgnore.class, "ignore");

	private static final Map<String, Action> annotationDescriptorMap;

	static {
		annotationDescriptorMap = new HashMap<>();
		for (Action action: Action.values()) {
			annotationDescriptorMap.put(action.annotationDescriptor, action);
		}
	}

	public static Action fromAnnotationDescriptor(String annotationDescriptor) {
		return annotationDescriptorMap.get(annotationDescriptor);
	}

	private final Class<? extends Annotation> annotationClass;
	private final String annotationDescriptor;
	private final String label;

	Action(Class<? extends Annotation> annotationClass, String label) {
		this.annotationClass = annotationClass;
		this.annotationDescriptor = Util.getTypeDescriptorFromClass(annotationClass);
		this.label = label;
	}

	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	public String getAnnotationDescriptor() {
		return annotationDescriptor;
	}

	public String getLabel() {
		return label;
	}
}
