package net.thesilkminer.bibliotech.launcher.crash;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.thesilkminer.bibliotech.launcher.Launcher;
import net.thesilkminer.bibliotech.launcher.logging.Logger;
import net.thesilkminer.bibliotech.launcher.os.Os;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Cursor;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

/**
 * Generates a crash report for all unhandled exceptions.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public enum CrashReportHandler {

	INSTANCE;

	private static class CrashFrame extends JFrame {

		CrashFrame(final CrashReport report) {
			super();
			this.setTitle("Crash Handler - Biblio-Tech");
			this.setLayout(new GridBagLayout());
			this.setResizable(false);
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowDeiconified(final WindowEvent e) {
					CrashFrame.this.setExtendedState(CrashFrame.this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
				}

				@Override
				public void windowClosing(final WindowEvent e) {
					System.exit(-1);
				}
			});

			final GridBagConstraints constraints = new GridBagConstraints();
			final Random rng = new Random(new Random(System.nanoTime()).nextLong());

			final JLabel title = new JLabel((rng.nextInt(1000) != 0? "Biblio-Tech" : "Book-Tech") + " has crashed!");
			title.setFont(title.getFont().deriveFont(40.0f));
			title.setVerticalAlignment(SwingConstants.CENTER);
			title.setHorizontalAlignment(SwingConstants.CENTER);
			title.setBounds(0, 0, 100, 40);
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.ipady = 0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.PAGE_START;
			this.add(title, constraints);

			final JLabel savedNotice = new JLabel("This crash has been saved to crash-reports directory");
			savedNotice.setVerticalAlignment(SwingConstants.CENTER);
			savedNotice.setHorizontalAlignment(SwingConstants.CENTER);
			savedNotice.setForeground(Color.YELLOW);
			savedNotice.setBounds(0, 0, 100, 40);
			savedNotice.setToolTipText("Opens the crash-reports directory");
			savedNotice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			savedNotice.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (!Desktop.isDesktopSupported()) return;
					try {
						Desktop.getDesktop().open(new File(Os.getCurrentOs().workingDir(), "crash-reports"));
						CrashFrame.this.setExtendedState(CrashFrame.this.getExtendedState() | JFrame.ICONIFIED);
					} catch (final IOException exception) {
						JOptionPane.showMessageDialog(CrashFrame.this,
								"Error occurred while attempting to open directory",
								"Directory opening failed",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			});
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.ipady = 0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			this.add(savedNotice, constraints);

			final JTextArea crashReport = new JTextArea();
			crashReport.setEditable(false);
			crashReport.setBorder(new BevelBorder(BevelBorder.RAISED));
			crashReport.setText(report.toString());
			crashReport.setBackground(new Color(40, 40, 40).brighter());
			crashReport.setCaretPosition(0);

			final JScrollPane scrollable = new JScrollPane(crashReport,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollable.setMinimumSize(new Dimension(800, 1000));
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.weightx = 10;
			constraints.weighty = 100;
			constraints.ipady = 0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.PAGE_END;
			this.add(scrollable, constraints);
		}
	}

	static final class CrashInfoProviderRegister implements Comparable<CrashInfoProviderRegister> {
		private final String id;
		private final int priority;
		private final ICrashInfoProvider provider;

		CrashInfoProviderRegister(final String id, final int priority, final ICrashInfoProvider provider) {
			this.id = id;
			this.priority = Math.max(0, priority);
			this.provider = provider;
		}

		@Contract(pure = true)
		public final String id() {
			return this.id;
		}

		@Contract(pure = true)
		public final int priority() {
			return this.priority;
		}

		@Contract(pure = true)
		public final ICrashInfoProvider provider() {
			return this.provider;
		}

		@Override
		public int compareTo(@Nonnull final CrashInfoProviderRegister o) {
			return -this.compareTo$opposite(o);
		}

		public int compareTo$opposite(@Nonnull final CrashInfoProviderRegister o) {
			if (this.priority() != o.priority()) return Integer.compare(this.priority(), o.priority());
			return this.id().compareTo(o.id());
		}

		@NotNull
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("id", id)
					.add("priority", priority)
					.add("provider", provider)
					.toString();
		}
	}

	private final Set<CrashInfoProviderRegister> providers = Sets.newTreeSet();

	public final void constructReport(final Thread thread, final Throwable t) {
		try {
			this.constructReport(this.populateReport(thread, t));
		} catch (final Throwable throwable) {
			// Let's exit otherwise we get stuck in a loop
			JOptionPane.showMessageDialog(null,
					"Error while creating crash report",
					"Unexpected exception",
					JOptionPane.ERROR_MESSAGE);
			// Output to file (through logger) so that it keeps track of the exception
			// We will never know what caused the crash otherwise
			this.outputThrowable(t);
			System.exit(-3);
		}
	}

	public final void constructReport(final CrashReport report) {
		try {
			final JFrame frame = new CrashFrame(report);
			frame.pack();
			frame.setAlwaysOnTop(true);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			frame.setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
			frame.setVisible(true);
		} catch (final Throwable t) {
			// Let's exit otherwise we get stuck in a loop
			JOptionPane.showMessageDialog(null,
					"Error while creating crash report",
					"Unexpected exception",
					JOptionPane.ERROR_MESSAGE);
			// Output to file (through logger) so that it keeps track of the exception
			// We will never know what caused the crash otherwise
			this.outputThrowable(t);
			System.exit(-4);
		}
	}

	private CrashReport populateReport(final Thread t, final Throwable thr) {
		final CrashReport report = new CrashReport(thr, t);
		this.handleReportedException(report, thr);
		try {
			this.saveReport(report);
		} catch (final IOException e) {
			// Unable to save crash report
			// Fine
			// Save it to log then, baby
			this.outputThrowable(thr);
		}
		return report;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private boolean saveReport(final CrashReport report) throws IOException {
		final File btDir = Os.getCurrentOs().workingDir();
		final File crashDirectory = new File(btDir, "crash-reports");
		if (!btDir.exists() || !crashDirectory.exists()) crashDirectory.mkdirs();
		final Calendar calendar = new GregorianCalendar();
		final File crashFile = new File(crashDirectory, String.format(
				"crash-%s-%02d.%02d.%02d-%d.txt",
				String.valueOf(report.platformType()).toLowerCase(java.util.Locale.ENGLISH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.YEAR),
				System.currentTimeMillis()
		));
		final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(crashFile)));
		writer.print(report);
		writer.close();
		return crashFile.exists();
	}

	private void handleReportedException(final CrashReport report, final Throwable thr) {
		if (thr instanceof ReportedException) {
			final ReportedException exception = (ReportedException) thr;
			report.description(exception.description());
			exception.customProviders().forEach(providerPair -> {
				try {
					this.registerProvider(providerPair.getLeft(),
							100,
							providerPair.getRight());
				} catch (final Throwable throwable) {
					// Let's exit otherwise we get stuck in a loop
					JOptionPane.showMessageDialog(null,
							"Error while creating crash report",
							"Unexpected exception",
							JOptionPane.ERROR_MESSAGE);
					// Output to file (through logger) so that it keeps track of the exception
					// We will never know what caused the crash otherwise
					this.outputThrowable(throwable);
					System.exit(-5);
				}
			});
		} else if (thr.getCause() != null) {
			try {
				this.handleReportedException(report, thr.getCause());
			} catch (final StackOverflowError e) {
				this.outputThrowable(e);
			}
		}
	}

	private void outputThrowable(final Throwable t) {
		final Logger log = Logger.obtain("Crash Handler");
		log.warning("********************************************************************************");
		log.warning("*          Unable to construct crash report for this particular crash          *");
		log.warning("********************************************************************************");
		log.warning("*  An unknown error has occurred while attempting to prompt the crash report   *");
		log.warning("* to the user.                                                                 *");
		log.warning("*                                                                              *");
		log.warning("*  After this error message, the throwable will be printed to file and on the  *");
		log.warning("* on the console. Please read the stacktrace carefully: a mod may have caused  *");
		log.warning("* this. If you don't have any mod installed and/or you are able to reproduce   *");
		log.warning("* this in a vanilla environment, then please contact the developer and submit  *");
		log.warning("* a bug report. Attach this log file and accurate reproduction steps.          *");
		log.warning("*                                                                              *");
		log.warning("*  TO THE DEVELOPERS: If you are unsure about what happened, ask the lead dev, *");
		log.warning("* do NOT attempt to fix this if you don't know what to do: it makes things     *");
		log.warning("* worse.                                                                       *");
		log.warning("********************************************************************************");
		log.warning("*                                                                              *");
		log.warning("********************************************************************************");
		log.warning("");
		log.warning("********************************************************************************");
		log.warning("*                               Stacktrace dump                                *");
		log.warning("********************************************************************************");
		this.printStackTrace(t, log);
		log.warning("********************************************************************************");
		log.warning("*                                                                              *");
		log.warning("********************************************************************************");
		log.warning("");
		log.warning("********************************************************************************");
		log.warning("*                                 Thread dump                                  *");
		log.warning("********************************************************************************");
		final Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
		stacks.entrySet().stream().forEach(stack -> {
			log.warning("\tThread: " + stack.getKey().getName());
			log.warning("\tStacktrace:");
			Arrays.stream(stack.getValue()).forEach(ele -> log.warning("\t\t" + ele.toString()));
			log.warning("\t");
		});
		log.warning("********************************************************************************");
		log.warning("*                                                                              *");
		log.warning("********************************************************************************");
		JOptionPane.showMessageDialog(null,
				"Please read the console output",
				t.getMessage().concat(" - Unexpected exception"),
				JOptionPane.ERROR_MESSAGE);

		new Thread(() -> {
			try {
				Thread.sleep(10000);
			} catch (final InterruptedException ignored) {
				// We don't really care about this
			}
			System.exit(-6);
		});
	}

	/*
	 * A copy of the method present in Throwable.class edited so that the stacktrace can
	 * be written to a StringBuilder
	 */
	private void printStackTrace(final Throwable throwable, final Logger log) {
		final Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
		seen.add(throwable);
		log.warning(throwable.toString());

		Arrays.stream(throwable.getStackTrace()).forEach(ele ->	log.warning(String.format("\tat %s", ele)));

		Arrays.stream(throwable.getSuppressed()).forEach(thr ->
				this.printEnclosedStackTrace(thr, log, throwable.getStackTrace(), "Suppressed: ", "\t", seen)
		);

		if (throwable.getCause() != null)
			this.printEnclosedStackTrace(throwable.getCause(), log, throwable.getStackTrace(), "Caused by: ", "", seen);
	}

	/*
	 * A copy of the method present in Throwable.class edited so that the stacktrace can
	 * be written to a StringBuilder
	 */
	private void printEnclosedStackTrace(final Throwable throwable,
	                                     final Logger log,
	                                     final StackTraceElement[] enclosing,
	                                     final String caption,
	                                     final String prefix,
	                                     final Set<Throwable> seen) {
		if (seen.contains(throwable)) {
			log.warning(String.format("\t[CIRCULAR REFERENCE: %s]", throwable));
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

		log.warning(prefix + caption + throwable);

		for (int i = 0; i <= m; i++) {
			log.warning(String.format("%s\tat %s", prefix, trace[i]));
		}

		if (framesInCommon != 0) {
			log.warning(String.format("%s\t... %d more", prefix, framesInCommon));
		}

		Arrays.stream(throwable.getSuppressed()).forEach(thr ->
				this.printEnclosedStackTrace(thr, log, trace, "Suppressed: ", prefix + "\t", seen));

		if (throwable.getCause() != null)
			this.printEnclosedStackTrace(throwable.getCause(), log, trace, "Caused by: ", prefix, seen);
	}

	public final boolean registerProvider(final String name, final int priority, final ICrashInfoProvider provider) {
		final CrashInfoProviderRegister register = new CrashInfoProviderRegister(name, priority, provider);
		Launcher.logger().info("Attempting to register crash provider " + register);
		return providers.add(register);
	}

	final Set<CrashInfoProviderRegister> providers() {
		return ImmutableSet.copyOf(this.providers);
	}
}
