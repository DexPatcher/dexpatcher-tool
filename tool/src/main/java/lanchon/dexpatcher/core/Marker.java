package lanchon.dexpatcher.core;

public enum Marker {

	ADD("DexAdd"),
	EDIT("DexEdit"),
	REPLACE("DexReplace"),
	REMOVE("DexRemove"),
	IGNORE("DexIgnore"),

	TAG("DexTag");

	// Annotations

	private final String className;

	Marker(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
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
