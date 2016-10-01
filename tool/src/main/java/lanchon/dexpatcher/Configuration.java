package lanchon.dexpatcher;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.logger.Logger;

import java.util.List;

public class Configuration {

	public String sourceFile;
	public List<String> patchFiles;
	public String patchedFile;

	public int apiLevel;
	public boolean experimental;

	public String annotationPackage = Context.DEFAULT_ANNOTATION_PACKAGE;
	public boolean dexTagSupported;

	public Logger.Level logLevel = Context.DEFAULT_LOG_LEVEL;

	public String sourceCodeRoot;
	public boolean timingStats;

}
