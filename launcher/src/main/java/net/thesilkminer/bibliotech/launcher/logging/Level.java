package net.thesilkminer.bibliotech.launcher.logging;

/**
 * Identifies the logging level of a certain message.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public enum Level {
	TRACE,
	DEBUG,
	FINEST,
	FINER,
	FINE,
	INFO,
	WARNING,
	ERROR;

	public static Level defaultLevel() {
		try {
			return Level.valueOf(System.getProperty("net.thesilkminer.bibliotech.shared.logging.level", "INFO"));
		} catch (final IllegalArgumentException e) {
			return INFO;
		}
	}
}
