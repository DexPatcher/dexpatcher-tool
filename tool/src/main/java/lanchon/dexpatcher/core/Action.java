package lanchon.dexpatcher.core;

import java.util.HashMap;
import java.util.Map;

public enum Action {

	ADD(Marker.ADD),
	EDIT(Marker.EDIT),
	REPLACE(Marker.REPLACE),
	REMOVE(Marker.REMOVE),
	IGNORE(Marker.IGNORE);

	private static Map<String, Action> markerTypeDescriptorMap;

	public static void setupTypeDescriptors() {
		Action[] actions = Action.values();
		markerTypeDescriptorMap = new HashMap<>(actions.length);
		for (Action action: actions) {
			markerTypeDescriptorMap.put(action.getMarker().getTypeDescriptor(), action);
		}
	}

	public static Action fromMarkerTypeDescriptor(String markerTypeDescriptor) {
		return markerTypeDescriptorMap.get(markerTypeDescriptor);
	}

	private final Marker marker;

	Action(Marker marker) {
		this.marker = marker;
	}

	public Marker getMarker() {
		return marker;
	}

}
