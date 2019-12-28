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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lanchon.dexpatcher.Processor.PreTransform;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.transform.anonymizer.TypeAnonymizer;
import lanchon.dexpatcher.transform.codec.StringCodec;
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

		// Main options:

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
		if (files.isEmpty()) {
			throw new ParseException("Missing argument: <source-dex-apk-or-dir>");
		}
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
		if (config.annotationPackage.isEmpty()) config.annotationPackage = null;
		config.constructorAutoIgnoreDisabled = cl.hasOption("no-auto-ignore");

		config.logLevel = WARN;
		if (cl.hasOption("quiet")) config.logLevel = ERROR;
		if (cl.hasOption("verbose")) config.logLevel = INFO;
		if (cl.hasOption("debug")) config.logLevel = DEBUG;

		if (cl.hasOption("path")) config.sourceCodeRoot = "";
		config.sourceCodeRoot = cl.getOptionValue("path-root", config.sourceCodeRoot);
		config.timingStats = cl.hasOption("stats");

		config.dryRun = cl.hasOption("dry-run");

		// Code transform options:

		config.mapSource = cl.hasOption("map-source");

		config.unmapSource = cl.hasOption("unmap-source");
		config.unmapPatches = cl.hasOption("unmap-patches");
		config.unmapOutput = cl.hasOption("unmap-output");

		String[] mapFiles = cl.getOptionValues("map");
		if (config.mapSource || config.unmapSource || config.unmapPatches || config.unmapOutput) {
			if (mapFiles == null) {
				throw new ParseException("Missing option: map");
			} else {
				config.mapFiles = Arrays.asList(mapFiles);
			}
		}
		config.invertMap = cl.hasOption("invert-map");

		config.deanonSource = cl.hasOption("deanon-source");
		config.deanonSourceAlternate = cl.hasOption("deanon-source-alt");
		if (config.deanonSource && config.deanonSourceAlternate) {
			throw new ParseException("Incompatible options: deanon-source, deanon-source-alt");
		}
		config.deanonPatches = cl.hasOption("deanon-patches");
		config.deanonPatchesAlternate = cl.hasOption("deanon-patches-alt");
		if (config.deanonPatches && config.deanonPatchesAlternate) {
			throw new ParseException("Incompatible options: deanon-patches, deanon-patches-alt");
		}

		config.reanonSource = cl.hasOption("reanon-source");
		config.reanonPatches = cl.hasOption("reanon-patches");
		config.reanonOutput = cl.hasOption("reanon-output");

		config.mainAnonymizationPlan = cl.getOptionValue("main-plan",
				TypeAnonymizer.DEFAULT_MAIN_ANONYMIZATION_PLAN);
		if (!TypeAnonymizer.isValidPlan(config.mainAnonymizationPlan)) {
			throw new ParseException("Invalid main anonymization plan: '" + config.mainAnonymizationPlan + "'");
		}
		config.alternateAnonymizationPlan = cl.getOptionValue("alt-plan",
				TypeAnonymizer.DEFAULT_ALTERNATE_ANONYMIZATION_PLAN);
		if (!TypeAnonymizer.isValidPlan(config.alternateAnonymizationPlan)) {
			throw new ParseException("Invalid alternate anonymization plan: '" + config.alternateAnonymizationPlan + "'");
		}
		config.treatReanonymizeErrorsAsWarnings = cl.hasOption("no-reanon-errors");

		config.decodeSource = cl.hasOption("decode-source");
		config.decodePatches = cl.hasOption("decode-patches");
		config.decodeOutput = cl.hasOption("decode-output");

		config.codeMarker = cl.getOptionValue("code-marker", StringCodec.DEFAULT_CODE_MARKER);
		if (!StringCodec.isValidCodeMarker(config.codeMarker)) {
			throw new ParseException("Invalid code marker: '" + config.codeMarker + "'");
		}
		config.treatDecodeErrorsAsWarnings = cl.hasOption("no-decode-errors");

		String preTransformSet = cl.getOptionValue("pre-transform", null);
		if (preTransformSet != null) {
			config.preTransform = PreTransform.parse(preTransformSet);
			if (config.preTransform == null) {
				throw new ParseException("Invalid pre-transform set: '" + preTransformSet + "'");
			}
		}

		return config;

	}

	public static void printUsage() {
		PrintWriter writer = new PrintWriter(System.out);
		HelpFormatter formatter = new HelpFormatter();
		printUsage(writer, formatter);
		writer.flush();
	}

	public static void printUsage(PrintWriter writer, HelpFormatter formatter) {
		int width = formatter.getWidth();
		int leftPadding = formatter.getLeftPadding();
		int descPadding = formatter.getDescPadding();
		String usage = "dexpatcher [<option> ...] [--output <patched-dex-or-dir>] " +
				"<source-dex-apk-or-dir> [<patch-dex-apk-or-dir> ...]";
		formatter.printWrapped(writer, width, Main.getHeader());
		writer.println();
		formatter.printUsage(writer, width, usage);
		writer.println();
		formatter.printWrapped(writer, width, "main options:");
		formatter.printOptions(writer, width, addMainOptions(new Options()), leftPadding, descPadding);
		writer.println();
		formatter.printWrapped(writer, width, "code transform options:");
		formatter.printOptions(writer, width, addTransformOptions(new Options()), leftPadding, descPadding);
		writer.println();
	}

	private static Options getOptions() {
		Options options = new Options();
		addMainOptions(options);
		addTransformOptions(options);
		return options;
	}

	private static Options addMainOptions(Options options) {

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

		options.addOption(new Option("q", "quiet", false, "do not output warnings"));
		options.addOption(new Option("v", "verbose", false, "output extra information"));
		options.addOption(new Option("d", "debug", false, "output debugging information"));

		options.addOption(new Option("p", "path", false, "output relative paths of source code files"));
		o = new Option("P", "path-root", true, "output absolute paths of source code files");
		o.setArgName("root"); options.addOption(o);
		options.addOption(new Option(null, "stats", false, "output timing statistics"));

		options.addOption(new Option(null, "dry-run", false, "do not write output files (much faster)"));

		options.addOption(new Option(null, "version", false, "print version information and exit"));
		options.addOption(new Option("?", "help", false, "print this help message and exit"));

		return options;

	}

	private static Options addTransformOptions(Options options) {

		Option o;

		options.addOption(new Option(null, "map-source", false, "apply map to identifiers in source"));

		options.addOption(new Option(null, "unmap-source", false, "apply map inverse to identifiers in source"));
		options.addOption(new Option(null, "unmap-patches", false, "apply map inverse to identifiers in patches"));
		options.addOption(new Option(null, "unmap-output", false, "apply map inverse to identifiers in output"));

		o = new Option(null, "map", true, "identifier map file (repeatable option)");
		o.setArgName("file"); options.addOption(o);
		options.addOption(new Option(null, "invert-map", false, "use inverse of identifier map file"));

		options.addOption(new Option(null, "deanon-source", false, "deanonymize anonymous classes in source"));
		options.addOption(new Option(null, "deanon-source-alt", false, "deanonymize source with alternate plan"));
		options.addOption(new Option(null, "deanon-patches", false, "deanonymize anonymous classes in patches"));
		options.addOption(new Option(null, "deanon-patches-alt", false, "deanonymize patches with alternate plan"));
		options.addOption(new Option(null, "reanon-source", false, "reanonymize anonymous classes in source"));
		options.addOption(new Option(null, "reanon-patches", false, "reanonymize anonymous classes in patches"));
		options.addOption(new Option(null, "reanon-output", false, "reanonymize anonymous classes in output"));

		o = new Option(null, "main-plan", true, "main anonymization plan (default: '" +
				TypeAnonymizer.DEFAULT_MAIN_ANONYMIZATION_PLAN + "')");
		o.setArgName("main-plan"); options.addOption(o);
		o = new Option(null, "alt-plan", true, "alternate plan (default: '" +
				TypeAnonymizer.DEFAULT_ALTERNATE_ANONYMIZATION_PLAN + "')");
		o.setArgName("alt-plan"); options.addOption(o);
		options.addOption(new Option(null, "no-reanon-errors", false, "treat reanonymize errors as warnings"));

		options.addOption(new Option(null, "decode-source", false, "decode identifiers in source"));
		options.addOption(new Option(null, "decode-patches", false, "decode identifiers in patches"));
		options.addOption(new Option(null, "decode-output", false, "decode identifiers in output"));

		o = new Option(null, "code-marker", true, "identifier code marker (default: '" +
				StringCodec.DEFAULT_CODE_MARKER + "')");
		o.setArgName("marker"); options.addOption(o);
		options.addOption(new Option(null, "no-decode-errors", false, "treat decode errors as warnings"));

		StringBuilder sb = new StringBuilder();
		for (PreTransform pt : PreTransform.values()) {
			if (sb.length() != 0) sb.append("|");
			sb.append("'").append(pt.format()).append("'");
		}
		o = new Option(null, "pre-transform", true, "add pre-transform stages (default: '" +
				Processor.DEFAULT_PRE_TRANSFORM.format() + "') (<set>: " + sb + ")");
		o.setArgName("set"); options.addOption(o);

		return options;

	}

	private Parser() {}

}
