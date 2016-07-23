package net.thesilkminer.bibliotech.launcher;

import net.thesilkminer.bibliotech.launcher.crash.CrashReportHandler;
import net.thesilkminer.bibliotech.launcher.logging.Logger;
import net.thesilkminer.bibliotech.launcher.ui.LoadingFrame;
import net.thesilkminer.bibliotech.launcher.ui.console.ConsoleFrame;

import org.jetbrains.annotations.Contract;

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.swing.UIManager;

/**
 * Main entry point of BookTech.
 *
 * <p>This class is the main launcher class, which loads up
 * and runs the main software.</p>
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public class Launcher {

	private static Logger log;

	private Launcher() {
		log = Logger.obtain("Launcher");
		ConsoleFrame.INSTANCE.init();
		log.info("Successfully loaded and constructed console");
		LoadingFrame.INSTANCE.init();
		log.info("Successfully loaded and constructed frame");
		LoadingFrame.INSTANCE.updateProgressBarGently(100, "ASD"); //TODO Debug line
		new net.thesilkminer.bibliotech.launcher.ui.LauncherFrame(); //TODO Debug line
	}

	@Contract(pure = true)
	public static Logger logger() {
		return log;
	}

	public static void main(@Nonnull final String... args) {
		try {
			for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equalsIgnoreCase(info.getName())) UIManager.setLookAndFeel(info.getClassName());
			}

			UIManager.put("control", new Color(40, 40, 40));
			UIManager.put("text", new Color(40, 40, 40).brighter().brighter().brighter().brighter().brighter());
			UIManager.put("nimbusBase", new Color(0, 0, 0));
			UIManager.put("nimbusFocus", new Color(40, 40, 40));
			UIManager.put("nimbusBorder", new Color(40, 40, 40));
			UIManager.put("nimbusLightBackground", new Color(40, 40, 40));
			UIManager.put("info", new Color(40, 40, 40).brighter().brighter());
			UIManager.put("nimbusSelectionBackground", new Color(40, 40, 40).brighter().brighter());
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}

		final Thread launcherThread = new Thread(Launcher::new);
		Thread.setDefaultUncaughtExceptionHandler(CrashReportHandler.INSTANCE::constructReport);
		launcherThread.start();
	}
}
