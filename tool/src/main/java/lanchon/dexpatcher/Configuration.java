package lanchon.dexpatcher;

import lanchon.dexpatcher.core.logger.Logger;

import java.util.List;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Configuration {

	public static final int DEFAULT_API_LEVEL = 14;
	public static final String DEFAULT_ANNOTATION_PACKAGE = "lanchon.dexpatcher.annotation";
	public static final Logger.Level DEFAULT_LOG_LEVEL = WARN;

	public String sourceFile;
	public List<String> patchFiles;
	public String patchedFile;

	public int apiLevel = DEFAULT_API_LEVEL;
	public boolean experimental;

	public String annotationPackage = DEFAULT_ANNOTATION_PACKAGE;

	public Logger.Level logLevel = DEFAULT_LOG_LEVEL;
	public boolean stats;

}
