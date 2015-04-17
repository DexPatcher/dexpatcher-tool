package lanchon.dexpatcher;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.jf.dexlib2.AccessFlags;

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
				String patchId = getId(patch);
				setupLogPrefix(patch);
				try {
					onPatch(patchId, patch);
				} catch (PatchException e) {
					log(ERROR, e.getMessage());
				}
			}

			for (T targeted : targetedMap.values()) {
				String id = getId(targeted);
				T patched = patchedMap.get(id);
				if (patched == null) targetMap.remove(id);
				else {
					targetMap.put(id, null);		// keep ordering stable when replacing items
					setupLogPrefix(patched);
					try {
						onEffectiveReplacement(id, patched, targeted);
					} catch (PatchException e) {
						log(ERROR, e.getMessage());
					}
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
			logPrefix = null;

		}

	}

	private final void setupLogPrefix(T t) {
		logPrefix = baseLogPrefix + getLogPrefix(t) + ": ";
	}

	protected final void extendLogPrefix(String prefixComponent) {
		logPrefix += prefixComponent + ": ";
	}

	protected final T findTarget(String targetId) throws PatchException {
		T target = targetMap.get(targetId);
		if (target == null) throw new PatchException("target not found");
		addTargeted(targetId, target);
		return target;
	}

	protected final void addTargeted(String targetId, T target) throws PatchException {
		if (targetedMap.put(targetId, target) != null) throw new PatchException("already targeted");
	}

	protected final void addPatched(String patchedId, T patched) throws PatchException {
		if (patchedMap.put(patchedId, patched) != null) throw new PatchException("already added");
	}


	protected final void addPatched(String patchId, T patch, T patched) {
		if (patched == null) throw new AssertionError("Null patched");
		String patchedId = getId(patched);
		if (!patchId.equals(patchedId)) throw new AssertionError("Changed patchedId");
		if (patchedMap.put(patchedId, patched) != null) throw new AssertionError("Colliding patchedId");
	}

	protected void checkAccessFlags(Logger.Level level, int flags1, int flags2, AccessFlags flags[], String message) {
		for (AccessFlags flag : flags) {
			if (flag.isSet(flags1) != flag.isSet(flags2)) {
				log(level, String.format(message, flag.toString()));
			}
		}
	}

	// Adapters

	protected abstract String getId(T t);
	protected abstract String getLogPrefix(T patch);

	// Handlers

	protected abstract void onPatch(String patchId, T patch) throws PatchException;
	protected void onEffectiveReplacement(String id, T patched, T original) throws PatchException {}

}
