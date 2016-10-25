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

	public MultiDexContainerBackedDexFile(MultiDexContainer<T> container) throws IOException {
		List<String> entryNames = container.getDexEntryNames();
		if (entryNames.size() == 1) {
			String entryName = entryNames.get(0);
			Set<? extends ClassDef> entryClasses = container.getEntry(entryName).getClasses();
			this.classes = Collections.unmodifiableSet(entryClasses);
		} else {
			LinkedHashSet<ClassDef> classes = new LinkedHashSet<>();
			for (String entryName : entryNames) {
				Set<? extends ClassDef> entryClasses = container.getEntry(entryName).getClasses();
				for (ClassDef entryClass : entryClasses) {
					if (!classes.add(entryClass)) throw new DuplicateTypeException(entryClass.getType());
				}
			}
			this.classes = Collections.unmodifiableSet(classes);
		}
		this.opcodes = container.getOpcodes();
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
