package net.thesilkminer.bibliotech.launcher.ui;

import net.thesilkminer.bibliotech.launcher.Launcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 * Shows the current loading process of the launcher.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public class LoadingFrame extends JFrame {

	public static final LoadingFrame INSTANCE = new LoadingFrame();

	/**
	 * Dummy method just to allow instance construction.
	 */
	public final void init() {}

	private JProgressBar bar;

	private LoadingFrame() {
		super();
		this.setTitle("Loading Biblio-Tech...");
		this.setUndecorated(true);
		this.setOpacity(0.7f);
		this.setMinimumSize(new Dimension(300, 260));
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setBackground(new Color(40, 40, 40));
		this.setLayout(null);

		final JLabel label = new JLabel();
		label.setText("");
		label.setIcon(new ImageIcon(this.getClass().getResource(
				"/assets/biblio-tech/launcher/textures/gui/loader/logo_large.png"
		)));
		label.setBounds(0, 20, 300, 160);
		this.add(label);

		this.bar = new JProgressBar();
		this.bar.setStringPainted(true);
		this.bar.setString("");
		this.bar.setBounds(10, 230, 280, 20);
		this.add(this.bar);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		this.reset();
	}

	@SuppressWarnings("WeakerAccess")
	public final void reset() {
		Launcher.logger().trace("Reset loading bar progress");
		this.updateProgressBar(0, "");
		this.bar.setValue(0);
		this.setVisible(true);
	}

	@SuppressWarnings("WeakerAccess")
	public final void updateProgressBarMessage(final String message) {
		if (message.equals(this.bar.getString())) return;
		this.bar.setStringPainted(false);
		this.bar.setString(message);
		this.bar.setStringPainted(true);
		Launcher.logger().trace("New progress bar message set: " + message);
	}

	@SuppressWarnings("WeakerAccess")
	public final void updateProgressBar(final int newVal, final String message) {
		final int val = Math.max(0, Math.min(100, Math.max(this.bar.getValue(), newVal)));
		this.updateProgressBarMessage(message);
		this.bar.setValue(val);

		Launcher.logger().trace("New progress bar progress set: " + newVal);

		if (val >= 100) this.setVisible(false); // We think the task is completed.
	}

	@SuppressWarnings("WeakerAccess")
	public final void updateProgressBarGently(final int newVal, final String message) {
		Launcher.logger().trace("Updating progress bar gently to " + newVal);
		final int val = Math.max(0, Math.min(100, Math.max(this.bar.getValue(), newVal)));
		int start = this.bar.getValue();
		for (; start <= val; ++start) {
			final int newV = start;

			try {
				EventQueue.invokeAndWait(() -> {
					try {
						Thread.sleep(50);
					} catch (final InterruptedException e) {
						throw new RuntimeException("Unable to update progress bar gently");
					}

					this.updateProgressBar(newV, message);
				});
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
