/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.logger;

import java.io.PrintStream;
import java.io.PrintWriter;

public class BasicLogger extends Logger {

	private final PrintWriter out;
	private final PrintWriter err;

	public BasicLogger() {
		this(System.out, System.err);
	}

	public BasicLogger(PrintStream out) {
		this(out, null);
	}

	public BasicLogger(PrintWriter out) {
		this(out, null);
	}

	public BasicLogger(PrintStream out, PrintStream err) {
		this(new PrintWriter(out, true), err != null ? new PrintWriter(err, true) : null);
	}

	public BasicLogger(PrintWriter out, PrintWriter err) {
		this.out = out;
		this.err = err;
		flush();
	}

	@Override
	protected void doLog(Level level, String message, Throwable throwable) {
		boolean isError = false;
		switch (level) {
			case ERROR:
			case FATAL:
				isError = true;
			case DEBUG:
			case INFO:
			case WARN:
				message = level.getLabel() + ": " + message;
			case NONE:
				break;
			default:
				throw new AssertionError("Unexpected log level");
		}
		if (isError && err != null) {
			out.flush();
			err.println(message);
			if (throwable != null) throwable.printStackTrace(err);
			err.flush();
		} else {
			out.println(message);
			if (throwable != null) {
				throwable.printStackTrace(out);
				out.flush();
			}
		}
	}

	@Override
	public void flush() {
		out.flush();
		if (err != null) err.flush();
	}

	@Override
	public void close() {
		out.close();
		if (err != null) err.close();
	}

}
