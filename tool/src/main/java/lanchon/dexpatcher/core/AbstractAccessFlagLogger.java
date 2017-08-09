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

import lanchon.dexpatcher.core.logger.Logger;

import org.jf.dexlib2.AccessFlags;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class AbstractAccessFlagLogger {

	private String item;
	private int oldFlags;
	private int newFlags;

	protected AbstractAccessFlagLogger(String item, int oldFlags, int newFlags) {
		this.item = item;
		this.oldFlags = oldFlags;
		this.newFlags = newFlags;
	}

	protected abstract void log(Logger.Level level, String message);

	private void log(Logger.Level level, AccessFlags flag, String message) {
		log(level, "'" + flag + "' modifier " + message);
	}

	private void flag(AccessFlags flag, Logger.Level level) {
		flag(flag, level, level);
	}

	private void flag(AccessFlags flag, Logger.Level added, Logger.Level removed) {
		boolean isSet = flag.isSet(newFlags);
		if (isSet != flag.isSet(oldFlags)) {
			Logger.Level level = isSet ? added : removed;
			log(level, flag, (isSet ? "added to " : "removed from ") + item);
		}
	}

	private void scopeFlags(Logger.Level decreased, Logger.Level notDecreased) {
		AccessFlags newScope = getScope(newFlags);
		AccessFlags oldScope = getScope(oldFlags);
		if (oldScope != null && newScope != null) {
			if (oldScope != newScope) {
				Logger.Level level = (oldScope == PRIVATE || newScope == PUBLIC) ? notDecreased : decreased;
				log(level, oldScope, "changed to '" + newScope + "' in " + item);
			}
		} else {
			flag(PUBLIC, notDecreased, decreased);
			flag(PRIVATE, decreased, notDecreased);
			flag(PROTECTED, decreased, decreased);
		}
	}

	private AccessFlags getScope(int flags) {
		boolean isPublic = PUBLIC.isSet(flags);
		boolean isPrivate = PRIVATE.isSet(flags);
		boolean isProtected = PROTECTED.isSet(flags);
		int n = (isPublic ? 1 : 0) + (isPrivate ? 1 : 0) + (isProtected ? 1 : 0);
		if (n != 1) return null;
		if (isPublic) return PUBLIC;
		if (isPrivate) return PRIVATE;
		if (isProtected) return PROTECTED;
		throw new AssertionError("Unexpected scope");
	}

	public void allFlags(boolean keepInterface, boolean keepImplementation) {

		// Interface Dependent
		scopeFlags(keepInterface ? WARN : DEBUG, keepInterface ? INFO : DEBUG);
		flag(FINAL, (keepInterface && !PRIVATE.isSet(oldFlags)) ? WARN : INFO, INFO);
		flag(VOLATILE, INFO, keepInterface ? WARN : INFO);
		flag(TRANSIENT, keepInterface ? WARN : INFO);
		flag(VARARGS, INFO);
		flag(CONSTRUCTOR, keepInterface ? WARN : DEBUG);

		// Interface And Implementation Dependent
		flag(STATIC, WARN);
		flag(INTERFACE, WARN);
		flag(ANNOTATION, WARN);
		flag(ENUM, WARN);

		// Implementation Dependent
		flag(SYNCHRONIZED, keepImplementation ? WARN : DEBUG);
		flag(NATIVE, keepImplementation ? WARN : DEBUG);
		flag(ABSTRACT, WARN, keepImplementation ? WARN : INFO);
		flag(STRICTFP, keepImplementation ? WARN : DEBUG);
		flag(DECLARED_SYNCHRONIZED, keepImplementation ? INFO : DEBUG);

		// Extra
		flag(BRIDGE, DEBUG);
		flag(SYNTHETIC, DEBUG);

	}

}
