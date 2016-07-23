package net.thesilkminer.bibliotech.launcher.auth;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Contract;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Holds the current database of authorized {@link AuthData} combinations.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public enum AuthDatabase {

	DATABASE;

	private final List<AuthData> validData = Lists.newArrayList();

	public final void populate() {
		// TODO
	}

	public final boolean populated() {
		return !this.validData.isEmpty();
	}

	@Contract(value = "null -> false; !null -> _", pure = true)
	public final boolean isValidData(@Nullable final AuthData data) {
		return data != null && this.validData.stream().anyMatch(data::equals);
	}
}
