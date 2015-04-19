package lanchon.dexpatcher;

public abstract class Tag {

	private static final String PACKAGE_PREFIX = "lanchon.dexpatcher.annotation.";
	private static final String CLASS_PREFIX = "Dex";

	// Annotations

	public static final String ADD = getSimpleClassName("Add");
	public static final String EDIT = getSimpleClassName("Edit");
	public static final String REPLACE = getSimpleClassName("Replace");
	public static final String REMOVE = getSimpleClassName("Remove");
	public static final String IGNORE = getSimpleClassName("Ignore");

	private static final String TAG = getSimpleClassName("Tag");

	// Annotation Elements

	public static final String ELEM_TARGET = "target";
	public static final String ELEM_TARGET_CLASS = "targetClass";
	public static final String ELEM_STATIC_CONSTRUCTOR_ACTION = "staticConstructorAction";
	public static final String ELEM_DEFAULT_ACTION = "defaultAction";
	public static final String ELEM_ONLY_EDIT_MEMBERS = "onlyEditMembers";
	public static final String ELEM_RECURSIVE = "recursive";

	// Type Descriptors

	public static final String TYPE_TAG = getTypeDescriptor(TAG);
	public static final String TYPE_VOID = Util.getTypeDescriptorFromClass(Void.class);

	// Dalvik

	private static final String NAME_INNER_CLASS = "dalvik.annotation.InnerClass";

	public static final String TYPE_INNER_CLASS = Util.getTypeDescriptorFromName(NAME_INNER_CLASS);
	public static final String ELEM_ACCESS_FLAGS = "accessFlags";
	public static final String SIGN_STATIC_CONSTRUCTOR = "<clinit>()V";
	public static final String PACKAGE_INFO = "package-info";

	// Helpers

	private static String getSimpleClassName(String tag) {
		return CLASS_PREFIX + tag;
	}

	private static String getClassName(String simpleClassName) {
		return PACKAGE_PREFIX + simpleClassName;
	}

	public static String getTypeDescriptor(String simpleClassName) {
		return Util.getTypeDescriptorFromName(getClassName(simpleClassName));
	}

}
