/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package com.google.common.io;

import java.io.IOException;
import java.io.InputStream;

// TODO: Remove hack when issue is fixed: https://github.com/google/guava/issues/2616

public class ByteStreamsHack {

	public static byte[] toByteArray(InputStream inputStream, long expectedSize) throws IOException {

		// WARNING: This implementation relies on non-public API of Guava.

		// An equivalent -though inefficient- implementation using public-only API is:
		//return ByteStreams.toByteArray(inputStream);

		if (expectedSize < 0) expectedSize = 0;
		return Files.readFile(inputStream, expectedSize);

	}

	private ByteStreamsHack() {}

}
