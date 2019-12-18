/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.wrappers;

import java.util.Set;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class WrapperDexFile implements DexFile {

	protected final DexFile wrappedDexFile;

	public WrapperDexFile(DexFile wrappedDexFile) {
		this.wrappedDexFile = wrappedDexFile;
	}

	@Override
	public Set<? extends ClassDef> getClasses() {
		return wrappedDexFile.getClasses();
	}

	@Override
	public Opcodes getOpcodes() {
		return wrappedDexFile.getOpcodes();
	}

}