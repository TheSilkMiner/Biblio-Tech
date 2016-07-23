package net.thesilkminer.bibliotech.launcher.os;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Holds a list of all possible operative systems.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public enum Os {
	// So much Closure...
	// Better, closure like code
	WINDOWS {
		@Override
		@Nonnull
		public File workingDir() {
			return new File(System.getenv("AppData") != null? System.getenv("AppData") : this.userHome(), ".bibliotech");
		}
	},
	MAC_OS {
		@Nonnull
		@Override
		public File workingDir() {
			return new File(new File(new File(this.home(), "Library"), "Application Support"), "bibliotech");
		}
	},
	LINUX {
		@Nonnull
		@Override
		public File workingDir() {
			return new File(this.home(), ".bibliotech");
		}
	},
	UNIX {
		@Nonnull
		@Override
		public File workingDir() {
			return new File(this.home(), ".bibliotech");
		}
	},
	UNKNOWN {
		@Nonnull
		@Override
		public File workingDir() {
			return new File(this.home(), "bibliotech");
		}
	};

	@Nonnull
	public abstract File workingDir();

	@Nonnull
	final String userHome() {
		return System.getProperty("user.home");
	}

	@Nonnull
	final File home() {
		return new File(this.userHome());
	}

	@Nonnull
	public final String details() {
		return String.format("%s (%s architecture) version %s", System.getProperty("os.name"),
				System.getProperty("os.arch"), System.getProperty("os.version"));
	}

	@Override
	@Nonnull
	public final String toString() {
		final String[] names = this.name().split("_");
		final StringBuilder toString = new StringBuilder();
		java.util.Arrays.stream(names).forEach(name -> {
			toString.append(name.substring(0, 1));
			toString.append(name.substring(1).toLowerCase());
			toString.append(' ');
		});
		return toString.toString();
	}

	@Nonnull
	public static Os getCurrentOs() {
		final String os = System.getProperty("os.name").toUpperCase(java.util.Locale.ENGLISH);
		if (os.contains("WIN")) return WINDOWS;
		if (os.contains("MAC")) return MAC_OS;
		if (os.contains("LINUX")) return LINUX;
		if (os.contains("UNIX")) return UNIX;
		return UNKNOWN;
	}
}
