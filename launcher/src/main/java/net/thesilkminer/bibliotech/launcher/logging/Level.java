package net.thesilkminer.bibliotech.launcher.logging;

import java.awt.Color;

import javax.annotation.Nonnull;

/**
 * Identifies the logging level of a certain message.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public enum Level {
	TRACE {
		@Nonnull
		@Override
		public Color color() {
			return Color.PINK.darker().darker().brighter();
		}
	},
	DEBUG {
		@Nonnull
		@Override
		public Color color() {
			return Color.PINK;
		}
	},
	FINEST {
		@Nonnull
		@Override
		public Color color() {
			return Color.LIGHT_GRAY;
		}
	},
	FINER {
		@Nonnull
		@Override
		public Color color() {
			return Color.LIGHT_GRAY;
		}
	},
	FINE {
		@Nonnull
		@Override
		public Color color() {
			return Color.LIGHT_GRAY;
		}
	},
	INFO {
		@Nonnull
		@Override
		public Color color() {
			return DEFAULT.brighter().brighter();
		}
	},
	WARNING {
		@Nonnull
		@Override
		public Color color() {
			return Color.YELLOW;
		}
	},
	ERROR {
		@Nonnull
		@Override
		public Color color() {
			return Color.RED;
		}
	};

	private static final Color DEFAULT = new Color(40, 40, 40).brighter().brighter().brighter().brighter().brighter();

	@Nonnull
	public abstract Color color();

	@Nonnull
	public static Level defaultLevel() {
		try {
			return Level.valueOf(System.getProperty("net.thesilkminer.bibliotech.shared.logging.level", "INFO"));
		} catch (final IllegalArgumentException e) {
			return INFO;
		}
	}
}
