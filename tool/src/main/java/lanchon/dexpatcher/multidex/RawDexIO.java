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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreamsHack;
import com.google.common.io.Files;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.util.DexUtil;

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
		/*
		InputStream inputStream = new FileInputStream(file);
		try {
			return readRawDexFile(inputStream, file.length(), opcodes);
		} finally {
			inputStream.close();
		}
		*/
		byte[] buf = Files.toByteArray(file);
		return readRawDexFile(buf, 0, opcodes);
	}

	public static DexBackedDexFile readRawDexFile(InputStream inputStream, long expectedSize,
			Opcodes opcodes) throws IOException {
		byte[] buf = ByteStreamsHack.toByteArray(inputStream, expectedSize);
		return readRawDexFile(buf, 0, opcodes);
	}

	public static DexBackedDexFile readRawDexFile(byte[] buf, int offset, Opcodes opcodes) throws IOException {
		DexUtil.verifyDexHeader(buf, offset);
		if (opcodes == null) opcodes = getOpcodesFromDexHeader(buf, offset);
		return new DexBackedDexFile(opcodes, buf, 0);
	}

	static Opcodes getOpcodesFromDexHeader(byte[] buf, int offset) {
		int dexVersion = HeaderItem.getVersion(buf, offset);
		return OpcodeUtils.getOpcodesFromDexVersion(dexVersion);
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
