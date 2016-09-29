package lanchon.dexpatcher.core.logger;

public class BasicLogger extends Logger {

	public BasicLogger(Level logLevel) {
		super(logLevel);
	}

	@Override
	protected void doLog(Level level, String message, Throwable throwable) {
		switch (level) {
		case DEBUG:
		case INFO:
		case WARN:
			System.out.println(level.getLabel() + ": " + message);
			if (throwable != null) throwable.printStackTrace(System.out);
			break;
		case ERROR:
		case FATAL:
			System.out.flush();
			System.err.println(level.getLabel() + ": " + message);
			if (throwable != null) throwable.printStackTrace(System.err);
			System.err.flush();
			break;
		case NONE:
			System.out.println(message);
			if (throwable != null) throwable.printStackTrace(System.out);
			break;
		default:
			throw new AssertionError("Unexpected log level");
		}
	}

}
