/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.multidex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.ZipDexContainer;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

public class MultiDexIO {

	public static final int DEFAULT_MAX_THREADS = 4;

	private static final int PER_THREAD_BATCH_SIZE = 100;

	public interface Logger {

		void log(File file, String entryName, int typeCount);

	}

	private MultiDexIO() {}

	// Read

	public static DexFile readDexFile(boolean multiDex, File file, DexFileNamer namer, Opcodes opcodes,
			MultiDexIO.Logger logger) throws IOException {
		MultiDexContainer<? extends DexFile> container = readMultiDexContainer(multiDex, file, namer, opcodes);
		if (logger != null) {
			for (String name : container.getDexEntryNames()) {
				logger.log(file, name, container.getEntry(name).getClasses().size());
			}
		}
		return new MultiDexContainerBackedDexFile<>(container, opcodes);
	}

	public static MultiDexContainer<? extends DexFile> readMultiDexContainer(boolean multiDex, File file,
			DexFileNamer namer, Opcodes opcodes) throws IOException {
		MultiDexContainer<? extends DexFile> container = readMultiDexContainer(file, namer, opcodes);
		if (!multiDex && container.getDexEntryNames().size() != 1) throw new MultiDexDetectedException(file.toString());
		return container;
	}

	public static MultiDexContainer<? extends DexFile> readMultiDexContainer(File file, DexFileNamer namer,
			Opcodes opcodes) throws IOException {
		if (file.isDirectory()) return new DirectoryDexContainer(file, namer, opcodes);
		if (!file.isFile()) throw new FileNotFoundException(file.toString());
		MultiDexContainer<? extends DexFile> container = DexFileFactory.loadDexContainer(file, opcodes);
		if (container instanceof ZipDexContainer) container = new FilteredMultiDexContainer<>(container, namer, true);
		return container;
	}

	public static DexBackedDexFile readRawDexFile(File file, Opcodes opcodes) throws IOException {
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
		try {
			return DexBackedDexFile.fromInputStream(opcodes, inputStream);
		} finally {
			inputStream.close();
		}
	}

	// Write

	public static int writeDexFile(boolean multiDex, File file, DexFileNamer namer, DexFile dexFile,
			MultiDexIO.Logger logger) throws IOException {
		return writeDexFile(multiDex, 1, file, namer, dexFile, logger);
	}

	public static int writeDexFile(boolean multiDex, int threadCount, File file, DexFileNamer namer,
			DexFile dexFile, MultiDexIO.Logger logger) throws IOException {
		if (file.isDirectory()) {
			return writeMultiDexDirectory(multiDex, threadCount, file, namer, dexFile, logger);
		} else {
			if (multiDex) throw new RuntimeException("Must output to a directory if multi-dex mode is enabled");
			writeRawDexFile(file, dexFile, logger);
			return 1;
		}
	}

	private static class NameIterator implements Iterator<String> {

		private final DexFileNamer namer;
		private int count;

		public NameIterator(DexFileNamer namer) {
			this.namer = namer;
		}

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public String next() {
			return namer.getName(count++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public int getCount() {
			return count;
		}

	}

	public static int writeMultiDexDirectory(boolean multiDex, int threadCount, File directory,
			DexFileNamer namer, DexFile dexFile, MultiDexIO.Logger logger) throws IOException {
		purgeMultiDexDirectory(multiDex, directory, namer);
		NameIterator nameIterator = new NameIterator(namer);
		if (threadCount <= 0) {
			threadCount = Runtime.getRuntime().availableProcessors();
			if (threadCount > DEFAULT_MAX_THREADS) threadCount = DEFAULT_MAX_THREADS;
		}
		if (multiDex && threadCount > 1) {
			writeMultiDexMultiThread(threadCount, directory, nameIterator, dexFile, logger);
		} else {
			writeMultiDexSingleThread(multiDex, directory, nameIterator, dexFile, logger);
		}
		return nameIterator.getCount();
	}

	public static void writeRawDexFile(File file, DexFile dexFile, MultiDexIO.Logger logger) throws IOException {
		writeCommonSingleThread(false, file, null, file.toString(), file, dexFile, logger);
	}

	private static void writeMultiDexSingleThread(boolean multiDex, File directory, NameIterator nameIterator,
			DexFile dexFile, MultiDexIO.Logger logger) throws IOException {
		writeCommonSingleThread(multiDex, directory, nameIterator, null, null, dexFile, logger);
	}

	private static void writeCommonSingleThread(boolean multiDex, File base, NameIterator nameIterator,
			String currentName, File currentFile, DexFile dexFile, MultiDexIO.Logger logger) throws IOException {
		Set<? extends ClassDef> classes = dexFile.getClasses();
		int minMainDexClassCount = (multiDex ? 0 : classes.size());
		Object lock = new Object();
		synchronized (lock) {       // avoid multiple synchronizations in single-threaded mode
			writeMultiDexCommon(minMainDexClassCount, base, nameIterator, currentName, currentFile,
					classes.iterator(), dexFile.getOpcodes(), logger, lock);
		}
	}

	public static void writeMultiDexMultiThread(int threadCount, final File directory, final NameIterator nameIterator,
			final DexFile dexFile, final MultiDexIO.Logger logger) throws IOException {
		final Iterator<? extends ClassDef> classIterator = dexFile.getClasses().iterator();
		final Object lock = new Object();
		List<Callable<Void>> callables = new ArrayList<>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws IOException {
					writeMultiDexCommon(0, directory, nameIterator, null, null, classIterator,
							dexFile.getOpcodes(), logger, lock);
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

	private static <T> void fillQueue(Queue<T> queue, Iterator<? extends T> iterator, int targetSize) {
		for (int i = queue.size(); i < targetSize; i++) {
			if (!iterator.hasNext()) break;
			queue.add(iterator.next());
		}
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

	private static void writeMultiDexCommon(int minMainDexClassCount, File base, NameIterator nameIterator,
			String currentName, File currentFile, Iterator<? extends ClassDef> classIterator, Opcodes opcodes,
			MultiDexIO.Logger logger, Object lock) throws IOException {
		Deque<ClassDef> queue = new ArrayDeque<>(PER_THREAD_BATCH_SIZE);
		ClassDef currentClass = getQueueItem(queue, classIterator, lock);
		do {
			DexPool dexPool = DexPool.makeDexPool(opcodes);
			int fileClassCount = 0;
			while (currentClass != null) {
				dexPool.mark();
				dexPool.internClass(currentClass);
				fileClassCount++;
				if (dexPool.hasOverflowed()) {
					if (fileClassCount <= minMainDexClassCount) throw new DexPoolOverflowException(
							"Dex pool overflowed while writing type " + (fileClassCount) + " of " + minMainDexClassCount);
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

	public static void purgeMultiDexDirectory(boolean multiDex, File directory, DexFileNamer namer) throws IOException {
		List<String> names = new DirectoryDexContainer(directory, namer, null).getDexEntryNames();
		if (!multiDex && names.size() > 1) throw new MultiDexDetectedException(directory.toString());
		for (String name : names) {
			File existingFile = new File(directory, name);
			if (!existingFile.delete()) throw new IOException("Cannot delete file: " + existingFile.toString());
		}
	}

}
