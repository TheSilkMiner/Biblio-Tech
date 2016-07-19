package net.thesilkminer.bibliotech.launcher.ui.console;

import com.google.common.base.Throwables;

import net.thesilkminer.bibliotech.launcher.crash.ReportedException;
import net.thesilkminer.bibliotech.launcher.logging.Level;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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

	private final JTextPane logBox;

	private ConsoleFrame() {
		super();
		this.setTitle("Console - Book-Tech");
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setLocation(57, 20);
		this.setMinimumSize(new Dimension(800, 400));
		this.setSize(new Dimension(800, 400));

		this.logBox = new JTextPane() {
			@Override
			public boolean getScrollableTracksViewportHeight() {
				return false;
			}

			@Override
			public boolean getScrollableTracksViewportWidth() {
				// We hate line-wrapping: we really do.
				// If you want to see the whole message, just scroll.
				// Usually line wrapping means that a log line gets split
				// into various lines and I really don't like that behaviour.
				// I may make this configurable, though.
				// TODO Ask my UX experts
				return false;
			}
		};
		this.logBox.setEditable(false);
		this.logBox.setMargin(null);

		final JScrollPane scrollPane = new JScrollPane(this.logBox,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.add(scrollPane);
		this.pack();
		this.setVisible(true);
	}

	@Deprecated
	@SuppressWarnings("unused")
	public final void appendLine(final String line) {
		this.appendLine(Level.INFO, line);
	}

	public final void appendLine(final Level level, final String line) {
		final Document doc = this.logBox.getDocument();
		final SimpleAttributeSet color = new SimpleAttributeSet();
		switch (level) {
			case INFO:
				break;
			case ERROR:
				StyleConstants.setForeground(color, Color.RED);
				break;
			case WARNING:
				StyleConstants.setForeground(color, Color.YELLOW);
				break;
			case FINE:
			case FINER:
			case FINEST:
			default:
				StyleConstants.setForeground(color, Color.LIGHT_GRAY);
				break;
			case DEBUG:
				StyleConstants.setForeground(color, Color.PINK);
				break;
			case TRACE:
				StyleConstants.setForeground(color, Color.MAGENTA);
		}
		try {
			doc.insertString(doc.getLength(), line + (line.endsWith("\n")? "" : "\n"), color);
		} catch (final BadLocationException exception) {
			Throwables.propagate(exception);
		}
		this.logBox.setCaretPosition(doc.getLength());
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
