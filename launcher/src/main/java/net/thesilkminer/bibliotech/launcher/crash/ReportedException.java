package net.thesilkminer.bibliotech.launcher.crash;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

/**
 * Customizable exception which can be used to supply more
 * detailed messages and descriptions to a
 * {@link CrashReport crash report}.
 *
 * @author TheSilkMiner
 *
 * @since 0.1
 */
public class ReportedException extends RuntimeException {

	private String description;
	private Set<Pair<String, ICrashInfoProvider>> customProviders = Sets.newLinkedHashSet();

	public ReportedException() {
		super();
	}

	public ReportedException(final String message) {
		super(message);
	}

	public ReportedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ReportedException(final Throwable cause) {
		super(cause);
	}

	protected ReportedException(final String message,
	                            final Throwable cause,
	                            final boolean enableSuppression,
	                            final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public String description() {
		return this.description;
	}

	public void description(final String description) {
		this.description = description;
	}

	public Set<Pair<String, ICrashInfoProvider>> customProviders() {
		return ImmutableSet.copyOf(this.customProviders);
	}

	public void addCustomProvider(final String name, final ICrashInfoProvider customProvider) {
		this.customProviders.add(Pair.of(name, customProvider));
	}
}
