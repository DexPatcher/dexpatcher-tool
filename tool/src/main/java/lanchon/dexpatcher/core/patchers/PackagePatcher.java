/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patchers;

import java.util.regex.Pattern;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.Util;

import org.jf.dexlib2.iface.ClassDef;

import static lanchon.dexpatcher.core.PatcherAnnotation.*;
import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class PackagePatcher extends ClassSetPatcher {

	private static final String PACKAGE_SUFFIX = Marker.PACKAGE_INFO + ';';
	private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?s)L(.*/)?" + Pattern.quote(Marker.PACKAGE_INFO) + ';');

	private static boolean isPackage(String patchId) {
		return patchId.endsWith(PACKAGE_SUFFIX) && PACKAGE_PATTERN.matcher(patchId).matches();
	}

	private boolean processingPackage;

	public PackagePatcher(Context context) {
		super(context);
	}

	// Implementation

	@Override
	protected void onPrepare(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		processingPackage = isPackage(patchId);
		if (!processingPackage) {
			super.onPrepare(patchId, patch, annotation);
			return;
		}
		if (annotation.getTargetClass() != null) throw invalidElement(Marker.ELEM_TARGET_CLASS);
	}

	@Override
	protected void onReplace(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		if (!processingPackage) {
			super.onReplace(patchId, patch, annotation);
			return;
		}
		String targetId = getPackageTargetId(patchId, patch, annotation);
		boolean recursive = annotation.getRecursive();
		if (isLogging(DEBUG)) log(DEBUG, recursive ? "replace package recursive" : "replace package non-recursive");
		removePackage(targetId, recursive);
		ClassDef patched = onSimpleAdd(patch, annotation);
		addPatched(patch, patched);
	}

	@Override
	protected void onRemove(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		if (!processingPackage) {
			super.onRemove(patchId, patch, annotation);
			return;
		}
		String targetId = getPackageTargetId(patchId, patch, annotation);
		boolean recursive = annotation.getRecursive();
		if (isLogging(DEBUG)) log(DEBUG, recursive ? "remove package recursive" : "remove package non-recursive");
		removePackage(targetId, recursive);
	}

	private String getPackageTargetId(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		String target = annotation.getTarget();
		String targetId;
		if (target != null) {
			if (Util.isLongTypeDescriptor(target)) {
				targetId = target;
			} else {
				// Target cannot be an empty string.
				targetId = Util.getTypeDescriptorFromName(target + '.' + Marker.PACKAGE_INFO);
			}
		} else {
			targetId = patchId;
		}
		if (!isPackage(targetId)) throw new PatchException("target is not a package");
		if (shouldLogTarget(patchId, targetId)) {
			extendLogPrefixWithTargetLabel(Util.getTypeLabelFromId(targetId));
		}
		return targetId;
	}

	private void removePackage(String targetId, boolean recursive) throws PatchException {
		String prefix = targetId.substring(0, targetId.length() - PACKAGE_SUFFIX.length());
		Pattern pattern = Pattern.compile("(?s)" + Pattern.quote(prefix) + (recursive ? ".*;" : "[^/]*;"));
		for (String id: getSourceMap().keySet()) {
			if (id.startsWith(prefix) && pattern.matcher(id).matches()) {
				try {
					addTarget(id, false);
					if (isLogging(DEBUG)) log(DEBUG, "remove type '" + id + "'");
				} catch (PatchException e) {
					log(ERROR, "already targeted type '" + id + "'");
				}
			}
		}
	}

}
