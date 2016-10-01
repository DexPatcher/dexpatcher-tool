package lanchon.dexpatcher.core;

import lanchon.dexpatcher.core.logger.Logger;

import java.util.HashMap;
import java.util.Map;

public class Context {

	private Logger logger;
	private String annotationPackage;
	private String sourceCodeRoot;

	private Map<String, Action> actionMap;
	private String tagTypeDescriptor;

	// Properties

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getAnnotationPackage() {
		return annotationPackage;
	}

	public void setAnnotationPackage(String value) {
		annotationPackage = value;
		Action[] actions = Action.values();
		actionMap = new HashMap<>(actions.length);
		for (Action action: actions) {
			actionMap.put(getTypeDescriptor(action.getMarker()), action);
		}
		tagTypeDescriptor = getTypeDescriptor(Marker.TAG);
	}

	public String getSourceCodeRoot() {
		return sourceCodeRoot;
	}

	public void setSourceCodeRoot(String sourceCodeRoot) {
		this.sourceCodeRoot = sourceCodeRoot;
	}

	// Extras

	private String getTypeDescriptor(Marker marker) {
		return Util.getTypeDescriptorFromName(annotationPackage + "." + marker.getClassName());
	}

	public Action getActionFromTypeDescriptor(String typeDescriptor) {
		return actionMap.get(typeDescriptor);
	}

	public String getTagTypeDescriptor() {
		return tagTypeDescriptor;
	}

}
