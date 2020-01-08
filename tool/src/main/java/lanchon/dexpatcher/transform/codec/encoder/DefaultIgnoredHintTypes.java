/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec.encoder;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.Flushable;
import java.io.Serializable;

import lanchon.dexpatcher.core.util.TypeName;

import com.google.common.collect.ImmutableSet;

public final class DefaultIgnoredHintTypes {

	public static final ImmutableSet<String> SET;

	static {

		Class<?>[] types = new Class[] {

				// java.lang
						AutoCloseable.class,
						Cloneable.class,
						Comparable.class,

				// java.io
						Closeable.class,
						Externalizable.class,
						Flushable.class,
						Serializable.class

		};

		ImmutableSet.Builder<String> builder = ImmutableSet.<String>builder();
		for (Class<?> type : types) builder.add(TypeName.toClassDescriptor(type.getName()));
		SET = builder.build();

	}

	private DefaultIgnoredHintTypes() {}

}
