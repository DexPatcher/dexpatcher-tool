/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.multidex;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public class MultiDexContainerBackedDexFile<T extends DexFile> implements DexFile {

	private final Set<? extends ClassDef> classes;
	private final Opcodes opcodes;

	public MultiDexContainerBackedDexFile(MultiDexContainer<T> container, Opcodes opcodes) throws IOException {
		List<String> names = container.getDexEntryNames();
		if (names.size() == 1) {
			Set<? extends ClassDef> classes = container.getEntry(names.get(0)).getClasses();
			this.classes = Collections.unmodifiableSet(classes);
		} else {
			LinkedHashSet<ClassDef> classes = new LinkedHashSet<>();
			for (String name : names) {
				for (ClassDef classDef : container.getEntry(name).getClasses()) {
					if (!classes.add(classDef)) throw new DuplicateTypeException(classDef.getType());
				}
			}
			this.classes = Collections.unmodifiableSet(classes);
		}
		this.opcodes = opcodes;
	}

	@Override
	public Set<? extends ClassDef> getClasses() {
		return classes;
	}

	@Override
	public Opcodes getOpcodes() {
		return opcodes;
	}

}
