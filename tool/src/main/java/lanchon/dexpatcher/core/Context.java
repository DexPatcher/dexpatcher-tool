package lanchon.dexpatcher.core;

import lanchon.dexpatcher.core.logger.Logger;

import java.util.HashMap;
import java.util.Map;

public class Context {

	private Logger logger;

	private String annotationPackageName;
	private Map<String, Action> actionMap;
	private String tagTypeDescriptor;

	// Logger

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	// Annotations

	public String getAnnotationPackageName() {
		return annotationPackageName;
	}

	public void setAnnotationPackageName(String value) {
		annotationPackageName = value;
		Action[] actions = Action.values();
		actionMap = new HashMap<>(actions.length);
		for (Action action: actions) {
			actionMap.put(getTypeDescriptor(action.getMarker()), action);
		}
		tagTypeDescriptor = getTypeDescriptor(Marker.TAG);
	}

	private String getTypeDescriptor(Marker marker) {
		return Util.getTypeDescriptorFromName(annotationPackageName + "." + marker.getClassName());
	}

	public Action getActionFromTypeDescriptor(String typeDescriptor) {
		return actionMap.get(typeDescriptor);
	}

	public String getTagTypeDescriptor() {
		return tagTypeDescriptor;
	}

}
