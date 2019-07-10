/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.util;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.patcher.AnnotatableSetPatcher;
import lanchon.dexpatcher.core.patcher.ClassSetPatcher;
import lanchon.dexpatcher.core.patcher.FieldSetPatcher;
import lanchon.dexpatcher.core.patcher.MethodSetPatcher;

import org.jf.dexlib2.AccessFlags;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class AccessFlagLogger {

	private final String item;
	private final int oldFlags;
	private final int newFlags;

	protected AccessFlagLogger(String item, int oldFlags, int newFlags) {
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

	private void scopeFlags(Logger.Level level) {
		scopeFlags(level, level);
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
			flag(PROTECTED, notDecreased, decreased);
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

	public void allFlags(AnnotatableSetPatcher<?> patcher, boolean keepInterface, boolean keepImplementation) {

		//if (!(keepInterface || keepImplementation)) {
		//	throw new AssertionError("Neither interface nor implementation kept");
		//}

		boolean isClass = (patcher instanceof ClassSetPatcher);
		boolean isField = (patcher instanceof FieldSetPatcher);
		boolean isMethod = (patcher instanceof MethodSetPatcher);

		// Interface Dependent
		if (keepInterface) {
			if (isMethod && !STATIC.isSet(oldFlags | newFlags) && PRIVATE.isSet(oldFlags ^ newFlags)) {
				scopeFlags(WARN);
			} else {
				scopeFlags(WARN, INFO);
			}
		} else {
			scopeFlags(DEBUG);
		}
		if (keepInterface) {
			if      (isClass)  flag(FINAL, WARN, INFO);
			else if (isField)  flag(FINAL, WARN, (STATIC.isSet(oldFlags) ? WARN : INFO));
			else if (isMethod) flag(FINAL, (!PRIVATE.isSet(oldFlags) ? WARN : INFO), INFO);
		} else {
			flag(FINAL, INFO);
		}
		flag(VOLATILE, INFO, keepInterface ? WARN : INFO);
		flag(TRANSIENT, keepInterface ? WARN : INFO);
		flag(VARARGS, INFO);
		// This is an error, but it is tolerated in case malformed code is not rejected by ART:
		flag(CONSTRUCTOR, keepInterface ? /* ERROR */ WARN : DEBUG);

		// Interface And Implementation Dependent
		flag(STATIC, (isMethod && keepImplementation ? ERROR : WARN));
		flag(INTERFACE, WARN);
		flag(ANNOTATION, WARN);
		flag(ENUM, WARN);

		// Implementation Dependent
		flag(SYNCHRONIZED, keepImplementation ? WARN : DEBUG);
		flag(NATIVE, keepImplementation ? WARN : DEBUG);
		if (keepImplementation) {
			flag(ABSTRACT, (isMethod ? ERROR : WARN));
		} else {
			flag(ABSTRACT, WARN, INFO);
		}
		flag(STRICTFP, keepImplementation ? WARN : DEBUG);
		flag(DECLARED_SYNCHRONIZED, keepImplementation ? WARN : DEBUG);

		// Extra
		flag(BRIDGE, DEBUG);
		flag(SYNTHETIC, DEBUG);

	}

}
