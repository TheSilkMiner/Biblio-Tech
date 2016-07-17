package net.thesilkminer.bibliotech.launcher.crash;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.thesilkminer.bibliotech.launcher.Launcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
			this.addWindowListener(new WindowAdapter() {
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

			final JTextArea crashReport = new JTextArea();
			crashReport.setEditable(false);
			crashReport.setBorder(new BevelBorder(BevelBorder.RAISED));
			crashReport.setText(report.toString());
			crashReport.setBackground(new Color(40, 40, 40).brighter());

			final JScrollPane scrollable = new JScrollPane(crashReport,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollable.setMinimumSize(new Dimension(800, 1000));
			constraints.gridx = 0;
			constraints.gridy = 1;
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
			System.exit(-4);
		}
	}

	private CrashReport populateReport(final Thread t, final Throwable thr) {
		final CrashReport report = new CrashReport(thr, t);
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
					System.exit(-5);
				}
			});
		}
		return report;
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
