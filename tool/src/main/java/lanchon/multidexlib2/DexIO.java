/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.multidexlib2;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

public class DexIO {

	public static final int DEFAULT_MAX_DEX_POOL_SIZE = 0x10000;

	public interface Logger {
		void log(File file, String entryName, int typeCount);
	}

	private DexIO() {}

	// Single-Threaded Write

	static void writeRawDexSingleThread(File file, DexFile dexFile, int maxDexPoolSize,
			DexIO.Logger logger) throws IOException {
		writeCommonSingleThread(false, file, null, SingletonDexContainer.UNDEFINED_ENTRY_NAME, file, dexFile,
				maxDexPoolSize, logger);
	}

	static void writeMultiDexSingleThread(boolean multiDex, File directory, DexFileNamer.Iterator nameIterator,
			DexFile dexFile, int maxDexPoolSize, DexIO.Logger logger) throws IOException {
		writeCommonSingleThread(multiDex, directory, nameIterator, null, null, dexFile, maxDexPoolSize, logger);
	}

	static void writeCommonSingleThread(boolean multiDex, File base, DexFileNamer.Iterator nameIterator,
			String currentName, File currentFile, DexFile dexFile, int maxDexPoolSize,
			DexIO.Logger logger) throws IOException {
		Set<? extends ClassDef> classes = dexFile.getClasses();
		int minMainDexClassCount = (multiDex ? 0 : classes.size());
		Object lock = new Object();
		synchronized (lock) {       // avoid multiple synchronizations in single-threaded mode
			writeCommon(minMainDexClassCount, base, nameIterator, currentName, currentFile, classes.iterator(),
					dexFile.getOpcodes(), maxDexPoolSize, logger, lock);
		}
	}

	// Multi-Threaded Write

	static void writeMultiDexMultiThread(int threadCount, final File directory,
			final DexFileNamer.Iterator nameIterator, final DexFile dexFile, final int maxDexPoolSize,
			final DexIO.Logger logger) throws IOException {
		final Iterator<? extends ClassDef> classIterator = dexFile.getClasses().iterator();
		final Object lock = new Object();
		List<Callable<Void>> callables = new ArrayList<>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws IOException {
					writeCommon(0, directory, nameIterator, null, null, classIterator, dexFile.getOpcodes(),
							maxDexPoolSize, logger, lock);
					return null;
				}
			});
		}
		ExecutorService service = Executors.newFixedThreadPool(threadCount);
		try {
			List<Future<Void>> futures = service.invokeAll(callables);
			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (ExecutionException e) {
					Throwable c = e.getCause();
					if (c instanceof IOException) throw (IOException) c;
					if (c instanceof RuntimeException) throw (RuntimeException) c;
					if (c instanceof Error) throw (Error) c;
					throw new UndeclaredThrowableException(c);
				}
			}
		} catch (InterruptedException e) {
			InterruptedIOException ioe = new InterruptedIOException();
			ioe.initCause(e);
			throw ioe;
		} finally {
			service.shutdown();
		}
	}

	// Common Code

	private static final int PER_THREAD_BATCH_SIZE = 100;

	private static void writeCommon(int minMainDexClassCount, File base, DexFileNamer.Iterator nameIterator,
			String currentName, File currentFile, Iterator<? extends ClassDef> classIterator, Opcodes opcodes,
			int maxDexPoolSize, DexIO.Logger logger,
			Object lock) throws IOException {
		Deque<ClassDef> queue = new ArrayDeque<>(PER_THREAD_BATCH_SIZE);
		ClassDef currentClass = getQueueItem(queue, classIterator, lock);
		do {
			DexPool dexPool = new DexPool(opcodes);
			int fileClassCount = 0;
			while (currentClass != null) {
				dexPool.mark();
				dexPool.internClass(currentClass);
				fileClassCount++;
				boolean overflow =
						dexPool.typeSection.getItemCount() > maxDexPoolSize ||
						//dexPool.protoSection.getItemCount() > maxDexPoolSize ||
						dexPool.fieldSection.getItemCount() > maxDexPoolSize ||
						dexPool.methodSection.getItemCount() > maxDexPoolSize ||
						//dexPool.classSection.getItemCount() > maxDexPoolSize ||
						false;
				if (overflow) {
					if (fileClassCount <= minMainDexClassCount) throw new DexPoolOverflowException(
							"Dex pool overflowed while writing type " + (fileClassCount) +
							" of " + minMainDexClassCount);
					if (fileClassCount == 1) throw new DexPoolOverflowException(
							"Type too big for dex pool: " + currentClass.getType());
					dexPool.reset();
					fileClassCount--;
					break;
				}
				currentClass = getQueueItem(queue, classIterator, lock);
			}
			synchronized (lock) {
				if (currentFile == null) {
					currentName = nameIterator.next();
					currentFile = new File(base, currentName);
				}
				if (logger != null) logger.log(base, currentName, fileClassCount);
				fillQueue(queue, classIterator, PER_THREAD_BATCH_SIZE - 1);
			}
			dexPool.writeTo(new FileDataStore(currentFile));
			currentFile = null;
			minMainDexClassCount = 0;
		} while (currentClass != null);
	}

	private static <T> T getQueueItem(Queue<T> queue, Iterator<? extends T> iterator, Object lock) {
		T item = queue.poll();
		if (item == null) {
			synchronized (lock) {
				fillQueue(queue, iterator, PER_THREAD_BATCH_SIZE);
			}
			item = queue.poll();
		}
		return item;
	}

	private static <T> void fillQueue(Queue<T> queue, Iterator<? extends T> iterator, int targetSize) {
		for (int i = queue.size(); i < targetSize; i++) {
			if (!iterator.hasNext()) break;
			queue.add(iterator.next());
		}
	}

}
