package lanchon.dexpatcher;

public class BasicLogger extends Logger {

	public BasicLogger(Level logLevel) {
		super(logLevel);
	}

	@Override
	protected void doLog(Level level, String message) {
		switch (level) {
		case DEBUG:
		case INFO:
		case WARN:
			System.out.println(level.label + ": " + message);
			break;
		case ERROR:
		case FATAL:
			System.out.flush();
			System.err.println(level.label + ": " + message);
			System.err.flush();
			break;
		case NONE:
			System.out.println(message);
			break;
		default:
			throw new AssertionError("unexpected log level");
		}
	}

}
