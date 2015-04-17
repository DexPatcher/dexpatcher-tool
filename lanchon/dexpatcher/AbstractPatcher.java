package lanchon.dexpatcher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotation;

import static lanchon.dexpatcher.Logger.Level.*;

public abstract class AbstractPatcher<T> {

	protected final Logger logger;
	protected final String baseLogPrefix;

	private String logPrefix;

	private LinkedHashMap<String, T> targetMap;
	private LinkedHashMap<String, T> targetedMap;
	private LinkedHashMap<String, T> patchedMap;

	protected AbstractPatcher(Logger logger, String baseLogPrefix) {
		this.logger = logger;
		this.baseLogPrefix = baseLogPrefix != null ? baseLogPrefix : "";
	}

	protected final void log(Logger.Level level, String message) {
		logger.log(level, logPrefix + message);
	}

	protected final String getLogPrefix() {
		return logPrefix;
	}

	public Collection<T> process(Iterable<? extends T> sourceSet, Iterable<? extends T> patchSet) {
		final int sizeHint = 16;
		return process(sourceSet, sizeHint, patchSet, sizeHint);
	}

	public Collection<T> process(Iterable<? extends T> sourceSet, int sourceSetSizeHint,
			Iterable<? extends T> patchSet, int patchSetSizeHint) {

		targetMap = new LinkedHashMap<>(sourceSetSizeHint + patchSetSizeHint);
		targetedMap = new LinkedHashMap<>();
		patchedMap = new LinkedHashMap<>(patchSetSizeHint);
		try
		{

			for (T source : sourceSet) {
				targetMap.put(getId(source), source);
			}

			for (T patch : patchSet) {

				String patchId = getId(patch);
				setupLogPrefix(patch);

				PatcherAnnotation annotation;
				String targetId = null;
				try {
					annotation = PatcherAnnotation.parse(getAnnotations(patch));
					if (annotation == null) {
						annotation = new PatcherAnnotation(getDefaultAction(patch), getAnnotations(patch));
					}
					targetId = parsePatcherAnnotation(patch, annotation);
				} catch (PatcherAnnotation.ParseException e) {
					log(ERROR, e.getMessage());
					continue;
				}
				Action action = annotation.getAction();

				if (targetId == null) targetId = patchId;
				else {
					if (!targetId.equals(patchId)) {
						logPrefix += getLogTargetPrefix(annotation, targetId) + ": ";
					}
				}

				T target = null;
				if (action != Action.ADD && action != Action.IGNORE) {
					target = targetMap.get(targetId);
					if (target == null) {
						log(ERROR, "target not found");
						continue;
					}
					if (!markAsTargeted(targetId, target)) continue;
				}

				if (logger.isLogging(DEBUG)) log(DEBUG, action.getLabel());
				switch (action) {
				case ADD:
					addPatched(patchId, patch, onAdd(patch, annotation));
					break;
				case EDIT:
					addPatched(patchId, patch, onEdit(patch, annotation, target));
					break;
				case REPLACE:
					addPatched(patchId, patch, onReplace(patch, annotation, target));
					break;
				case REMOVE:
					onRemove(patch, annotation, target);
					break;
				case IGNORE:
					break;
				default:
					throw new AssertionError("Unexpected action");
				}

			}

			for (T targeted : targetedMap.values()) {
				String id = getId(targeted);
				T patched = patchedMap.get(id);
				if (patched == null) targetMap.remove(id);
				else {
					setupLogPrefix(patched);
					onEffectiveReplacement(patched, targeted);
					targetMap.put(id, patched);		// keep ordering stable when replacing items
					patchedMap.remove(id);
				}
			}

			for (T patched : patchedMap.values()) {
				if (targetMap.put(getId(patched), patched) != null) {
					setupLogPrefix(patched);
					log(ERROR, "already exists");
				}
			}

			return targetMap.values();

		} finally {
			targetMap = null;
			targetedMap = null;
			patchedMap = null;
		}

	}

	private final void setupLogPrefix(T t) {
		logPrefix = baseLogPrefix + getLogPrefix(t) + ": ";
	}

	private final boolean markAsTargeted(String targetId, T target) {
		if (targetedMap.put(targetId, target) == null) return true;
		log(ERROR, "already targeted");
		return false;
	}

	private final void addPatched(String patchId, T patch, T patched) {
		if (patched == null) throw new AssertionError("Null patched");
		String patchedId = getId(patched);
		if (!patchId.equals(patchedId)) throw new AssertionError("Changed patchedId");
		T previous = patchedMap.put(patchedId, patched);
		if (previous != null) throw new AssertionError("Colliding patchedId");
	}

	protected void checkAccessFlags(Logger.Level level, int flags1, int flags2, AccessFlags flags[], String message) {
		for (AccessFlags flag : flags) {
			if (flag.isSet(flags1) != flag.isSet(flags2)) {
				log(level, String.format(message, flag.toString()));
			}
		}
	}

	// Adapters

	// TODO:
	// When this commit ships: https://code.google.com/p/smali/issues/detail?id=237
	// Eliminate: protected abstract Set<? extends Annotation> getAnnotations(T patch);

	protected abstract String getId(T t);
	protected abstract Set<? extends Annotation> getAnnotations(T patch);
	protected abstract String parsePatcherAnnotation(T patch, PatcherAnnotation annotation) throws PatcherAnnotation.ParseException;
	protected abstract String getLogPrefix(T patch);
	protected abstract String getLogTargetPrefix(PatcherAnnotation annotation, String targetId);

	// Handlers

	protected abstract Action getDefaultAction(T patch);
	protected abstract T onAdd(T patch, PatcherAnnotation annotation);
	protected abstract T onEdit(T patch, PatcherAnnotation annotation, T target);

	protected T onReplace(T patch, PatcherAnnotation annotation, T target) { return onAdd(patch, annotation); }
	protected void onRemove(T patch, PatcherAnnotation annotation, T target) {}
	protected void onEffectiveReplacement(T patched, T original) {}

}
