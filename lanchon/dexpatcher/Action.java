package lanchon.dexpatcher;

import java.util.HashMap;
import java.util.Map;

public enum Action {

	ADD(Tag.ADD),
	EDIT(Tag.EDIT),
	REPLACE(Tag.REPLACE),
	REMOVE(Tag.REMOVE),
	IGNORE(Tag.IGNORE);

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

	Action(String annotationClassName) {
		this.label = name().toLowerCase();
		this.annotationClassName = annotationClassName;
		this.annotationDescriptor = Tag.getTypeDescriptor(annotationClassName);
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
