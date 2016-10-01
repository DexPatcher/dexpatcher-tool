package lanchon.dexpatcher;

import java.util.List;

import lanchon.dexpatcher.core.Context;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Parser {

	public static final int DEFAULT_API_LEVEL = 14;

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
		if (files.isEmpty()) throw new ParseException("Missing argument: <source-dex-or-apk>");
		config.sourceFile = files.remove(0);
		config.patchFiles = files;
		config.patchedFile = cl.getOptionValue("output");

		Number apiLevel = (Number) cl.getParsedOptionValue("api-level");
		config.apiLevel = (apiLevel != null ? apiLevel.intValue() : DEFAULT_API_LEVEL);
		config.experimental = cl.hasOption("experimental");

		config.annotationPackage = cl.getOptionValue("annotations", Context.DEFAULT_ANNOTATION_PACKAGE);
		config.dexTagSupported = cl.hasOption("compat-dextag");

		config.logLevel = WARN;
		if (cl.hasOption("quiet")) config.logLevel = ERROR;
		if (cl.hasOption("verbose")) config.logLevel = INFO;
		if (cl.hasOption("debug")) config.logLevel = DEBUG;

		if (cl.hasOption("path")) config.sourceCodeRoot = "";
		config.sourceCodeRoot = cl.getOptionValue("path-root", config.sourceCodeRoot);
		config.timingStats = cl.hasOption("stats");

		return config;

	}

	public static void printUsage() {
		System.out.println("DexPatcher Version " + Main.getVersion() + " by Lanchon");
		String usage = "dexpatcher [<option> ...] [--output <patched-dex>] <source-dex-or-apk> [<patch-dex-or-apk> ...]";
		new HelpFormatter().printHelp(usage, getOptions());
	}

	private static Options getOptions() {

		Options options = new Options();
		Option o;

		o = new Option("o", "output", true, "name of patched dex file to write");
		o.setArgName("patched-dex"); options.addOption(o);

		o = new Option("a", "api-level", true, "android api level of files (default: " + DEFAULT_API_LEVEL + ")");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);
		options.addOption(new Option("X", "experimental", false, "enable support for experimental opcodes"));

		o = new Option(null, "annotations", true, "package name of DexPatcher annotations (default: '" +
				Context.DEFAULT_ANNOTATION_PACKAGE + "')");
		o.setArgName("package"); options.addOption(o);
		options.addOption(new Option(null, "compat-dextag", false, "enable support for the deprecated DexTag"));

		options.addOption(new Option("q", "quiet", false, "do not output warnings"));
		options.addOption(new Option("v", "verbose", false, "output extra information"));
		options.addOption(new Option(null, "debug", false, "output debugging information"));

		options.addOption(new Option("p", "path", false, "output relative paths of source code files"));
		o = new Option(null, "path-root", true, "output absolute paths of source code files");
		o.setArgName("root"); options.addOption(o);
		options.addOption(new Option(null, "stats", false, "output timing statistics"));

		options.addOption(new Option(null, "version", false, "print version information and exit"));
		options.addOption(new Option("?", "help", false, "print this help message and exit"));

		return options;

	}

}
