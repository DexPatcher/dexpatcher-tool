/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.DexPatcher;
import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.transform.DexProvider;
import lanchon.dexpatcher.transform.DexVisitor;
import lanchon.dexpatcher.transform.anonymizer.DexAnonymizer;
import lanchon.dexpatcher.transform.anonymizer.TypeAnonymizer;
import lanchon.dexpatcher.transform.codec.decoder.DexDecoder;
import lanchon.dexpatcher.transform.codec.decoder.StringDecoder;
import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.DexFileNamer;
import lanchon.multidexlib2.DexIO;
import lanchon.multidexlib2.MultiDexIO;
import lanchon.multidexlib2.OpcodeUtils;
import lanchon.multidexlib2.SingletonDexContainer;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Processor {

	public enum PreTransform {

		NONE,
		DRY,
		OUT,
		INOUT,
		ALL;

		public static PreTransform parse(String s) {
			s = s /* .replace('-', '_') */ .toUpperCase(Locale.ROOT);
			try {
				return PreTransform.valueOf(s);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		public String format() {
			return name().toLowerCase(Locale.ROOT) /* .replace('_', '-') */ ;
		}

	}

	public static final PreTransform DEFAULT_PRE_TRANSFORM = PreTransform.OUT;

	public static boolean processFiles(Logger logger, Configuration config) throws IOException {
		return new Processor(logger, config).processFiles();
	}

	private final Logger logger;
	private final Configuration config;

	private DexFileNamer dexFileNamer;
	private Opcodes opcodes;
	private StringDecoder stringDecoder;

	private Processor(Logger logger, Configuration config) {
		this.logger = logger;
		this.config = config;
	}

	private boolean processFiles() throws IOException {

		long time = System.nanoTime();

		logger.setLogLevel(config.logLevel);
		dexFileNamer = new BasicDexFileNamer();
		if (config.apiLevel > 0) opcodes = Opcodes.forApi(config.apiLevel);
		stringDecoder = new StringDecoder(config.codeMarker);

		DexProvider dex = readDex(new File(config.sourceFile));
		dex = anonymizeDex(dex, config.deanonSourcePlan, false, "deanonymize source");
		dex = decodeDex(dex, config.decodeSource, "decode source");
		dex = anonymizeDex(dex, config.reanonSourcePlan, true, "reanonymize source");
		if (config.preTransform == PreTransform.INOUT) preTransformDex(dex, "transform source");
		int types = dex.getDexFile().getClasses().size();

		for (String patchFile : config.patchFiles) {
			DexProvider patchDex = readDex(new File(patchFile));
			patchDex = anonymizeDex(patchDex, config.deanonPatchesPlan, false, "deanonymize patch");
			patchDex = decodeDex(patchDex, config.decodePatches, "decode patch");
			patchDex = anonymizeDex(patchDex, config.reanonPatchesPlan, true, "reanonymize patch");
			if (config.preTransform == PreTransform.INOUT) preTransformDex(patchDex, "transform patch");
			types += patchDex.getDexFile().getClasses().size();
			dex = patchDex(dex, patchDex);
		}

		dex = decodeDex(dex, config.decodeOutput, "decode output");
		dex = anonymizeDex(dex, config.reanonOutputPlan, true, "reanonymize output");

		boolean writeDex = logger.hasNotLoggedErrors() && !config.dryRun && config.patchedFile != null;
		boolean preTransformOutput = (config.preTransform == PreTransform.DRY && !writeDex) ||
				config.preTransform == PreTransform.OUT || config.preTransform == PreTransform.INOUT;
		if (preTransformOutput) preTransformDex(dex, "transform output");

		if (logger.hasNotLoggedErrors()) {
			if (config.dryRun) {
				logger.log(INFO, "dry run due to '--dry-run' option");
			} else {
				if (config.patchedFile == null) {
					logger.log(WARN, "dry run due to missing '--output' option");
				} else {
					writeDex(new File(config.patchedFile), dex);
				}
			}
		}

		time = System.nanoTime() - time;
		logStats("total process", types, time);

		logger.logErrorAndWarningCounts();
		return logger.hasNotLoggedErrors();

	}

	private DexProvider anonymizeDex(DexProvider dex, String plan, boolean reanonymize, String logPrefix) {
		if (plan != null) {
			dex = DexAnonymizer.anonymize(dex, new TypeAnonymizer(plan, reanonymize), logger, logPrefix, DEBUG,
					config.treatReanonymizeErrorsAsWarnings ? WARN : ERROR);
			if (config.preTransform == PreTransform.ALL) preTransformDex(dex, logPrefix);
		}
		return dex;
	}

	private DexProvider decodeDex(DexProvider dex, boolean enabled, String logPrefix) {
		if (enabled) {
			dex = DexDecoder.decode(dex, stringDecoder, logger, logPrefix, DEBUG,
					config.treatDecodeErrorsAsWarnings ? WARN : ERROR);
			if (config.preTransform == PreTransform.ALL) preTransformDex(dex, logPrefix);
		}
		return dex;
	}

	private void preTransformDex(DexProvider dex, String logPrefix) {
		if (dex.isLogging()) {
			long time = System.nanoTime();
			new DexVisitor().visitDexFile(dex.getDexFile());
			time = System.nanoTime() - time;
			logStats(logPrefix, dex.getDexFile().getClasses().size(), time);
			dex.stopLogging();
		}
	}

	private Context createContext() {
		return new Context.Builder(logger)
			.setAnnotationPackage(config.annotationPackage)
			.setConstructorAutoIgnoreDisabled(config.constructorAutoIgnoreDisabled)
			.setSourceCodeRoot(config.sourceCodeRoot)
			.build();
	}

	private DexProvider patchDex(DexProvider sourceDex, DexProvider patchDex) {
		long time = System.nanoTime();
		DexFile sourceDexFile = sourceDex.getDexFile();
		DexFile patchDexFile = patchDex.getDexFile();
		Opcodes patchedOpcodes = opcodes;
		if (patchedOpcodes == null) {
			Opcodes sourceOpcodes = sourceDexFile.getOpcodes();
			patchedOpcodes = OpcodeUtils.getNewestOpcodes(sourceOpcodes, patchDexFile.getOpcodes(), true);
			if (sourceOpcodes != null && patchedOpcodes != null && sourceOpcodes != patchedOpcodes) {
				int sourceDexVersion = OpcodeUtils.getDexVersionFromOpcodes(sourceOpcodes);
				int patchedDexVersion = OpcodeUtils.getDexVersionFromOpcodes(patchedOpcodes);
				if (sourceDexVersion != patchedDexVersion) {
					logger.log(INFO, String.format("patch changes dex version from '%03d' to '%03d'",
							sourceDexVersion, patchedDexVersion));
				}
			}
		}
		DexFile patchedDexFile = DexPatcher.process(createContext(), sourceDexFile, patchDexFile, patchedOpcodes);
		time = System.nanoTime() - time;
		logStats("patch process", sourceDexFile.getClasses().size() + patchDexFile.getClasses().size(), time);
		return new DexProvider.FromTransform(patchedDexFile, sourceDex, patchDex);
	}

	private DexProvider readDex(File file) throws IOException {
		String message = "read '" + file + "'";
		logger.log(INFO, message);
		long time = System.nanoTime();
		DexFile dex = MultiDexIO.readDexFile(config.multiDex, file, dexFileNamer, opcodes, getIOLogger(message));
		time = System.nanoTime() - time;
		if (logger.isLogging(DEBUG) && opcodes == null && dex.getOpcodes() != null) {
			int dexVersion = OpcodeUtils.getDexVersionFromOpcodes(dex.getOpcodes());
			logger.log(DEBUG, String.format(message + ": dex version '%03d'", dexVersion));
		}
		logStats(message, dex.getClasses().size(), time);
		return new DexProvider.FromDexFile(dex);
	}

	private void writeDex(File file, DexProvider dex) throws IOException {
		DexFile dexFile = dex.getDexFile();
		String message = "write '" + file + "'";
		logger.log(INFO, message);
		if (logger.isLogging(DEBUG) && dexFile.getOpcodes() != null) {
			int dexVersion = OpcodeUtils.getDexVersionFromOpcodes(dexFile.getOpcodes());
			logger.log(DEBUG, String.format(message + ": dex version '%03d'", dexVersion));
		}
		long time = System.nanoTime();
		MultiDexIO.writeDexFile(config.multiDex, config.multiDexJobs, file, dexFileNamer,
				dexFile, config.maxDexPoolSize, getIOLogger(message));
		time = System.nanoTime() - time;
		logStats(message, dexFile.getClasses().size(), time);
	}

	private DexIO.Logger getIOLogger(final String header) {
		if (!logger.isLogging(DEBUG)) return null;
		return new DexIO.Logger() {
			@Override
			public void log(File file, String entryName, int typeCount) {
				if (logger.isLogging(DEBUG)) {
					String h = header;
					// See https://github.com/DexPatcher/multidexlib2/commit/177350fbba4d490111d1362810bbeb4521d1803b
					// noinspection StringEquality
					if (entryName != SingletonDexContainer.UNDEFINED_ENTRY_NAME) {
						h += ": file '" + entryName + "'";
					}
					logger.log(DEBUG, h + ": " + typeCount + " types");
				}
			}
		};
	}

	private void logStats(String header, int typeCount, long nanoTime) {
		if (config.timingStats) {
			logger.log(NONE, "stats: " + header + ": " +
					typeCount + " types, " +
					((nanoTime + 500000) / 1000000) + " ms, " +
					(((nanoTime / typeCount) + 500) / 1000) + " us/type");
		}
	}

}
