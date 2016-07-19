package net.thesilkminer.bibliotech.launcher.ui.console;

import net.thesilkminer.bibliotech.launcher.crash.ReportedException;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

/**
 * Holds the frame where the console will be shown.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public class ConsoleFrame extends JFrame {

	public static final ConsoleFrame INSTANCE = new ConsoleFrame();

	/**
	 * Dummy method just to allow instance construction.
	 */
	public final void init() {}

	private final JTextArea logBox;

	private ConsoleFrame() {
		super();
		this.setTitle("Console - Book-Tech");
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setLocation(57, 20);
		this.setMinimumSize(new Dimension(800, 400));
		this.setSize(new Dimension(800, 400));

		this.logBox = new JTextArea();
		this.logBox.setRows(20);
		this.logBox.setColumns(80);
		this.logBox.setEditable(false);

		final JScrollPane scrollPane = new JScrollPane(this.logBox,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.add(scrollPane);
		this.pack();
		this.setVisible(true);
	}

	public final void appendLine(final String line) {
		this.logBox.append(line + (line.endsWith("\n")? "" : "\n"));
		this.logBox.setCaretPosition(this.logBox.getDocument().getLength());
	}

	public final void purge() {
		try {
			java.awt.EventQueue.invokeAndWait(() -> this.logBox.setText(""));
		} catch (final Exception e) {
			final ReportedException report = new ReportedException(e.getMessage(), e);
			report.description("Exception while purging console");
			report.addCustomProvider("Console status", (crash, builder) -> {
				builder.append("Console text length: ").append(this.logBox.getDocument().getLength()).append("\n");
				builder.append("Last console line: ");
				try {
					final String text = this.logBox.getText().substring(0, this.logBox.getText().length() - 1);
					builder.append(
							text.substring(text.lastIndexOf('\n') + 1)).append("\n");
				} catch (final Exception ex) {
					builder.append("~~ERROR~~ ").append(ex.getMessage()).append("\n");
				}
				throw new RuntimeException();
			});
			throw report;
		}
	}
}
