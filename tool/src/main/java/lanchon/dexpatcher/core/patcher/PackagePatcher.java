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

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.Id;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.core.util.TypeName;

import org.jf.dexlib2.iface.ClassDef;

import static lanchon.dexpatcher.core.PatcherAnnotation.*;
import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class PackagePatcher extends ClassSetPatcher {

	private boolean processingPackage;

	public PackagePatcher(Context context) {
		super(context);
	}

	// Implementation

	@Override
	protected void onPrepare(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		processingPackage = DexUtils.isPackageId(patchId);
		if (!processingPackage) {
			super.onPrepare(patchId, patch, annotation);
			return;
		}
		if (annotation.getTargetClass() != null) throw invalidElement(Marker.ELEM_TARGET_CLASS);
		if (annotation.getContentOnly()) throw invalidElement(Marker.ELEM_CONTENT_ONLY);
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
		String targetId = patchId;
		String target = annotation.getTarget();
		if (target != null) {
			String targetDescriptor;
			if (DexUtils.isClassDescriptor(target)) {
				targetDescriptor = target;
			} else {
				if (target.startsWith(".")) target = target.substring(1);
				if (target.length() != 0) target += '.' + Marker.NAME_PACKAGE_INFO;
				else target = Marker.NAME_PACKAGE_INFO;
				targetDescriptor = TypeName.toClassDescriptor(target);
			}
			targetId = Id.fromClassDescriptor(targetDescriptor);
		}
		if (!DexUtils.isPackageId(targetId)) throw new PatchException("target is not a package");
		if (shouldLogTarget(patchId, targetId)) {
			extendLogPrefixWithTargetLabel(Label.fromClassId(targetId));
		}
		return targetId;
	}

	private void removePackage(String targetId, boolean recursive) throws PatchException {
		int prefixLength = targetId.length() - (Marker.NAME_PACKAGE_INFO.length() + 1);
		String prefix = targetId.substring(0, prefixLength);
		for (String id: getSourceMap().keySet()) {
			if (id.startsWith(prefix) && (recursive || id.indexOf('/', prefixLength) < 0) && id.endsWith(";")) {
				try {
					addTarget(id, false);
					if (isLogging(DEBUG)) log(DEBUG, "remove type '" + Label.fromClassId(id) + "'");
				} catch (PatchException e) {
					log(ERROR, "already targeted type '" + Label.fromClassId(id) + "'");
				}
			}
		}
	}

}
