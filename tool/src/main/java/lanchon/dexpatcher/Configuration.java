/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher;

import java.util.List;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.logger.Logger;

public class Configuration {

	public String sourceFile;
	public List<String> patchFiles;
	public String patchedFile;

	public int apiLevel;
	public boolean multiDex;

	public String annotationPackage = Context.DEFAULT_ANNOTATION_PACKAGE;
	public boolean dexTagSupported;

	public Logger.Level logLevel = Context.DEFAULT_LOG_LEVEL;

	public String sourceCodeRoot;
	public boolean timingStats;

}
