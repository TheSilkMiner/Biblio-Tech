package net.thesilkminer.bibliotech.launcher.auth;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.Contract;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Holds the various data used for login.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public class AuthData {

	public static class Builder {

		private String user;
		private char[] pass;

		@Contract("-> !null")
		@Nonnull
		public static Builder of() {
			return new Builder();
		}

		@Contract("-> !null")
		@Nonnull
		public final AuthData build() {
			final AuthData data = new AuthData(this.user, this.pass);
			this.user = null;
			this.pass = null;
			return data;
		}

		@Contract("!null -> !null; null -> fail")
		public final Builder user(@Nonnull final String user) {
			this.user = Preconditions.checkNotNull(user);
			return this;
		}

		@Contract("!null -> !null; null -> fail")
		public final Builder pass(@Nonnull final char... pass) {
			this.pass = Preconditions.checkNotNull(pass);
			return this;
		}
	}

	private final String userName;
	private final char[] password;

	private AuthData(@Nonnull final String userName, @Nonnull final char... password) {
		this.userName = Preconditions.checkNotNull(userName);
		this.password = Preconditions.checkNotNull(password);
	}

	@Contract(pure = true)
	@Nonnull
	public final String userName() {
		return this.userName;
	}

	@Contract(pure = true)
	@Nonnull
	public final char[] password() {
		return this.password;
	}

	 public final void secure() {
		 for (int i = 0; i < this.password.length; ++i) this.password[i] = 0x0;
	 }

	@Contract(value = "null -> false; !null -> _", pure = true)
	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) return true;
		if (o == null || this.getClass() != o.getClass()) return false;
		final AuthData authData = (AuthData) o;

		return new EqualsBuilder()
				.append(this.userName(), authData.userName())
				.append(this.password(), authData.password())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(this.userName())
				.append(this.password())
				.toHashCode();
	}

	@Nonnull
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("userName", this.userName())
				.add("password", new Random(new Random(System.currentTimeMillis()).nextLong()).nextLong())
				.toString();
	}
}
