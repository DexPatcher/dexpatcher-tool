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

import java.util.ArrayList;
import java.util.List;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.naming.decoder.NameDecoder;
import lanchon.multidexlib2.DexIO;
import lanchon.multidexlib2.MultiDexIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Parser {

	public static Configuration parseCommandLine(String[] args) throws ParseException {

		Configuration config = new Configuration();

		Options options = getOptions();
		CommandLine cl = new PosixParser().parse(options, args);

		if (cl.hasOption("help")) {
			printUsage();
			return null;
		}

		if (cl.hasOption("version")) {
			System.out.println(Main.getVersion());
			return null;
		}

		@SuppressWarnings("unchecked")
		List<String> files = cl.getArgList();
		if (files.isEmpty()) throw new ParseException("Missing argument: <source-dex-apk-or-dir>");
		config.sourceFile = files.get(0);
		config.patchFiles = new ArrayList<>(files.subList(1, files.size()));
		config.patchedFile = cl.getOptionValue("output");

		Number apiLevel = (Number) cl.getParsedOptionValue("api-level");
		if (apiLevel != null) config.apiLevel = apiLevel.intValue();

		config.multiDex = cl.hasOption("multi-dex");
		if (cl.hasOption("multi-dex-threaded")) { config.multiDex = true; config.multiDexJobs = 0; }
		Number multiDexJobs = (Number) cl.getParsedOptionValue("multi-dex-jobs");
		if (multiDexJobs != null) { config.multiDex = true; config.multiDexJobs = multiDexJobs.intValue(); }

		Number maxDexPoolSize = (Number) cl.getParsedOptionValue("max-dex-pool-size");
		if (maxDexPoolSize != null) config.maxDexPoolSize = maxDexPoolSize.intValue();

		config.annotationPackage = cl.getOptionValue("annotations", Context.DEFAULT_ANNOTATION_PACKAGE);
		config.constructorAutoIgnoreDisabled = cl.hasOption("no-auto-ignore");

		config.decodeSource = cl.hasOption("decode-source");
		config.decodePatches = cl.hasOption("decode-patches");
		config.decodeOutput = cl.hasOption("decode-output");

		config.codeMarker = cl.getOptionValue("code-marker", NameDecoder.DEFAULT_CODE_MARKER);
		if (!NameDecoder.isValidCodeMarker(config.codeMarker)) {
			throw new ParseException("Invalid code marker: '" + config.codeMarker + "'");
		}
		config.treatDecodeErrorsAsWarnings = cl.hasOption("no-decode-errors");

		config.logLevel = WARN;
		if (cl.hasOption("quiet")) config.logLevel = ERROR;
		if (cl.hasOption("verbose")) config.logLevel = INFO;
		if (cl.hasOption("debug")) config.logLevel = DEBUG;

		if (cl.hasOption("path")) config.sourceCodeRoot = "";
		config.sourceCodeRoot = cl.getOptionValue("path-root", config.sourceCodeRoot);
		config.timingStats = cl.hasOption("stats");

		config.dryRun = cl.hasOption("dry-run");

		return config;

	}

	public static void printUsage() {
		System.out.println(Main.getHeader());
		String usage = "dexpatcher [<option> ...] [--output <patched-dex-or-dir>] " +
				"<source-dex-apk-or-dir> [<patch-dex-apk-or-dir> ...]";
		new HelpFormatter().printHelp(usage, getOptions());
	}

	private static Options getOptions() {

		Options options = new Options();
		Option o;

		o = new Option("o", "output", true, "name of output file or directory");
		o.setArgName("dex-or-dir"); options.addOption(o);

		o = new Option("a", "api-level", true, "android api level (default: auto-detect)");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);

		options.addOption(new Option("m", "multi-dex", false, "enable multi-dex support"));
		options.addOption(new Option("M", "multi-dex-threaded", false, "multi-threaded multi-dex (implies: -m)"));
		o = new Option("J", "multi-dex-jobs", true, "multi-dex thread count (implies: -m -M) (default: " +
				"available processors up to " + MultiDexIO.DEFAULT_MAX_THREADS + ")");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);

		o = new Option(null, "max-dex-pool-size", true, "maximum size of dex pools (default: " +
				DexIO.DEFAULT_MAX_DEX_POOL_SIZE + ")");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);

		o = new Option(null, "annotations", true, "package name of DexPatcher annotations (default: '" +
				Context.DEFAULT_ANNOTATION_PACKAGE + "')");
		o.setArgName("package"); options.addOption(o);
		options.addOption(new Option(null, "no-auto-ignore", false, "no trivial default constructor auto-ignore"));

		options.addOption(new Option(null, "decode-source", false, "decode identifiers in source"));
		options.addOption(new Option(null, "decode-patches", false, "decode identifiers in patches"));
		options.addOption(new Option(null, "decode-output", false, "decode identifiers in output"));

		o = new Option(null, "code-marker", true, "identifier code marker (default: '" +
				NameDecoder.DEFAULT_CODE_MARKER + "')");
		o.setArgName("marker"); options.addOption(o);
		options.addOption(new Option(null, "no-decode-errors", false, "treat identifier decode errors as warnings"));

		options.addOption(new Option("q", "quiet", false, "do not output warnings"));
		options.addOption(new Option("v", "verbose", false, "output extra information"));
		options.addOption(new Option(null, "debug", false, "output debugging information"));

		options.addOption(new Option("p", "path", false, "output relative paths of source code files"));
		o = new Option("P", "path-root", true, "output absolute paths of source code files");
		o.setArgName("root"); options.addOption(o);
		options.addOption(new Option(null, "stats", false, "output timing statistics"));

		options.addOption(new Option(null, "dry-run", false, "do not write output files (much faster)"));

		options.addOption(new Option(null, "version", false, "print version information and exit"));
		options.addOption(new Option("?", "help", false, "print this help message and exit"));

		return options;

	}

	private Parser() {}

}
