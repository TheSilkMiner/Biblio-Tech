package net.thesilkminer.bibliotech.launcher.crash;

import com.google.common.base.Throwables;

import org.jetbrains.annotations.Contract;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Represents a category of a crash report.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public interface ICrashReportCategory extends ICrashInfoProvider {

	/**
	 * Type used to identify all the defaults crash report categories.
	 *
	 * @author TheSilkMiner
	 *
	 * @since 0.1
	 */
	enum Defaults {
		;
		public static final ICrashReportCategory HEAD = new ICrashReportCategory() {
			@Contract(pure = true)
			@Nonnull
			@Override
			public String name() {
				return "Head";
			}

			@Override
			public void provideInfo(final CrashReport report, final StringBuilder builder) {
				builder.append("Stacktrace:\n");
				boolean skip = false;
				for (final StackTraceElement ele : report.throwable.getStackTrace()) {
					if (ele.toString().contains("net.thesilkminer.bibliotech.launcher")) skip = true;
					if (skip) continue;
					builder.append(this.indent()).append(ele.toString()).append("\n");
				}
				builder.append("\n");
			}
		};
		public static final ICrashReportCategory INITIALIZATION = new ICrashReportCategory() {
			@Contract(pure = true)
			@Nonnull
			@Override
			public String name() {
				return "Initialization";
			}

			@Override
			public void provideInfo(final CrashReport report, final StringBuilder builder) {
				builder.append("Stacktrace:\n");
				boolean skip = true;
				for (final StackTraceElement ele : report.throwable.getStackTrace()) {
					if (ele.toString().contains("net.thesilkminer.bibliotech.launcher")) skip = false;
					if (skip) continue;
					builder.append(this.indent()).append(ele.toString()).append("\n");
				}
			}
		};
		public static final ICrashReportCategory THREAD_DETAILS = new ICrashReportCategory() {
			@Contract(pure = true)
			@Nonnull
			@Override
			public String name() {
				return "Thread Details";
			}

			@Override
			public void provideInfo(final CrashReport report, final StringBuilder builder) {
				builder.append(this.indent()).append("Name: ").append(report.thread().getName()).append("\n");
				builder.append(this.indent()).append("Priority: ").append(report.thread().getPriority()).append("\n");
			}
		};
		public static final ICrashReportCategory THREAD_STACKS = new ICrashReportCategory() {
			@Contract(pure = true)
			@Nonnull
			@Override
			public String name() {
				return "Thread Stacks";
			}

			@Override
			public void provideInfo(final CrashReport report, final StringBuilder builder) {
				try {
					this.provideInfo$task(builder);
				} catch (final Exception e) {
					Throwables.propagate(e);
				}
			}

			private void provideInfo$task(final StringBuilder builder) throws Exception {
				EventQueue.invokeAndWait(() -> {
					final Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
					stacks.entrySet().stream().forEach(stack -> {
						builder.append(this.indent()).append("Thread: ").append(stack.getKey().getName()).append("\n");
						builder.append(this.indent()).append("Stacktrace:\n");
						Arrays.stream(stack.getValue()).forEach(element ->
								builder.append(this.indent())
										.append(this.indent()).append(element.toString()).append("\n"));
						builder.append(this.indent()).append("\n");
					});
				});
			}
		};
		public static final ICrashReportCategory PROVIDERS = new ICrashReportCategory() {
			@Contract(pure = true)
			@Nonnull
			@Override
			public String name() {
				return "Providers";
			}

			@Override
			public void provideInfo(final CrashReport report, final StringBuilder builder) {
				builder.append("Providers amount: ").append(CrashReportHandler.INSTANCE.providers().size()).append("\n");
				builder.append("Providers list: ");
				CrashReportHandler.INSTANCE.providers().forEach(provider -> builder.append(provider.id()).append(", "));
				builder.append("\n");
			}
		};
		public static final ICrashReportCategory PROVIDERS_INFO = new ICrashReportCategory() {
			@Contract(pure = true)
			@Nonnull
			@Override
			public String name() {
				return "Providers Information";
			}

			@Override
			public void provideInfo(final CrashReport report, final StringBuilder builder) {
				try {
					this.provideInfo$task(report, builder);
				} catch (final Exception e) {
					Throwables.propagate(e);
				}
			}

			private void provideInfo$task(final CrashReport report, final StringBuilder builder) throws Exception {
				EventQueue.invokeAndWait(() ->
					CrashReportHandler.INSTANCE.providers().forEach(provider -> {
						builder.append(this.indent()).append("-- ").append(provider.id()).append(" --\n");
						try {
							this.provideInfo$task$task(provider, report, builder);
						} catch (final RuntimeException e) {
							builder.append(this.indent()).append("Error occurred while obtaining provider information.")
									.append("\n").append(this.indent()).append("Provider skipped.").append("\n");
						}
					})
				);
			}

			private void provideInfo$task$task(final CrashReportHandler.CrashInfoProviderRegister provider,
			                                   final CrashReport report,
			                                   final StringBuilder builder) {
				final StringBuilder tmp = new StringBuilder();
				provider.provider().provideInfo(report, tmp);
				final String[] messages = tmp.toString().split("\\n");
				Arrays.stream(messages).forEach(message ->
						builder.append(this.indent()).append(this.indent()).append(message).append("\n"));
				builder.append("\n");
			}
		};
		public static final ICrashReportCategory SYSTEM_DETAILS = new ICrashReportCategory() {
			@Contract(pure = true)
			@Nonnull
			@Override
			public String name() {
				return "System details";
			}

			@Override
			public void provideInfo(final CrashReport report, final StringBuilder builder) {
				builder.append("Details:\n");
				builder.append(this.indent()).append("Biblio-Tech version: ").append(report.version()).append("\n");
				builder.append(this.indent()).append("Operating system: ").append(report.os().details()).append("\n");
				builder.append(this.indent()).append("Java version: ").append(report.java()).append("\n");
				builder.append(this.indent()).append("Java VM version: ").append("").append("\n"); //TODO
				builder.append(this.indent()).append("Memory: ").append("").append("\n"); //TODO
				builder.append(this.indent()).append("JVM Flags: ").append("").append("\n"); //TODO
				builder.append(this.indent()).append("Software status: ")
						.append(report.softwareStatus()).append("\n");
				builder.append(this.indent()).append("Type: ").append(report.platformType()).append("\n");
				builder.append(this.indent()).append("Current language: ").append(report.languageCode()).append("\n");
			}
		};
	}

	String name();

	/**
	 * Provides information to the crash report.
	 *
	 * <p>Keep in mind that indentation is <strong>NOT</strong> added
	 * automatically, so you need to handle it yourself. You can use
	 * {@link #indent()} if you want.</p>
	 *
	 * @param report
	 *      The constructed crash report. Can be useful to get the various causes
	 *      or other bits of information.
	 * @param builder
	 *      The builder where all information should be added.
	 *
	 * @since 0.1
	 */
	@Override
	void provideInfo(final CrashReport report, final StringBuilder builder);

	/**
	 * Adds an indentation.
	 *
	 * <p>You should not implement this method on your own.</p>
	 *
	 * @return
	 *      A string used to indent text.
	 *
	 * @since 0.1
	 */
	default String indent() {
		return "\t";
	}
}
