/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import lanchon.dexpatcher.core.model.BasicDexFile;
import lanchon.dexpatcher.core.patchers.PackagePatcher;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class DexPatcher {

	private DexPatcher() {}

	public static DexFile process(Context context, DexFile sourceDex, DexFile patchDex) {
		return process(context, sourceDex, patchDex, sourceDex.getOpcodes());
	}

	public static DexFile process(Context context, DexFile sourceDex, DexFile patchDex, Opcodes opcodes) {
		Set<? extends ClassDef> sourceClasses = sourceDex.getClasses();
		Set<? extends ClassDef> patchClasses = patchDex.getClasses();
		PackagePatcher patcher = new PackagePatcher(context);
		Collection<ClassDef> patchedClasses = patcher.process(sourceClasses, sourceClasses.size(),
				patchClasses, patchClasses.size());
		return new BasicDexFile(opcodes, Collections.unmodifiableSet(new LinkedHashSet<>(patchedClasses)));
	}

}
