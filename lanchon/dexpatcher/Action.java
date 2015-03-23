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

	ADD(DexAdd.class),
	EDIT(DexEdit.class),
	REPLACE(DexReplace.class),
	REMOVE(DexRemove.class),
	IGNORE(DexIgnore.class);

	private static final Map<String, Action> labelMap;
	private static final Map<String, Action> annotationDescriptorMap;

	static {
		labelMap = new HashMap<>();
		annotationDescriptorMap = new HashMap<>();
		for (Action action: Action.values()) {
			labelMap.put(action.getLabel(), action);
			annotationDescriptorMap.put(action.getAnnotationDescriptor(), action);
		}
	}

	public static Action fromLabel(String label) {
		return labelMap.get(label);
	}

	public static Action fromAnnotationDescriptor(String annotationDescriptor) {
		return annotationDescriptorMap.get(annotationDescriptor);
	}

	private final String label;
	private final String annotationClassName;
	private final String annotationDescriptor;

	Action(Class<? extends Annotation> annotationClass) {
		this.label = name().toLowerCase();
		this.annotationClassName = annotationClass.getSimpleName();
		this.annotationDescriptor = Util.getTypeDescriptorFromClass(annotationClass);
	}

	public String getLabel() {
		return label;
	}

	public String getAnnotationClassName() {
		return annotationClassName;
	}

	public String getAnnotationDescriptor() {
		return annotationDescriptor;
	}

}
