package lanchon.dexpatcher.annotation;

@DexIgnore
public final class DexTarget {
	public static final String STATIC_CONSTRUCTOR= "<clinit>";
	public static final String CONSTRUCTOR = "<init>";
	private DexTarget() {}
}
