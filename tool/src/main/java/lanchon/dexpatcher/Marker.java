package lanchon.dexpatcher;

public enum Marker {

	ADD("DexAdd"),
	EDIT("DexEdit"),
	REPLACE("DexReplace"),
	REMOVE("DexRemove"),
	IGNORE("DexIgnore"),

	TAG("DexTag");

	// Annotation Package

	private static String packageName;

	public static String getPackageName() {
		return packageName;
	}

	public static void setPackageName(String value) {
		packageName = value;
		for (Marker marker : Marker.values()) {
			String className = packageName + "." + marker.className;
			marker.typeDescriptor = Util.getTypeDescriptorFromName(className);
		}
		Action.setupTypeDescriptors();
	}

	// Annotations

	private final String className;
	private final String label;

	private String typeDescriptor;

	Marker(String className) {
		this.className = className;
		label = name().toLowerCase();
	}

	public String getClassName() {
		return className;
	}

	public String getLabel() {
		return label;
	}

	public String getTypeDescriptor() {
		return typeDescriptor;
	}

	// Annotation Elements

	public static final String ELEM_TARGET = "target";
	public static final String ELEM_TARGET_CLASS = "targetClass";
	public static final String ELEM_STATIC_CONSTRUCTOR_ACTION = "staticConstructorAction";
	public static final String ELEM_DEFAULT_ACTION = "defaultAction";
	public static final String ELEM_ONLY_EDIT_MEMBERS = "onlyEditMembers";
	public static final String ELEM_RECURSIVE = "recursive";

	// Dalvik

	public static final String TYPE_VOID = Util.getTypeDescriptorFromClass(Void.class);
	private static final String NAME_INNER_CLASS = "dalvik.annotation.InnerClass";
	public static final String TYPE_INNER_CLASS = Util.getTypeDescriptorFromName(NAME_INNER_CLASS);
	public static final String ELEM_ACCESS_FLAGS = "accessFlags";
	public static final String SIGN_STATIC_CONSTRUCTOR = "<clinit>()V";
	public static final String PACKAGE_INFO = "package-info";
	
}
