package lanchon.dexpatcher;

import java.util.Set;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.immutable.ImmutableDexFile;

public class DexPatcher extends ClassSetPatcher {

	public DexPatcher(Logger logger) {
		super(logger);
	}

	public DexFile process(DexFile sourceDex, DexFile patchDex) {
		Set<? extends ClassDef> sourceClasses = sourceDex.getClasses();
		Set<? extends ClassDef> patchClasses = patchDex.getClasses();
		return new ImmutableDexFile(process(sourceClasses, sourceClasses.size(), patchClasses, patchClasses.size()));
	}

}
