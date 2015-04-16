package lanchon.dexpatcher;

public abstract class Tag {

	private static final String PACKAGE_PREFIX = "lanchon.dexpatcher.annotation.";

	private static final String SIMPLE_CLASS_NAME_PREFIX = "Dex";

	// Annotations

	public static final String ADD = getSimpleClassName("Add");
	public static final String EDIT = getSimpleClassName("Edit");
	public static final String REPLACE = getSimpleClassName("Replace");
	public static final String REMOVE = getSimpleClassName("Remove");
	public static final String IGNORE = getSimpleClassName("Ignore");

	// Annotation Elements

	public static final String ELEM_TARGET = "target";
	public static final String ELEM_TARGET_CLASS = "targetClass";
	public static final String ELEM_STATIC_CONSTRUCTOR_ACTION = "staticConstructorAction";
	public static final String ELEM_DEFAULT_ACTION = "defaultAction";
	public static final String ELEM_ONLY_EDIT_MEMBERS = "onlyEditMembers";

	// Type Descriptors

	private static final String TAG = getSimpleClassName("Tag");

	public static final String TYPE_TAG = getTypeDescriptor(TAG);
	public static final String TYPE_VOID = Util.getTypeDescriptorFromClass(Void.class);

	// Helpers

	private static String getSimpleClassName(String tag) {
		return SIMPLE_CLASS_NAME_PREFIX + tag;
	}

	private static String getClassName(String simpleClassName) {
		return PACKAGE_PREFIX + simpleClassName;
	}

	public static String getTypeDescriptor(String simpleClassName) {
		return Util.getTypeDescriptorFromName(getClassName(simpleClassName));
	}

}
