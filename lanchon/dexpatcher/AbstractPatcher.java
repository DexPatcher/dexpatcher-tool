package lanchon.dexpatcher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Annotation;

import static lanchon.dexpatcher.Logger.Level.*;

public abstract class AbstractPatcher<T> {

	private final Logger logger;
	private final String baseLogPrefix;

	private LinkedHashMap<String, T> targetMap;
	private LinkedHashMap<String, T> targetedMap;
	private LinkedHashMap<String, T> patchedMap;

	private String logPrefix;

	protected AbstractPatcher(Logger logger, String baseLogPrefix) {
		this.logger = logger;
		this.baseLogPrefix = baseLogPrefix != null ? baseLogPrefix : "";
	}

	protected AbstractPatcher(AbstractPatcher<?> parent) {
		this.logger = parent.logger;
		this.baseLogPrefix = parent.logPrefix;
	}

	protected final void log(Logger.Level level, String message) {
		logger.log(level, logPrefix + message);
	}

	protected final boolean isLogging(Logger.Level level) {
		return logger.isLogging(level);
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
				setupLogPrefix(patch);
				String patchId = getId(patch);
				try {

					Set<? extends Annotation> rawAnnotations = getAnnotations(patch);
					PatcherAnnotation annotation = PatcherAnnotation.parse(rawAnnotations);
					if (annotation == null) annotation = new PatcherAnnotation(getDefaultAction(patch), rawAnnotations);
					Action action = annotation.getAction();
					String targetId = parsePatcherAnnotation(patch, annotation);
					if (targetId == null) targetId = patchId;

					if (isLogging(DEBUG)) log(DEBUG, action.getLabel());
					switch (action) {
					case ADD:
						addPatched(patchId, patch, onAdd(patch, annotation));
						break;
					case EDIT:
						addPatched(patchId, patch, onEdit(patch, annotation, setupTarget(patchId, annotation, targetId)));
						break;
					case REPLACE:
						addPatched(patchId, patch, onReplace(patch, annotation, setupTarget(patchId, annotation, targetId)));
						break;
					case REMOVE:
						onRemove(patch, annotation, setupTarget(patchId, annotation, targetId));
						break;
					case IGNORE:
						break;
					default:
						throw new AssertionError("Unexpected action");
					}

				} catch (PatchException e) {
					log(ERROR, e.getMessage());
//				} finally {
//					patch = null;
//					patchId = null;
				}
			}

			for (T targeted : targetedMap.values()) {
				String id = getId(targeted);
				T patched = patchedMap.get(id);
				if (patched == null) targetMap.remove(id);
				else {
					targetMap.put(id, null);		// keep ordering stable when replacing items
					setupLogPrefix(patched);
					onEffectiveReplacement(patched, targeted);
				}
			}

			for (T patched : patchedMap.values()) {
				String patchedId = getId(patched);
				if (targetMap.put(patchedId, patched) != null) {
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

	private final void extendLogPrefix(String patchId, PatcherAnnotation annotation, String targetId) {
		if (!targetId.equals(patchId)) {
			logPrefix += getLogTargetPrefix(annotation, targetId) + ": ";
		}
	}

	private final T setupTarget(String patchId, PatcherAnnotation annotation, String targetId) throws PatchException {
		extendLogPrefix(patchId, annotation, targetId);
		T target = targetMap.get(targetId);
		if (target == null) throw new PatchException("target not found");
		markAsTargeted(targetId, target);
		return target;
	}

	private final void markAsTargeted(String targetId, T target) throws PatchException {
		if (targetedMap.put(targetId, target) != null) {
			throw new PatchException("already targeted");
		}
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
	protected abstract String parsePatcherAnnotation(T patch, PatcherAnnotation annotation) throws PatchException;
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
