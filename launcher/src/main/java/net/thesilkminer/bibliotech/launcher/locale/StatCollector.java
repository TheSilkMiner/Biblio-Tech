package net.thesilkminer.bibliotech.launcher.locale;

import com.google.common.collect.Maps;

import net.thesilkminer.bibliotech.launcher.crash.ReportedException;
import net.thesilkminer.bibliotech.launcher.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Handles localization for the various applications.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public enum StatCollector {

	INSTANCE;

	private static final String MANAGER_LOGGING_ID = "Language Manager";

	private final Map<String, String> locale = Maps.newHashMap();
	private final Logger logger = Logger.obtain(MANAGER_LOGGING_ID);
	private Languages current;

	StatCollector() {
		this.setLocale(Languages.ENGLISH_USA);
	}

	@Contract(value = "-> !null", pure = true)
	@Nonnull
	private Logger log() {
		return this.logger;
	}

	@Contract("null -> null; !null -> !null")
	@Nullable
	public String translateToLocal(@Nullable final String id) {
		if (id == null) return null;
		final String translation = this.locale.get(id);
		if (translation != null) return translation;
		this.locale.put(id, id);
		this.log().warning("Missing translation for id " + id);
		return id;
	}

	public void setLocale(@Nonnull final Languages language) {
		// Micro-optimization: don't reload the same language
		if (language == this.current) return;

		this.log().info("Attempting to set language to " + language.toString());
		this.log().fine("    Language code: " + language.languageCode());
		this.log().debug(
				String.format("    File location: /assets/biblio-tech/launcher/lang/%s.lang", language.languageCode())
		);

		final BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(new File(this.getClass().getResource(String.format(
					"/assets/biblio-tech/launcher/lang/%s.lang", language.languageCode()
			)).getFile())));
		} catch (final Throwable e) {
			if (language.equals(Languages.ENGLISH_USA)) {
				this.log().error("Main file not found. Crashing!");
				final ReportedException toThrow = new ReportedException(e.getMessage(), e);
				toThrow.description("Unable to find basic language file (en_US.lang)\n"
						+ "Make sure it exists.\n"
						+ "This is a serious bug: report to TheSilkMiner");
				toThrow.addCustomProvider("Language code not found", (report, builder) -> {
					builder.append("Expected language path: /assets/biblio-tech/launcher/lang/en_US.lang\n");
					builder.append("Result: FILE NOT FOUND\n");
				});
				throw toThrow;
			}
			this.log().warning("Unable to find the specified language file");
			this.log().warning("Falling back to en_US.lang (American English)");
			this.setLocale(Languages.ENGLISH_USA);
			return;
		}

		reader.lines()
				.filter(line -> !line.isEmpty())
				.filter(line -> {
					if (!line.startsWith("#")) return true;
					this.log().fine("Found comment line in file --> skipping over it");
					return false;
				})
				.filter(line -> {
					if (line.contains("=")) return true;
					this.log().warning("Identified line not containing an equal sign");
					this.log().warning("Currently skipping it, but the behaviour may change in future versions");
					return false;
				})
				.forEachOrdered(line -> {
					final int equalLocation = line.indexOf('=');
					if (equalLocation == -1) throw new RuntimeException("equalLocation == -1");
					final String id = line.substring(0, equalLocation);
					final String translation = line.substring(equalLocation).substring(1);
					final Pair<String, String> pair = Pair.of(id.trim(), translation);
					if (pair.getLeft().isEmpty() || pair.getRight().isEmpty()) {
						this.log().warning("Id or translation is empty. Please check the translation");
					}
					this.locale.put(pair.getLeft(), pair.getRight());
					this.log().info(String.format("Registering pair %s -> %s", id.trim(), translation));
					this.log().debug("    " + pair.toString());
				});

		this.log().finer("Checking language code: it should match " + language.languageCode());
		final String langCode = language.languageCode();
		final String reported = this.locale.get("language.code"); // Do not use StatCollector

		if (!langCode.equals(reported)) {
			this.log().error(String.format("Language file for language %s is invalid!", language));
			this.log().debug(String.format("    Expected: %s", langCode));
			this.log().debug(String.format("    Got: %s", reported));

			if (language.equals(Languages.ENGLISH_USA)) {
				this.log().error("Corrupted main file. Crashing...");
				throw new ReportedException("Corrupted main language file");
			}

			this.log().error("Falling back to default (American English)");
			this.setLocale(Languages.ENGLISH_USA);
			return;
		}

		this.log().finest("Matches. Allowing the process to complete");
		this.log().trace("Current language map: " + this.locale.toString());

		this.current = language;
	}
}
