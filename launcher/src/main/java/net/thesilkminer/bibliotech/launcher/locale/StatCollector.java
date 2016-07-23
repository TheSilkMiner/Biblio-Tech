package net.thesilkminer.bibliotech.launcher.locale;

import org.jetbrains.annotations.Contract;

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

	@Contract("null -> null; !null -> !null")
	@Nullable
	public String translateToLocal(@Nullable final String id) {
		if (id == null) return null;
		return id;
		// TODO
	}

	public void setLocale(final Languages languages) {
		// TODO
	}
}
