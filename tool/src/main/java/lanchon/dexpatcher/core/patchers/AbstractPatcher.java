package lanchon.dexpatcher.core.patchers;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.PatchException;

import org.jf.dexlib2.AccessFlags;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public abstract class AbstractPatcher<T> {

	static class PatchedItem<T> {
		final T patch;
		final T patched;
		PatchedItem(T patch, T patched) {
			this.patch = patch;
			this.patched = patched;
		}
	}

	private final Logger logger;
	private final String baseLogPrefix;

	private LinkedHashMap<String, T> sourceMap;
	private LinkedHashMap<String, Boolean> targetedMap;
	private LinkedHashMap<String, PatchedItem<T>> patchedMap;

	private String logPrefix;

	protected AbstractPatcher(Logger logger) {
		this.logger = logger;
		this.baseLogPrefix = "";
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
		targetedMap = new LinkedHashMap<>();
		patchedMap = new LinkedHashMap<>(patchSetSizeHint);

		try
		{

			for (T source : sourceSet) {
				sourceMap.put(getId(source), source);
			}

			for (T patch : patchSet) {
				String patchId = getId(patch);
				setupLogPrefix(patchId, patch, null);
				try {
					onPatch(patchId, patch);
				} catch (PatchException e) {
					log(ERROR, e.getMessage());
				}
			}

			for (Entry<String, Boolean> entry : targetedMap.entrySet()) {
				String id = entry.getKey();
				boolean inPlaceEdit = entry.getValue();
				PatchedItem<T> patchedItem = patchedMap.get(id);
				if (patchedItem == null) {
					// Source item is being removed.
					T original = sourceMap.remove(id);
					if (original == null) throw new AssertionError("Missing target");
				} else {
					// Source item is being effectively replaced. Note that it could
					// have been targeted by an item other than the one replacing it.
					T original = sourceMap.put(id, null);		// keep ordering stable when replacing items
					if (original == null) throw new AssertionError("Missing target");
					setupLogPrefix(id, patchedItem.patch, patchedItem.patched);
					try {
						onEffectiveReplacement(id, patchedItem.patch, patchedItem.patched, original, inPlaceEdit);
					} catch (PatchException e) {
						log(ERROR, e.getMessage());
					}
				}
			}

			for (Entry<String, PatchedItem<T>> entry : patchedMap.entrySet()) {
				String id = entry.getKey();
				PatchedItem<T> patchedItem = entry.getValue();
				if (sourceMap.put(id, patchedItem.patched) != null) {
					setupLogPrefix(id, patchedItem.patch, patchedItem.patched);
					log(ERROR, "already exists");
				}
			}

			return sourceMap.values();

		} finally {

			sourceMap = null;
			targetedMap = null;
			patchedMap = null;
			logPrefix = null;

		}

	}

	private final void setupLogPrefix(String id, T patch, T patched) {
		logPrefix = baseLogPrefix + getLogPrefix(id, patch, patched) + ": ";
	}

	protected final void extendLogPrefix(String prefixComponent) {
		logPrefix += prefixComponent + ": ";
	}

	protected final Map<String, T> getSourceMap() {
		return sourceMap;
	}

	protected final T findTarget(String targetId, boolean inPlaceEdit) throws PatchException {
		T target = sourceMap.get(targetId);
		if (target == null) throw new PatchException("target not found");
		addTarget(targetId, inPlaceEdit);
		return target;
	}

	protected final void addTarget(String targetId, boolean inPlaceEdit) throws PatchException {
		if (targetedMap.put(targetId, inPlaceEdit) != null) throw new PatchException("already targeted");
	}

	protected final void addPatched(String patchId, T patch, T patched) {
		if (patched == null) throw new AssertionError("Null patched");
		if (!patchId.equals(getId(patched))) throw new AssertionError("Changed patchedId");
		PatchedItem<T> patchedItem = new PatchedItem<>(patch, patched);
		if (patchedMap.put(patchId, patchedItem) != null) throw new AssertionError("Colliding patchedId");
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
	protected abstract String getLogPrefix(String id, T patch, T patched);

	// Handlers

	protected abstract void onPatch(String patchId, T patch) throws PatchException;
	protected void onEffectiveReplacement(String id, T patch, T patched, T original, boolean inPlaceEdit) throws PatchException {}

}
