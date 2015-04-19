package lanchon.dexpatcher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jf.dexlib2.AccessFlags;

import static lanchon.dexpatcher.Logger.Level.*;

public abstract class AbstractPatcher<T> {

	private final Logger logger;
	private final String baseLogPrefix;

	private LinkedHashMap<String, T> sourceMap;
	private LinkedHashMap<String, Boolean> targetMap;
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

		sourceMap = new LinkedHashMap<>(sourceSetSizeHint + patchSetSizeHint);
		targetMap = new LinkedHashMap<>();
		patchedMap = new LinkedHashMap<>(patchSetSizeHint);

		try
		{

			for (T source : sourceSet) {
				sourceMap.put(getId(source), source);
			}

			for (T patch : patchSet) {
				String patchId = getId(patch);
				setupLogPrefix(patchId, patch);
				try {
					onPatch(patchId, patch);
				} catch (PatchException e) {
					log(ERROR, e.getMessage());
				}
			}

			for (Entry<String, Boolean> entry : targetMap.entrySet()) {
				String id = entry.getKey();
				boolean editedInPlace = entry.getValue();
				T patched = patchedMap.get(id);
				if (patched == null) {
					T original = sourceMap.remove(id);
					if (original == null) throw new AssertionError("Missing target");
				} else {
					T original = sourceMap.put(id, null);		// keep ordering stable when replacing items
					if (original == null) throw new AssertionError("Missing target");
					setupLogPrefix(id, patched);
					try {
						onEffectiveReplacement(id, patched, original, editedInPlace);
					} catch (PatchException e) {
						log(ERROR, e.getMessage());
					}
				}
			}

			for (Entry<String, T> entry : patchedMap.entrySet()) {
				String patchedId = entry.getKey();
				T patched = entry.getValue();
				if (sourceMap.put(patchedId, patched) != null) {
					setupLogPrefix(patchedId, patched);
					log(ERROR, "already exists");
				}
			}

			return sourceMap.values();

		} finally {

			sourceMap = null;
			targetMap = null;
			patchedMap = null;
			logPrefix = null;

		}

	}

	private final void setupLogPrefix(String id, T t) {
		logPrefix = baseLogPrefix + getLogPrefix(id, t) + ": ";
	}

	protected final void extendLogPrefix(String prefixComponent) {
		logPrefix += prefixComponent + ": ";
	}

	protected final Map<String, T> getSourceMap() {
		return sourceMap;
	}

	protected final T findTarget(String targetId, boolean editingInPlace) throws PatchException {
		T target = sourceMap.get(targetId);
		if (target == null) throw new PatchException("target not found");
		addTarget(targetId, editingInPlace);
		return target;
	}

	protected final void addTarget(String targetId, boolean editingInPlace) throws PatchException {
		if (targetMap.put(targetId, editingInPlace) != null) throw new PatchException("already targeted");
	}

	//private final void addPatched(String patchedId, T patched) throws PatchException {
	//	if (patchedMap.put(patchedId, patched) != null) throw new PatchException("already added");
	//}


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
	protected abstract String getLogPrefix(String id, T t);

	// Handlers

	protected abstract void onPatch(String patchId, T patch) throws PatchException;
	protected void onEffectiveReplacement(String id, T patched, T original, boolean editedInPlace) throws PatchException {}

}
