package net.thesilkminer.bibliotech.launcher.crash;

/**
 * Marks an information provider for crash reports.
 *
 * <p>Every implementor of this class should add some useful
 * information to the crash report.</p>
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
@FunctionalInterface
public interface ICrashInfoProvider {

	/**
	 * Provides information to the crash report.
	 *
	 * @param report
	 *      The constructed crash report. Can be useful to get the various causes
	 *      or other bits of information.
	 * @param builder
	 *      The builder where all information should be added.
	 *
	 * @since 0.1
	 */
	void provideInfo(final CrashReport report, final StringBuilder builder);
}
