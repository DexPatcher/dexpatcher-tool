package lanchon.dexpatcher.core.model;

import java.util.Set;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class BasicDexFile implements DexFile {

	private final Opcodes opcodes;
	private final Set<? extends ClassDef> classes;

	public BasicDexFile(
			Opcodes opcodes,
			Set<? extends ClassDef> classes
	) {
		this.opcodes = opcodes;
		this.classes = classes;
	}

	@Override
	public Opcodes getOpcodes() {
		return opcodes;
	}

	@Override
	public Set<? extends ClassDef> getClasses() {
		return classes;
	}

}
