package lanchon.dexpatcher.core;

public enum Action {

	ADD(Marker.ADD),
	EDIT(Marker.EDIT),
	REPLACE(Marker.REPLACE),
	REMOVE(Marker.REMOVE),
	IGNORE(Marker.IGNORE);

	private final Marker marker;
	private final String label;

	Action(Marker marker) {
		this.marker = marker;
		label = name().toLowerCase();
	}

	public Marker getMarker() {
		return marker;
	}

	public String getLabel() {
		return label;
	}

}
