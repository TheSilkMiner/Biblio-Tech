package net.thesilkminer.bibliotech.launcher.crash;

import com.google.common.collect.ImmutableList;

import net.thesilkminer.bibliotech.launcher.os.Os;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Represents a crash report, which is constructed every time an error is thrown.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public class CrashReport {

	private enum SoftwareStatus {

		VANILLA,
		DEVELOPMENT_VERSION,
		UNLIKELY_MODDED,
		MODDED,
		FORGED;

		@NotNull
		@Override
		public String toString() {
			final String[] names = this.name().split("_");
			final StringBuilder toString = new StringBuilder();
			Arrays.stream(names).forEach(name -> {
				toString.append(name.substring(0, 1));
				toString.append(name.substring(1).toLowerCase());
				toString.append(' ');
			});
			return toString.toString();
		}

		private static SoftwareStatus test() {
			SoftwareStatus current;
			try {
				Class.forName("net.thesilkminer.bibliotech.forge.Forge");
				current = SoftwareStatus.FORGED;
			} catch (final ClassNotFoundException e) {
				try {
					Class.forName("net.thesilkminer.bibliotech.fml.FML");
					current = SoftwareStatus.MODDED;
				} catch (final ClassNotFoundException ex) {
					try {
						if (new java.io.File(SoftwareStatus.class.getResource("/META-INF/SECURITY.TSM").getFile())
								.exists()) {
							throw new ClassNotFoundException();
						}
						current = SoftwareStatus.UNLIKELY_MODDED;
					} catch (final ClassNotFoundException exc) {
						try {
							if ("false".equalsIgnoreCase(
									System.getProperty("net.thesilkminer.bibliotech.generic.debug.idea", "false")
								)) {
								throw new ClassNotFoundException();
							}
							current = SoftwareStatus.DEVELOPMENT_VERSION;
						} catch (final ClassNotFoundException exception) {
							current = SoftwareStatus.VANILLA;
						}
					}
				}
			}
			return current;
		}
	}

	private enum PlatformType {
		CLIENT,
		SERVER;

		@NotNull
		@Override
		public String toString() {
			return this.name().substring(0, 1).concat(this.name().substring(1).toLowerCase());
		}

		public static PlatformType test() {
			try {
				Class.forName("net.thesilkminer.bibliotech.launcher.ui.LauncherFrame");
				return PlatformType.CLIENT;
			} catch (final ClassNotFoundException e) {
				return PlatformType.SERVER;
			}
		}
	}

	private static final String SC = "Suppressed: ";
	private static final String CC = "Caused by: ";
	private static final List<String> WITTY_COMMENTS = ImmutableList.of(
			"Who set us up the TNT?",
			"Everything's going to plan. No, really, that was supposed to happen.",
			"Uh... Did I do that?",
			"Oops.",
			"Why did you do that?",
			"I feel sad now :(",
			"My bad.",
			"I'm sorry, Dave.",
			"I let you down. Sorry :(",
			"On the bright side, I bought you a teddy bear!",
			"Daisy, daisy...",
			"Oh - I know what I did wrong!",
			"Hey, that tickles! Hehehe!",
			"I blame TheSilkMiner", //Dinnerbone
			"You should also try out our other software: Book-Tech", // "Minceraft",
			"Don't be sad. I'll do better next time, I promise!",
			"Don't be sad, have a hug! <3",
			"I just don't know what went wrong :(",
			"Shall we play a game?",
			"Quite honestly, I wouldn't worry myself about that.",
			"I bet Cylons wouldn't have this problem",
			"Sorry :(",
			"Surprise! Haha. Well, this is awkward.",
			"Would you like a cupcake?",
			"Hi I'm Biblio-Tech and I'm a crashaholic", //Minecraft
			"Ooh. Shiny.",
			"That doesn't make any sense!",
			"Why is it breaking :(",
			"Don't do that",
			"Ouch. That hurt :(",
			"You're mean.",
			"This is token for 1 free hug. Redeem at your nearest TSM Inc. Employee: [~~HUG~~]", //Mojangsta
			"There are four lights!",
			"Witty comment unavailable :("
	);
	private static final List<ICrashReportCategory> CATEGORIES = ImmutableList.of(
			ICrashReportCategory.Defaults.HEAD,
			ICrashReportCategory.Defaults.INITIALIZATION,
			ICrashReportCategory.Defaults.THREAD_DETAILS,
			ICrashReportCategory.Defaults.THREAD_STACKS,
			ICrashReportCategory.Defaults.PROVIDERS,
			ICrashReportCategory.Defaults.PROVIDERS_INFO,
			ICrashReportCategory.Defaults.SYSTEM_DETAILS
	);

	private Date generationTime;
	private String description;
	public Throwable throwable; //Made public because Idea derp
	private Thread thread;
	private String version;
	private Os os;
	private String java;
	private SoftwareStatus softwareStatus;
	private PlatformType platformType;
	private String languageCode;

	public CrashReport(final Throwable t, final Thread thread) {
		this.generationTime = new Date(System.currentTimeMillis());
		this.description = t.getMessage();
		this.throwable = t;
		this.thread = thread;
		this.version = "v0.1";
		this.os = Os.getCurrentOs();
		this.java = System.getProperty("java.version")
				+ ", "
				+ System.getProperty("java.vendor");
		this.softwareStatus = SoftwareStatus.test();
		this.platformType = PlatformType.test();
		this.languageCode = this.platformType.equals(PlatformType.SERVER)? "en_US" : "~~TODO~~"; //TODO
	}

	public Date generationTime() {
		return this.generationTime;
	}

	public String description() {
		return this.description;
	}

	public void description(final String description) {
		if (this.description == null || this.description.equals(throwable.getMessage())) this.description = description;
	}

	public Thread thread() {
		return this.thread;
	}

	public String version() {
		return this.version;
	}

	public Os os() {
		return this.os;
	}

	public String java() {
		return this.java;
	}

	public SoftwareStatus softwareStatus() {
		return this.softwareStatus;
	}

	public PlatformType platformType() {
		return this.platformType;
	}

	public String languageCode() {
		return this.languageCode;
	}

	private String generateWittyComment() {
		try {
			return "// " + WITTY_COMMENTS.get(new Random(System.nanoTime()).nextInt(WITTY_COMMENTS.size() - 1)) + "\n";
		} catch (final Throwable throwable) {
			return "// " + WITTY_COMMENTS.get(WITTY_COMMENTS.size() - 1) + "\n";
		}

	}

	@Contract(pure = true)
	@NotNull
	private String indent() {
		return "\t";
	}

	/*
	 * A copy of the method present in Throwable.class edited so that the stacktrace can
	 * be written to a StringBuilder
	 */
	private void printStackTrace(final Throwable throwable, final StringBuilder build) {
		final Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
		seen.add(throwable);
		build.append(throwable).append("\n");

		Arrays.stream(throwable.getStackTrace()).forEach(ele ->
				build.append(this.indent()).append("at ").append(ele).append("\n"));

		Arrays.stream(throwable.getSuppressed()).forEach(thr ->
			this.printEnclosedStackTrace(thr, build, throwable.getStackTrace(), SC, this.indent(), seen)
		);

		if (throwable.getCause() != null)
			this.printEnclosedStackTrace(throwable.getCause(), build, throwable.getStackTrace(), CC, "", seen);
	}

	/*
	 * A copy of the method present in Throwable.class edited so that the stacktrace can
	 * be written to a StringBuilder
	 */
	private void printEnclosedStackTrace(final Throwable throwable,
	                                     final StringBuilder build,
	                                     final StackTraceElement[] enclosing,
	                                     final String caption,
	                                     final String prefix,
	                                     final Set<Throwable> seen) {
		if (seen.contains(throwable)) {
			build.append(this.indent()).append("[CIRCULAR REFERENCE: ").append(throwable).append("]\n");
			return;
		}

		seen.add(throwable);

		final StackTraceElement[] trace = throwable.getStackTrace();
		int m = trace.length - 1;
		int n = enclosing.length - 1;

		while (m >= 0 && n >=0 && trace[m].equals(enclosing[n])) {
			m--;
			n--;
		}

		final int framesInCommon = trace.length - 1 - m;

		build.append(prefix).append(caption).append(throwable).append("\n");

		for (int i = 0; i <= m; i++) {
			build.append(prefix).append(this.indent()).append("at ").append(trace[i]).append("\n");
		}

		if (framesInCommon != 0) {
			build.append(prefix).append(this.indent()).append("... ").append(framesInCommon).append(" more\n");
		}

		Arrays.stream(throwable.getSuppressed()).forEach(thr ->
			this.printEnclosedStackTrace(thr, build, trace, SC, prefix + this.indent(), seen));

		if (throwable.getCause() != null)
			this.printEnclosedStackTrace(throwable.getCause(), build, trace, CC, prefix, seen);
	}

	@Override
	public String toString() {
		final StringBuilder build = new StringBuilder(100);
		build.append("---- Biblio-Tech Crash Report ----\n");
		build.append(this.generateWittyComment());
		build.append("\n");
		build.append("Time: ").append(this.generationTime()).append("\n");
		build.append("Description: ").append(this.description()).append("\n");
		build.append("\n");
		this.printStackTrace(this.throwable, build);
		build.append("\n");
		build.append("A detailed walk-through of the error, its code path and all known details is as follows:\n");
		build.append("----------------------------------------------------------------------------------------\n");
		build.append("\n");
		CATEGORIES.stream().forEach(category -> {
			build.append("-- ").append(category.name()).append(" --\n");
			category.provideInfo(this, build);
			build.append("\n");
		});
		return build.toString();
	}
}
