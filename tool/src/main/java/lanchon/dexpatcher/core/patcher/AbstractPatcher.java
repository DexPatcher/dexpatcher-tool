/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patcher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.logger.Logger;

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

	private final Context context;
	private final Logger logger;
	private final String baseLogPrefix;

	private String logPrefix;

	private LinkedHashMap<String, T> sourceMap;
	private LinkedHashMap<String, Boolean> targetedMap;
	private LinkedHashMap<String, PatchedItem<T>> patchedMap;

	protected AbstractPatcher(Context context) {
		this.context = context;
		logger = context.getLogger();
		baseLogPrefix = "";
		logPrefix = baseLogPrefix;
	}

	protected AbstractPatcher(AbstractPatcher<?> parent) {
		context = parent.context;
		logger = parent.logger;
		baseLogPrefix = parent.logPrefix;
		logPrefix = baseLogPrefix;
	}

	// Logging

	protected void clearLogPrefix() {
		logPrefix = baseLogPrefix;
	}

	protected final void setupLogPrefix(String prefix) {
		logPrefix = baseLogPrefix + prefix + ": ";
	}

	protected final void extendLogPrefix(String prefixComponent) {
		logPrefix += prefixComponent + ": ";
	}

	protected void log(Logger.Level level, String message) {
		logger.log(level, logPrefix + message);
	}

	protected final boolean isLogging(Logger.Level level) {
		return logger.isLogging(level);
	}

	// Implementation

	public final Collection<T> process(Iterable<? extends T> sourceSet, Iterable<? extends T> patchSet) {
		final int sizeHint = 16;
		return process(sourceSet, sizeHint, patchSet, sizeHint);
	}

	public Collection<T> process(Iterable<? extends T> sourceSet, int sourceSetSizeHint,
			Iterable<? extends T> patchSet, int patchSetSizeHint) {

		sourceMap = new LinkedHashMap<>(sourceSetSizeHint + patchSetSizeHint);
		targetedMap = new LinkedHashMap<>();
		patchedMap = new LinkedHashMap<>(patchSetSizeHint);

		try {

			for (T source : sourceSet) {
				String sourceId = getId(source);
				if (sourceMap.put(sourceId, source) != null) {
					setupLogPrefix(sourceId, source, null, null);
					log(ERROR, "duplicate found in source");
				}
			}

			for (T patch : patchSet) {
				String patchId = getId(patch);
				setupLogPrefix(patchId, patch, patch, null);
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
					setupLogPrefix(id, patchedItem.patch, patchedItem.patch, patchedItem.patched);
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
					setupLogPrefix(id, patchedItem.patch, patchedItem.patch, patchedItem.patched);
					log(ERROR, "already exists");
				}
			}

			return sourceMap.values();

		} finally {

			sourceMap = null;
			targetedMap = null;
			patchedMap = null;
			clearLogPrefix();

		}

	}

	protected final Context getContext() {
		return context;
	}

	protected final Map<String, T> getSourceMap() {
		return sourceMap;
	}

	protected final boolean targetExists(String targetId) {
		return sourceMap.get(targetId) != null;
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

	protected final void addPatched(T patch, T patched) throws PatchException {
		String id = getId(patched);
		PatchedItem<T> patchedItem = new PatchedItem<>(patch, patched);
		if (patchedMap.put(id, patchedItem) != null) throw new PatchException("already injected");
	}

	// Handlers

	protected abstract String getId(T item);
	protected abstract void setupLogPrefix(String id, T item, T patch, T patched);

	protected abstract void onPatch(String patchId, T patch) throws PatchException;
	protected void onEffectiveReplacement(String id, T patch, T patched, T original, boolean inPlaceEdit) throws PatchException {}

}
