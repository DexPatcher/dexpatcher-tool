/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.multidexlib2;

import java.util.Set;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.MultiDexContainer.MultiDexFile;

public class BasicMultiDexFile<T extends MultiDexContainer<? extends MultiDexFile>> implements MultiDexFile {

	private final T container;
	private final String entryName;
	private final DexFile dexFile;

	public BasicMultiDexFile(T container, String entryName, DexFile dexFile) {
		this.container = container;
		this.entryName = entryName;
		this.dexFile = dexFile;
	}

	@Override
	public Set<? extends ClassDef> getClasses() {
		return dexFile.getClasses();
	}

	@Override
	public Opcodes getOpcodes() {
		return dexFile.getOpcodes();
	}

	@Override
	public String getEntryName() {
		return entryName;
	}

	@Override
	public T getContainer() {
		return container;
	}

}
