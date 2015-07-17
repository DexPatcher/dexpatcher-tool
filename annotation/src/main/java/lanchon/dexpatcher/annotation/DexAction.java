package lanchon.dexpatcher.annotation;

@DexIgnore
public enum DexAction {
	ADD,
	EDIT,
	REPLACE,
	REMOVE,
	IGNORE,
	UNDEFINED;
}
