package net.thesilkminer.bibliotech.launcher.ui;

import net.thesilkminer.bibliotech.launcher.Launcher;
import net.thesilkminer.bibliotech.launcher.auth.AuthData;
import net.thesilkminer.bibliotech.launcher.auth.AuthDatabase;
import net.thesilkminer.bibliotech.launcher.crash.ReportedException;
import net.thesilkminer.bibliotech.launcher.locale.Languages;
import net.thesilkminer.bibliotech.launcher.locale.StatCollector;
import net.thesilkminer.bibliotech.launcher.logging.Level;

import net.thesilkminer.bibliotech.launcher.logging.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Holds the frame where the launcher will be shown.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public class LauncherFrame extends JFrame {

	@Nls private static final String BRUTE$MESSAGE = "launcher.login.bruteForce.message";
	@Nls private static final String BRUTE$TITLE = "launcher.login.bruteForce.title";
	@Nls private static final String CLEAR = "launcher.login.button.clear";
	@Nls private static final String INVALID$MESSAGE = "launcher.login.wrongDetails.message";
	@Nls private static final String INVALID$TITLE = "launcher.login.wrongDetails.title";
	@Nls private static final String LANGUAGE = "launcher.settings.language";
	@Nls private static final String LEVEL = "launcher.settings.logLevel";
	@Nls private static final String LOGGING = "launcher.login.button.logging";
	@Nls private static final String LOGIN = "launcher.login.button.login";
	@Nls private static final String PASSWORD = "launcher.login.password";
	@Nls private static final String UPDATES$FOUND = "launcher.settings.updates.available";
	@Nls private static final String UPDATES$SEARCH = "launcher.settings.updates.search";
	@Nls private static final String UPDATES$UPDATE = "launcher.settings.updates.upToDate";
	@Nls private static final String USERNAME = "launcher.login.username";
	@Nls private static final String VERSION = "launcher.settings.version";

	private final JButton clear;
	private final JButton log;
	private final JPasswordField passField;
	private final JTextField userNameField;

	private int loginTimes;

	public LauncherFrame() {
		super();
		this.setTitle("Biblio-Tech Launcher");
		this.setMinimumSize(new Dimension(400, 225));
		this.setSize(new Dimension(400, 225));
		this.setMaximumSize(new Dimension(400, 225));
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(1, 1));

		final JTabbedPane tabs = new JTabbedPane(SwingConstants.LEFT);
		final GridBagConstraints c = new GridBagConstraints();

		final JPanel options = new JPanel();
		options.setLayout(new GridLayout(4, 2));

		final JLabel languageLabel = new JLabel(StatCollector.INSTANCE.translateToLocal(LANGUAGE));
		languageLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		final JLabel loggingLevelLabel = new JLabel(StatCollector.INSTANCE.translateToLocal(LEVEL));
		loggingLevelLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		final JCheckBox updates = new JCheckBox(StatCollector.INSTANCE.translateToLocal(UPDATES$SEARCH), null, true);
		updates.setHorizontalAlignment(SwingConstants.RIGHT);

		final JLabel versionLabel = new JLabel(StatCollector.INSTANCE.translateToLocal(VERSION));
		versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		final JComboBox<Languages> languageOptions = new JComboBox<>(Languages.values());
		languageOptions.setEnabled(true);
		languageOptions.setEditable(false);
		languageOptions.setSelectedItem(Languages.ENGLISH_USA); //TODO
		languageOptions.addActionListener(e -> {
			if (!(e.getSource() instanceof JComboBox<?>)) return;
			final JComboBox<?> source = (JComboBox<?>) e.getSource();
			StatCollector.INSTANCE.setLocale(Languages.values()[source.getSelectedIndex()]);
		});

		final JComboBox<Level> loggingLevelOptions = new JComboBox<>(Level.values());
		loggingLevelOptions.setEnabled(true);
		loggingLevelOptions.setEditable(false);
		loggingLevelOptions.setSelectedItem(Level.defaultLevel()); //TODO
		loggingLevelOptions.addActionListener(e -> {
			if (!(e.getSource() instanceof JComboBox<?>)) return;
			final JComboBox<?> source = (JComboBox<?>) e.getSource();
			Logger.minimum(Level.values()[source.getSelectedIndex()]);
		});
		loggingLevelOptions.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
			final JLabel label = new JLabel(value.toString());

			if (isSelected)	label.setBackground(list.getSelectionBackground());
			else label.setBackground(list.getBackground());

			label.setForeground(value.color());
			return label;
		});

		final JLabel updatesAvailable = new JLabel(StatCollector.INSTANCE.translateToLocal(UPDATES$FOUND)); // TODO
		updatesAvailable.setHorizontalAlignment(SwingConstants.CENTER);
		if (updatesAvailable.getText().equals(UPDATES$FOUND)) updatesAvailable.setForeground(new Color(191, 98, 4));

		final JComboBox<String> versionOptions = new JComboBox<>(new String[] {"dev"});
		versionOptions.setEnabled(true);
		versionOptions.setEditable(false);
		versionOptions.setSelectedItem("dev"); //TODO
		loggingLevelOptions.addActionListener(e -> {
			if (!(e.getSource() instanceof JComboBox<?>)) return;
			final JComboBox<?> source = (JComboBox<?>) e.getSource();
			// TODO
		});

		options.add(languageLabel);
		options.add(languageOptions);
		options.add(loggingLevelLabel);
		options.add(loggingLevelOptions);
		options.add(updates);
		options.add(updatesAvailable);
		options.add(versionLabel);
		options.add(versionOptions);

		final JPanel login = new JPanel();
		login.setLayout(new GridBagLayout());

		final JLabel userNameLabel = new JLabel(StatCollector.INSTANCE.translateToLocal(USERNAME));
		userNameLabel.setHorizontalAlignment(SwingConstants.CENTER);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1;

		login.add(userNameLabel, c);

		this.userNameField = new JTextField();
		this.userNameField.setColumns(4);
		this.userNameField.setEditable(true);
		this.userNameField.addKeyListener(new KeyAdapter() {
			@Contract("null -> fail; !null -> _")
			@Override
			public void keyTyped(@Nonnull final KeyEvent e) {
				if (LauncherFrame.this.userNameField.getText().length() >= 20) {
					e.consume();
					LauncherFrame.this.getToolkit().beep();
					Launcher.logger().trace("Stopped user writing attempt");
					Launcher.logger().trace("    - Reason: Exceeded character limit");
					Launcher.logger().trace("    - At: LauncherFrame.userNameField");
				}
			}
		});
		this.userNameField.addCaretListener(e -> {
			if (!LauncherFrame.this.userNameField.getText().isEmpty()) LauncherFrame.this.clear.setEnabled(true);
		});

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 3;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		login.add(this.userNameField, c);

		final JLabel passLabel = new JLabel(StatCollector.INSTANCE.translateToLocal(PASSWORD));
		passLabel.setHorizontalAlignment(SwingConstants.CENTER);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
		c.weighty = 1;

		login.add(passLabel, c);

		this.passField = new JPasswordField();
		this.passField.setColumns(4);
		this.passField.setEditable(true);
		this.passField.addKeyListener(new KeyAdapter() {
			@Contract("null -> fail; !null -> _")
			@Override
			public void keyTyped(@Nonnull final KeyEvent e) {
				if (LauncherFrame.this.passField.getPassword().length >= 20) {
					e.consume();
					LauncherFrame.this.getToolkit().beep();
					Launcher.logger().trace("Stopped user writing attempt");
					Launcher.logger().trace("    - Reason: Exceeded character limit");
					Launcher.logger().trace("    - At: LauncherFrame.passField");
				}
			}
		});
		this.passField.addCaretListener(e -> {
			if (LauncherFrame.this.passField.getPassword().length >= 0) LauncherFrame.this.clear.setEnabled(true);
		});

		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 3;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		login.add(this.passField, c);

		this.clear = new JButton(StatCollector.INSTANCE.translateToLocal(CLEAR));
		this.clear.setEnabled(false);
		this.clear.addActionListener(e -> {
			LauncherFrame.this.userNameField.setText("");
			LauncherFrame.this.passField.setText("");
			Launcher.logger().info("Cleared login fields");
			this.clear.setEnabled(false);
		});

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 2;
		c.weighty = 5;
		c.fill = GridBagConstraints.BOTH;

		login.add(clear, c);

		this.log = new JButton(StatCollector.INSTANCE.translateToLocal(LOGIN));
		this.log.setEnabled(true);
		this.setAutoRequestFocus(true);
		this.log.addActionListener(e -> {
			final AuthData data = new AuthData.Builder().user(this.userNameField.getText())
					.pass(this.passField.getPassword()).build();
			this.passField.setText("");
			this.log.setEnabled(false);
			this.log.setText(StatCollector.INSTANCE.translateToLocal(LOGGING));
			Launcher.logger().trace("Attempting login with specified data " + data);
			if (!AuthDatabase.DATABASE.populated()) AuthDatabase.DATABASE.populate();
			final boolean isValid = AuthDatabase.DATABASE.isValidData(data);
			if (!isValid) {
				this.log.setEnabled(true);
				this.log.setText(StatCollector.INSTANCE.translateToLocal(LOGIN));
				Launcher.logger().warning("Wrong login details");
				JOptionPane.showMessageDialog(this,
						StatCollector.INSTANCE.translateToLocal(INVALID$MESSAGE),
						StatCollector.INSTANCE.translateToLocal(INVALID$TITLE),
						JOptionPane.ERROR_MESSAGE,
						null);
				this.loginTimes++;

				if (this.loginTimes >= 5) {
					Launcher.logger().error("Wrong login details 5 times in a row");
					Launcher.logger().error("Assuming brute force attack");
					Launcher.logger().error("Shutting down...");
					JOptionPane.showMessageDialog(this,
							StatCollector.INSTANCE.translateToLocal(BRUTE$MESSAGE),
							StatCollector.INSTANCE.translateToLocal(BRUTE$TITLE),
							JOptionPane.ERROR_MESSAGE,
							null);
					final ReportedException exception = new ReportedException();
					exception.description("Brute force attack detected");
					// Now let's have some fun...
					final byte[] bytes = new byte[1024]; // 1 kb of information, wow!
					//final byte[] bytes = new byte[1024 * 1024] // Let's avoid being evil with 1 MB of characters
					//final byte[] bytes = new byte[1024 * 1024 * 1024] // Or even a GB
					new Random(new Random(System.currentTimeMillis()).nextLong()).nextBytes(bytes); // Randomness...
					bytes[0] = 'i'; // Let's avoid crashing right as soon as a "toString()" is invoked
					bytes[1] = 'd'; // Just complete the word
					for (int i = 0; i < bytes.length; ++i) {
						if (bytes[i] == '\n') bytes[i] = -100; // Whatever -100 is in characters...
					}
					exception.addCustomProvider(new String(bytes), (report, builder) -> {
						throw new RuntimeException();
					});
					throw exception;
				}
				return;
			}

			// TODO Launch software
		});

		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 2;
		c.weighty = 5;
		c.fill = GridBagConstraints.BOTH;

		login.add(log, c);

		tabs.addTab("", new ImageIcon(this.getClass().getResource(
				"/assets/biblio-tech/launcher/textures/gui/launcher/options.png"
		)), options);
		tabs.addTab("", new ImageIcon(this.getClass().getResource(
				"/assets/biblio-tech/launcher/textures/gui/launcher/login.png"
		)), login);
		tabs.setSelectedIndex(1);

		this.add(tabs);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
