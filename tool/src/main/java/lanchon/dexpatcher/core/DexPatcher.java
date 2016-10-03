package lanchon.dexpatcher.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import lanchon.dexpatcher.core.model.BasicDexFile;
import lanchon.dexpatcher.core.patchers.PackagePatcher;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class DexPatcher {

	private DexPatcher() {}

	public static DexFile process(Context context, DexFile sourceDex, DexFile patchDex) {
		Set<? extends ClassDef> sourceClasses = sourceDex.getClasses();
		Set<? extends ClassDef> patchClasses = patchDex.getClasses();
		PackagePatcher patcher = new PackagePatcher(context);
		Collection<ClassDef> patchedClasses = patcher.process(
				sourceClasses, sourceClasses.size(), patchClasses, patchClasses.size());
		return new BasicDexFile(
				sourceDex.getOpcodes(),
				Collections.unmodifiableSet(new LinkedHashSet<>(patchedClasses)));
	}

}
