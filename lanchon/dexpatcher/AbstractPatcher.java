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

	public Collection<T> run(Iterable<? extends T> sourceSet, Iterable<? extends T> patchSet) {
		final int sizeHint = 16;
		return run(sourceSet, sizeHint, patchSet, sizeHint);
	}

	public Collection<T> run(Iterable<? extends T> sourceSet, int sourceSetSizeHint,
			Iterable<? extends T> patchSet, int patchSetSizeHint) {

		LinkedHashMap<String, T> targetMap = new LinkedHashMap<>(sourceSetSizeHint + patchSetSizeHint);
		LinkedHashMap<String, T> targetedMap = new LinkedHashMap<>();
		LinkedHashMap<String, T> patchedMap = new LinkedHashMap<>(patchSetSizeHint);

		for (T source : sourceSet) {
			targetMap.put(getId(source), source);
		}

		for (T patch : patchSet) {

			String patchId = getId(patch);
			logPrefix = baseLogPrefix + getLogPrefix(patch) + ": ";

			PatcherAnnotation annotation;
			String targetId = null;
			try {
				annotation = PatcherAnnotation.parse(getAnnotations(patch));
				if (annotation == null) annotation = getDefaultAnnotation(patch);
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
				if (targetedMap.put(targetId, target) != null) {
					log(ERROR, "already targeted");
					continue;
				}
			}

			if (logger.isLogging(DEBUG)) log(DEBUG, action.getLabel());
			T patched = null;
			switch (action) {
			case ADD:
				patched = onAdd(patch, annotation);
				break;
			case EDIT:
				patched = onEdit(patch, annotation, target);
				break;
			case REPLACE:
				patched = onReplace(patch, annotation, target);
				break;
			case REMOVE:
				onRemove(patch, annotation, target);
				break;
			case IGNORE:
				break;
			default:
				throw new AssertionError("Unexpected action");
			}

			if (action != Action.REMOVE && action != Action.IGNORE) {
				if (patched == null) throw new AssertionError("Patched is null");
				String patchedId = getId(patched);
				if (!patchId.equals(patchedId)) throw new AssertionError("Patched id changed");
				T previous = patchedMap.put(patchedId, patched);
				if (previous != null) throw new AssertionError("Patched id collision");
			}

		}

		for (T targeted : targetedMap.values()) {
			String id = getId(targeted);
			T patched = patchedMap.get(id);
			if (patched == null) targetMap.remove(id);
			else {
				logPrefix = baseLogPrefix + getLogPrefix(patched) + ": ";
				onEffectiveReplacement(patched, targeted);
				targetMap.put(id, patched);		// keep ordering stable when replacing items
				patchedMap.remove(id);
			}
		}

		for (T patched : patchedMap.values()) {
			if (targetMap.put(getId(patched), patched) != null) {
				logPrefix = baseLogPrefix + getLogPrefix(patched) + ": ";
				log(ERROR, "already exists");
			}
		}

		return targetMap.values();

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

	protected abstract PatcherAnnotation getDefaultAnnotation(T patch);
	protected abstract T onAdd(T patch, PatcherAnnotation annotation);
	protected abstract T onEdit(T patch, PatcherAnnotation annotation, T target);

	protected T onReplace(T patch, PatcherAnnotation annotation, T target) { return onAdd(patch, annotation); }
	protected void onRemove(T patch, PatcherAnnotation annotation, T target) {}
	protected void onEffectiveReplacement(T patched, T original) {}

}
