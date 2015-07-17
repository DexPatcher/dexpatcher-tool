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
	private static final Map<String, Action> typeDescriptorMap;

	static {
		labelMap = new HashMap<>();
		typeDescriptorMap = new HashMap<>();
		for (Action action: Action.values()) {
			labelMap.put(action.getLabel(), action);
			typeDescriptorMap.put(action.getTypeDescriptor(), action);
		}
	}

	public static Action fromLabel(String label) {
		return labelMap.get(label);
	}

	public static Action fromAnnotationDescriptor(String annotationDescriptor) {
		return typeDescriptorMap.get(annotationDescriptor);
	}

	private final String label;
	private final String simpleClassName;
	private final String typeDescriptor;

	Action(String simpleClassName) {
		this.label = name().toLowerCase();
		this.simpleClassName = simpleClassName;
		this.typeDescriptor = Tag.getTypeDescriptor(simpleClassName);
	}

	public String getLabel() {
		return label;
	}

	public String getSimpleClassName() {
		return simpleClassName;
	}

	public String getTypeDescriptor() {
		return typeDescriptor;
	}

}
