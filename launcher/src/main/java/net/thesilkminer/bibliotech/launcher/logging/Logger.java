package net.thesilkminer.bibliotech.launcher.logging;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.thesilkminer.bibliotech.launcher.crash.ReportedException;
import net.thesilkminer.bibliotech.launcher.os.Os;
import net.thesilkminer.bibliotech.launcher.ui.console.ConsoleFrame;

import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * Logger class used to print out all the various messages to
 * various logging sources.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public final class Logger {

	private static final Map<String, Logger> CACHE = Maps.newLinkedHashMap();
	private static Logger loggerLogger;
	private static PrintWriter out;
	private static Level minimum;
	private static List<Pair<Level, String>> messagesCache;
	private final String source;

	private Logger(final String source) {
		try {
			initLogFile();
			this.source = source;
			if (messagesCache == null) messagesCache = Lists.newLinkedList();
			if (minimum == null) minimum = Level.defaultLevel();
			if (loggerLogger != null) loggerLogger.fine(String.format("Requested new logger for source %s", source));
		} catch (final IOException e) {
			Throwables.propagate(e);
			throw new RuntimeException(e); // Dead code, but who cares?
		}
	}

	static {
		final String mainSource = "Logger";
		loggerLogger = Logger.obtain(mainSource);
		loggerLogger.fine(String.format("Requested new logger for source %s", mainSource));
	}

	@Nonnull
	public static Logger obtain(final String source) {
		if (!CACHE.containsKey(source)) CACHE.put(source, new Logger(source));
		return CACHE.get(source);
	}

	private static void initLogFile() throws IOException {
		if (out != null) return;
		if (!Os.getCurrentOs().workingDir().exists() || !Os.getCurrentOs().workingDir().isDirectory()) {
			if (!Os.getCurrentOs().workingDir().mkdirs()) {
				final ReportedException exception = new ReportedException("Unable to create software directory");
				exception.description("Unable to create software directory. Does this software have read/write "
						+ "permissions for " + Os.getCurrentOs().workingDir().getAbsolutePath());
				throw exception;
			}
		}
		out = new PrintWriter(new BufferedWriter(new FileWriter(obtainLogFile(Os.getCurrentOs().workingDir()))), true);
		out.println("# Biblio-Tech v0.1 - LogFile");
		final Calendar cal = new GregorianCalendar();
		out.println(String.format("# Log opened on date %02d.%02d.%02d",
				cal.get(Calendar.DAY_OF_MONTH),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.YEAR)));
	}

	@Nonnull
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static File obtainLogFile(final File workingDir) {
		final File logDir = new File(workingDir, "logs");
		if (!logDir.exists() && !logDir.mkdirs()) {
			final ReportedException exception = new ReportedException("Unable to create logs directory");
			exception.description("Unable to create log directory. Does this software have read/write "
					+ "permissions for " + logDir.getAbsolutePath());
			throw exception;
		}
		final File latest = new File(logDir, "latest.log");
		if (latest.exists()) {
			try {
				final BufferedReader in = new BufferedReader(new FileReader(latest));
				String date = "";
				for (String line = ""; line != null; line = in.readLine()) {
					if (!line.startsWith("# Log opened on date ")) continue;
					line = line.replace("# Log opened on date ", "");
					final String[] dates = line.replace('.', '-').split("-");
					if (dates.length != 3) continue;
					date = String.format("%s-%s-%s", dates[2], dates[1], dates[0]);
				}
				in.close();
				final File backup = new File(logDir, String.format("log_%s_%d.zip", date,
						System.currentTimeMillis() / 1000));
				final ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(backup));
				final ZipEntry log = new ZipEntry(String.format("log_%s.log", date));
				stream.putNextEntry(log);
				final byte[] data = new byte[2048];
				final BufferedInputStream input = new BufferedInputStream(new FileInputStream(latest), 2048);
				for (int size = -2; size != -1; size = input.read(data, 0, 2048)) {
					if (size == -2) continue;
					stream.write(data, 0, size);
				}
				input.close();
				stream.closeEntry();
				stream.close();
				latest.delete();
			} catch (final IOException e) {
				Throwables.propagate(e);
			}
		}
		try {
			latest.createNewFile();
		} catch (final IOException e) {
			Throwables.propagate(e);
		}
		return latest;
	}

	public static void minimum(final Level minimum) {
		Logger.minimum = minimum;
		ConsoleFrame.INSTANCE.purge();
		for (final Pair<Level, String> pair : messagesCache) {
			if (pair.getLeft().ordinal() < minimum.ordinal()) continue;
			ConsoleFrame.INSTANCE.appendLine(pair.getRight());
		}
	}

	private String format(final Level level, final String message) {
		// Is GregorianCalendar cache-able?
		final Calendar cal = new GregorianCalendar();
		return String.format("%02d/%02d/%02d %02d:%02d:%02d %s [%s] [%s] %s",
				cal.get(Calendar.DAY_OF_MONTH),
				cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.YEAR),
				cal.get(Calendar.HOUR),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND),
				cal.get(Calendar.AM_PM) == Calendar.SUNDAY? "PM" : "AM", //What? Why do I need to check against Sunday?
				this.source,
				level.toString(),
				message);
	}

	public void log(final Level level, final String message) {
		final String msg = this.format(level, message);
		out.println(msg);
		messagesCache.add(Pair.of(level, msg));

		if (level.ordinal() < minimum.ordinal()) return;

		System.out.println(msg);
		ConsoleFrame.INSTANCE.appendLine(msg);
	}

	public void trace(final String message) {
		this.log(Level.TRACE, message);
	}

	public void debug(final String message) {
		this.log(Level.DEBUG, message);
	}

	public void finest(final String message) {
		this.log(Level.FINEST, message);
	}

	public void finer(final String message) {
		this.log(Level.FINER, message);
	}

	public void fine(final String message) {
		this.log(Level.FINE, message);
	}

	public void info(final String message) {
		this.log(Level.INFO, message);
	}

	public void warning(final String message) {
		this.log(Level.WARNING, message);
	}

	public void error(final String message) {
		this.log(Level.ERROR, message);
	}
}
