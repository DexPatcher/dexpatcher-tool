package lanchon.dexpatcher;

import static lanchon.dexpatcher.Logger.Level.*;

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
	private final int[] counts;

	public Logger(Level logLevel) {
		this.logLevel = logLevel;
		counts = new int[Level.values().length];
	}

	public final void log(Level level, String message) {
		if (message == null) throw new NullPointerException("message");
		counts[level.ordinal()]++;
		if (level.ordinal() >= logLevel.ordinal()) doLog (level, message);
	}

	public final boolean ok() {
		int errors = getCount(FATAL) + getCount(ERROR);
		return errors == 0;
	}

	public final void close() {
		int errors = getCount(FATAL) + getCount(ERROR);
		int warnings = getCount(WARN);
		if (errors != 0 || warnings != 0) {
			doLog (NONE, errors + " error(s), " + warnings + " warning(s)");
		}
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	public final int getCount(Level level) {
		return counts[level.ordinal()];
	}

	protected abstract void doLog(Level level, String message);

}
