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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public class FilteredMultiDexContainer<T extends DexFile> implements MultiDexContainer<T> {

	private final MultiDexContainer<T> container;
	private final DexFileNamer namer;
	private final List<String> dexEntryNames;

	public FilteredMultiDexContainer(MultiDexContainer<T> container, DexFileNamer namer) throws IOException {
		this.container = container;
		this.namer = namer;
		List<String> dexEntryNames = new ArrayList<>();
		for (String name : container.getDexEntryNames()) {
			if (namer.isValidName(name)) {
				dexEntryNames.add(name);
			}
		}
		this.dexEntryNames = Collections.unmodifiableList(dexEntryNames);
	}

	@Override
	public List<String> getDexEntryNames() throws IOException {
		return dexEntryNames;
	}

	@Override
	public T getEntry(String entryName) throws IOException {
		if (!namer.isValidName(entryName)) return null;
		return container.getEntry(entryName);
	}

}
