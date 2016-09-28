package lanchon.dexpatcher;

import java.util.regex.Pattern;

import org.jf.dexlib2.iface.ClassDef;

import static lanchon.dexpatcher.Logger.Level.*;

public class PackagePatcher extends ClassSetPatcher {

	private static final String PACKAGE_SUFFIX = Tag.PACKAGE_INFO + ';';
	private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?s)L(.*/)?" + Pattern.quote(Tag.PACKAGE_INFO) + ';');

	private static boolean isPackage(String patchId) {
		return patchId.endsWith(PACKAGE_SUFFIX) && PACKAGE_PATTERN.matcher(patchId).matches();
	}

	private boolean processingPackage;

	public PackagePatcher(Logger logger) {
		super(logger);
	}

	// Handlers

	@Override
	protected void onPrepare(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		processingPackage = isPackage(patchId);
		if (!processingPackage) {
			super.onPrepare(patchId, patch, annotation);
			return;
		}
		if (annotation.getTargetClass() != null) PatcherAnnotation.throwInvalidElement(Tag.ELEM_TARGET_CLASS);
	}

	@Override
	protected void onReplace(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		if (!processingPackage) {
			super.onReplace(patchId, patch, annotation);
			return;
		}
		String targetId = getPackageTargetId(patchId, patch, annotation);
		boolean recursive = annotation.getRecursive();
		if (isLogging(DEBUG)) log(DEBUG, recursive ? "replace package recursive" :  "replace package non-recursive");
		removePackage(targetId, recursive);
		ClassDef patched = onSimpleAdd(patch, annotation);
		addPatched(patchId, patch, patched);
	}

	@Override
	protected void onRemove(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		if (!processingPackage) {
			super.onRemove(patchId, patch, annotation);
			return;
		}
		String targetId = getPackageTargetId(patchId, patch, annotation);
		boolean recursive = annotation.getRecursive();
		if (isLogging(DEBUG)) log(DEBUG, recursive ? "remove package recursive" :  "remove package non-recursive");
		removePackage(targetId, recursive);
	}

	private String getPackageTargetId(String patchId, ClassDef patch, PatcherAnnotation annotation) throws PatchException {
		String target = annotation.getTarget();
		String targetId;
		if (target != null) {
			if (Util.isTypeDescriptor(target)) {
				targetId = target;
			} else {
				// Target cannot be an empty string.
				targetId = Util.getTypeDescriptorFromName(target + '.' + Tag.PACKAGE_INFO);
			}
		} else {
			targetId = patchId;
		}
		if (!isPackage(targetId)) throw new PatchException("target is not a package");
		setTargetLogPrefix(patchId, targetId);
		return targetId;
	}

	private void removePackage(String targetId, boolean recursive) throws PatchException {
		String prefix = targetId.substring(0, targetId.length() - PACKAGE_SUFFIX.length());
		Pattern pattern = Pattern.compile("(?s)" + Pattern.quote(prefix) + (recursive ? ".*;" : "[^/]*;"));
		for(String id : getSourceMap().keySet()) {
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
