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
import java.io.IOException;
import java.io.InputStream;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

public class RawDexIO {

	private RawDexIO() {}

	// Read

	public static DexBackedDexFile readRawDexFile(File file, Opcodes opcodes,
			DexIO.Logger logger) throws IOException {
		DexBackedDexFile dexFile = readRawDexFile(file, opcodes);
		if (logger != null) {
			logger.log(file, SingletonDexContainer.UNDEFINED_ENTRY_NAME, dexFile.getClasses().size());
		}
		return dexFile;
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

	public static void writeRawDexFile(int maxDexPoolSize, File file, DexFile dexFile,
			DexIO.Logger logger) throws IOException {
		DexIO.writeRawDexSingleThread(maxDexPoolSize, file, dexFile, logger);
	}

	public static void writeRawDexFile(int maxDexPoolSize, File file, DexFile dexFile) throws IOException {
		DexIO.writeRawDexSingleThread(maxDexPoolSize, file, dexFile, null);
	}

}
