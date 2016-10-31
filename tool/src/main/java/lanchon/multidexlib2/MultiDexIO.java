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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public class MultiDexIO {

	public static final int DEFAULT_MAX_THREADS = 4;

	private MultiDexIO() {}

	// Read

	public static DexFile readDexFile(boolean multiDex, File file, DexFileNamer namer, Opcodes opcodes,
			DexIO.Logger logger) throws IOException {
		MultiDexContainer<? extends DexFile> container = readMultiDexContainer(multiDex, file, namer, opcodes, logger);
		return new MultiDexContainerBackedDexFile<>(container);
	}

	public static MultiDexContainer<? extends DexFile> readMultiDexContainer(boolean multiDex, File file,
			DexFileNamer namer, Opcodes opcodes, DexIO.Logger logger) throws IOException {
		MultiDexContainer<? extends DexFile> container = readMultiDexContainer(file, namer, opcodes, logger);
		int entries = container.getDexEntryNames().size();
		if (entries == 0) throw new EmptyMultiDexContainerException(file.toString());
		if (!multiDex && entries > 1) throw new MultiDexDetectedException(file.toString());
		return container;
	}

	public static MultiDexContainer<? extends DexFile> readMultiDexContainer(File file, DexFileNamer namer,
			Opcodes opcodes, DexIO.Logger logger) throws IOException {
		MultiDexContainer<? extends DexFile> container = readMultiDexContainer(file, namer, opcodes);
		if (logger != null) {
			for (String name : container.getDexEntryNames()) {
				logger.log(file, name, container.getEntry(name).getClasses().size());
			}
		}
		return container;
	}

	public static MultiDexContainer<? extends DexFile> readMultiDexContainer(File file, DexFileNamer namer,
			Opcodes opcodes) throws IOException {
		if (file.isDirectory()) return new DirectoryDexContainer(file, namer, opcodes);
		if (!file.isFile()) throw new FileNotFoundException(file.toString());
		if (ZipFileDexContainer.isZipFile(file)) return new ZipFileDexContainer(file, namer, opcodes);
		return new SingletonDexContainer(RawDexIO.readRawDexFile(file, opcodes));
	}

	// Write

	public static int writeDexFile(boolean multiDex, File file, DexFileNamer namer, DexFile dexFile,
			int maxDexPoolSize, DexIO.Logger logger) throws IOException {
		return writeDexFile(multiDex, 1, file, namer, dexFile, maxDexPoolSize, logger);
	}

	public static int writeDexFile(boolean multiDex, int threadCount, File file, DexFileNamer namer, DexFile dexFile,
			int maxDexPoolSize, DexIO.Logger logger) throws IOException {
		return writeDexFile(multiDex, threadCount, file, namer, dexFile, 0, false, maxDexPoolSize, logger);
	}

	public static int writeDexFile(boolean multiDex, File file, DexFileNamer namer, DexFile dexFile,
			int minMainDexClassCount, boolean minimalMainDex, int maxDexPoolSize,
			DexIO.Logger logger) throws IOException {
		return writeDexFile(multiDex, 1, file, namer, dexFile, minMainDexClassCount, minimalMainDex, maxDexPoolSize,
				logger);
	}

	public static int writeDexFile(boolean multiDex, int threadCount, File file, DexFileNamer namer, DexFile dexFile,
			int minMainDexClassCount, boolean minimalMainDex, int maxDexPoolSize,
			DexIO.Logger logger) throws IOException {
		if (file.isDirectory()) {
			return writeMultiDexDirectory(multiDex, threadCount, file, namer, dexFile, minMainDexClassCount,
					minimalMainDex, maxDexPoolSize, logger);
		} else {
			if (multiDex) throw new UnsupportedOperationException(
					"Must output to a directory if multi-dex mode is enabled");
			RawDexIO.writeRawDexFile(file, dexFile, maxDexPoolSize, logger);
			return 1;
		}
	}

	public static int writeMultiDexDirectory(boolean multiDex, int threadCount, File directory, DexFileNamer namer,
			DexFile dexFile, int minMainDexClassCount, boolean minimalMainDex, int maxDexPoolSize, DexIO.Logger logger)
			throws IOException {
		purgeMultiDexDirectory(multiDex, directory, namer);
		DexFileNameIterator nameIterator = new DexFileNameIterator(namer);
		if (threadCount <= 0) {
			threadCount = Runtime.getRuntime().availableProcessors();
			if (threadCount > DEFAULT_MAX_THREADS) threadCount = DEFAULT_MAX_THREADS;
		}
		if (threadCount > 1 && multiDex && minMainDexClassCount == 0 && !minimalMainDex) {
			DexIO.writeMultiDexDirectoryMultiThread(threadCount, directory, nameIterator, dexFile, maxDexPoolSize,
					logger);
		} else {
			DexIO.writeMultiDexDirectorySingleThread(multiDex, directory, nameIterator, dexFile, minMainDexClassCount,
					minimalMainDex, maxDexPoolSize, logger);
		}
		return nameIterator.getCount();
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
