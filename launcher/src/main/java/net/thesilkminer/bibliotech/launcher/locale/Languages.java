package net.thesilkminer.bibliotech.launcher.locale;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

/**
 * Holds a list of all supported and unsupported languages.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public enum Languages {
	ENGLISH_USA("en_US") {
		@Nonnull
		@Override
		protected String overrideToString() {
			return "English (USA)";
		}
	},
	ENGLISH_GB("en_GB") {
		@Nonnull
		@Override
		protected String overrideToString() {
			return "English (GB)";
		}
	},
	ITALIAN("it_IT");

	public final String languageCode;

	Languages(final String languageCode) {
		this.languageCode = languageCode;
	}

	@Contract(pure = true)
	@Nonnull
	protected String overrideToString() {
		return "";
	}

	@Contract(pure = true)
	@Nonnull
	@Override
	public final String toString() {
		if (!this.overrideToString().isEmpty()) return this.overrideToString();
		final String[] names = this.name().split("_");
		final StringBuilder toString = new StringBuilder();
		java.util.Arrays.stream(names).forEach(name -> {
			toString.append(name.substring(0, 1));
			toString.append(name.substring(1).toLowerCase());
			toString.append(' ');
		});
		return toString.toString();
	}
}
