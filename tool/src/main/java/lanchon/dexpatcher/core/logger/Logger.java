package lanchon.dexpatcher.core.logger;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public abstract class Logger {

	public enum Level {

		DEBUG("debug"),
		INFO("info"),
		WARN("warning"),
		ERROR("error"),
		FATAL("fatal"),
		NONE("none");

		private final String label;

		Level(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

	private Level logLevel;
	private int[] counts;

	public Logger() {
		this.logLevel = DEBUG;
		clearMessageCounts();
	}

	public final void log(Level level, String message) {
		log(level, message, null);
	}

	public final void log(Level level, String message, Throwable throwable) {
		if (message == null) throw new NullPointerException("message");
		counts[level.ordinal()]++;
		if (isLogging(level)) doLog(level, message, throwable);
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	public final boolean isLogging(Level level) {
		return level.ordinal() >= logLevel.ordinal();
	}

	public int getMessageCount(Level level) {
		return counts[level.ordinal()];
	}

	public void clearMessageCounts() {
		counts = new int[Level.values().length];
	}

	public boolean hasNotloggedErrors() {
		int errors = getMessageCount(FATAL) + getMessageCount(ERROR);
		return errors == 0;
	}

	public void logErrorAndWarningCounts() {
		int errors = getMessageCount(FATAL) + getMessageCount(ERROR);
		int warnings = getMessageCount(WARN);
		if (errors != 0 || warnings != 0) {
			log(NONE, errors + " error(s), " + warnings + " warning(s)");
		}
	}

	protected abstract void doLog(Level level, String message, Throwable throwable);

	public abstract void flush();
	public abstract void close();

}
