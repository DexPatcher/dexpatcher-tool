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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.ZipDexContainer;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public class MultiDexIO {

	private MultiDexIO() {}

	public static DexBackedDexFile readRawDexFile(File file, Opcodes opcodes) throws IOException {
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
		try {
			return DexBackedDexFile.fromInputStream(opcodes, inputStream);
		} finally {
			inputStream.close();
		}
	}

	public static DexFile readMultiDexFile(boolean multiDex, File file, DexFileNamer namer, Opcodes opcodes) throws IOException {
		return new MultiDexContainerBackedDexFile<>(readMultiDexContainer(multiDex, file, namer, opcodes), opcodes);
	}

	public static MultiDexContainer<? extends DexFile> readMultiDexContainer(boolean multiDex, File file, DexFileNamer namer, Opcodes opcodes) throws IOException {
		MultiDexContainer<? extends DexFile> container = readMultiDexContainer(file, namer, opcodes);
		if (!multiDex && container.getDexEntryNames().size() != 1) throw new MultiDexDetectedException(file.toString());
		return container;
	}

	public static MultiDexContainer<? extends DexFile> readMultiDexContainer(File file, DexFileNamer namer, Opcodes opcodes) throws IOException {
		if (file.isDirectory()) return new DirectoryDexContainer(file, namer, opcodes);
		if (!file.isFile()) throw new FileNotFoundException(file.toString());
		MultiDexContainer<? extends DexFile> container = DexFileFactory.loadDexContainer(file, opcodes);
		if (container instanceof ZipDexContainer) container = new FilteredMultiDexContainer<>(container, namer);
		return container;
	}

}
