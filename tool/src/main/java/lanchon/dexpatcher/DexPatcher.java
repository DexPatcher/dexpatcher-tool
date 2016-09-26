package lanchon.dexpatcher;

import java.util.Collection;
import java.util.Set;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.immutable.ImmutableDexFile;

public class DexPatcher extends PackagePatcher {

	public DexPatcher(Logger logger) {
		super(logger);
	}

	public DexFile process(DexFile sourceDex, DexFile patchDex) {
		Set<? extends ClassDef> sourceClasses = sourceDex.getClasses();
		Set<? extends ClassDef> patchClasses = patchDex.getClasses();
		Collection<ClassDef> patchedClasses =
				process(sourceClasses, sourceClasses.size(), patchClasses, patchClasses.size());
		return new ImmutableDexFile(sourceDex.getOpcodes(), patchedClasses);
	}

}
