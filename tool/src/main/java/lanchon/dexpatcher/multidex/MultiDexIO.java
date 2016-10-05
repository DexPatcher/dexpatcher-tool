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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.ZipDexContainer;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

public class MultiDexIO {

	public interface Logger {

		void log(boolean multiDex, File file, String entryName, int typeCount);

	}

	private MultiDexIO() {}

	// Read

	public static DexFile readDexFile(boolean multiDex, File file, DexFileNamer namer, Opcodes opcodes,
			MultiDexIO.Logger logger) throws IOException {
		MultiDexContainer<? extends DexFile> container = readMultiDexContainer(multiDex, file, namer, opcodes);
		if (logger != null) {
			for (String name : container.getDexEntryNames()) {
				logger.log(multiDex, file, name, container.getEntry(name).getClasses().size());
			}
		}
		return new MultiDexContainerBackedDexFile<>(container, opcodes);
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

	public static DexBackedDexFile readRawDexFile(File file, Opcodes opcodes) throws IOException {
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
		try {
			return DexBackedDexFile.fromInputStream(opcodes, inputStream);
		} finally {
			inputStream.close();
		}
	}

	// Write

	public static void writeDexFile(boolean multiDex, File file, DexFileNamer namer, DexFile dexFile,
			MultiDexIO.Logger logger) throws IOException {
		if (file.isDirectory()) {
			writeMultiDexDirectory(multiDex, file, namer, dexFile, logger);
		} else {
			if (multiDex) throw new IOException("The output location must be a directory if multi-dex mode is enabled");
			writeRawDexFile(file, dexFile, logger);
		}
	}

	public static void writeMultiDexDirectory(boolean multiDex, File directory, DexFileNamer namer, DexFile dexFile,
			MultiDexIO.Logger logger) throws IOException {
		purgeMultiDexDirectory(multiDex, directory, namer);
		String currentName = namer.getName(0);
		File currentFile = new File(directory, currentName);
		writeMultiDexCommon(multiDex, directory, namer, currentName, currentFile, dexFile, logger);
	}

	public static void writeRawDexFile(File file, DexFile dexFile, MultiDexIO.Logger logger) throws IOException {
		String currentName = file.toString();
		File currentFile = file;
		writeMultiDexCommon(false, file, null, currentName, currentFile, dexFile, logger);
	}

	private static int writeMultiDexCommon(boolean multiDex, File base, DexFileNamer namer,
			String currentName, File currentFile, DexFile dexFile, MultiDexIO.Logger logger) throws IOException {
		Set<? extends ClassDef> classes = dexFile.getClasses();
		Iterator<? extends ClassDef> classIterator = classes.iterator();
		int fileCount = 0;
		ClassDef currentClass = (classIterator.hasNext() ? classIterator.next() : null);
		for (;;) {
			DexPool dexPool = DexPool.makeDexPool(dexFile.getOpcodes());
			int fileClassCount = 0;
			while (currentClass != null) {
				dexPool.mark();
				dexPool.internClass(currentClass);
				if (dexPool.hasOverflowed()) {
					if (!multiDex) throw new DexPoolOverflowException(
							"Dex pool overflowed while writing type " + (fileClassCount + 1) + " of " + classes.size());
					if (fileClassCount == 0) throw new DexPoolOverflowException(
							"Type too big for dex pool: " + currentClass.getType());
					dexPool.reset();
					break;
				}
				fileClassCount++;
				currentClass = (classIterator.hasNext() ? classIterator.next() : null);
			}
			if (logger != null) logger.log(multiDex, base, currentName, fileClassCount);
			dexPool.writeTo(new FileDataStore(currentFile));
			fileCount++;
			if (currentClass == null) break;
			currentName = namer.getName(fileCount);
			currentFile = new File(base, currentName);
		}
		return fileCount;
	}

	public static void purgeMultiDexDirectory(boolean multiDex, File directory, DexFileNamer namer) throws IOException {
		List<String> names = new DirectoryDexContainer(directory, namer, null).getDexEntryNames();
		if (!multiDex && names.size() > 1) throw new MultiDexDetectedException(directory.toString());
		for (String name : names) {
			File existingFile = new File(directory, name);
			if (!existingFile.delete()) throw new IOException("Cannot delete file: " + existingFile.toString());
		}
	}

}
