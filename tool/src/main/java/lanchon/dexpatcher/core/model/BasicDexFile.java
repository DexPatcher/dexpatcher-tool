/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

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
