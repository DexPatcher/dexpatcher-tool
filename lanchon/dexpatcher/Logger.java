package lanchon.dexpatcher;

public abstract class Logger {

	public enum Level {
		DEBUG(0, "debug"),
		INFO(1, "info"),
		WARN(2, "warning"),
		ERROR(3, "error"),
		FATAL(4, "fatal"),
		NONE(5, "none");

		public final int level;
		public final String label;

		Level(int level, String label)
		{
			this.level = level;
			this.label = label;
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
		counts[level.level]++;
		if (level.level >= logLevel.level) doLog (level, message);
	}

	public final boolean ok() {
		int errors = counts[Level.FATAL.level] + counts[Level.ERROR.level];
		return errors == 0;
	}

	public final void close() {
		int errors = counts[Level.FATAL.level] + counts[Level.ERROR.level];
		int warnings = counts[Level.WARN.level];
		if (errors != 0 || warnings != 0) {
			doLog (Level.NONE, errors + " error(s), " + warnings + " warning(s)");
		}
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	public final int getCount(Level level) {
		return counts[level.level];
	}

	protected abstract void doLog(Level level, String message);

}
