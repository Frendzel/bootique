package com.nhl.bootique.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.function.Function;

import com.google.inject.Inject;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.log.BootLogger;

/**
 * A {@link ConfigurationSource} that locates configuration in a file specified
 * via command-line '--config' option.
 */
public class CliConfigurationSource implements ConfigurationSource {

	public static final String CONFIG_OPTION = "config";

	private String location;

	@Inject
	public CliConfigurationSource(Cli cli, BootLogger bootLogger) {

		Collection<String> configs = cli.optionStrings(CONFIG_OPTION);
		if (configs.isEmpty()) {
			// we are likely in boot sequence, so be quiet about no config. This
			// is likely ok.
		} else if (configs.size() == 1) {
			this.location = configs.iterator().next();
			bootLogger.stdout("Using configuration at " + location);
		} else {
			this.location = configs.iterator().next();
			bootLogger.stdout("Ignoring multiple configurations. Using configuration at " + location);
		}
	}

	@Override
	public <T> T readConfig(Function<InputStream, T> processor) {
		if (location == null) {
			return null;
		}

		try {
			return doReadConfig(processor);
		} catch (IOException e) {
			throw new RuntimeException("Error reading config: " + location, e);
		}
	}

	private <T> T doReadConfig(Function<InputStream, T> processor) throws FileNotFoundException, IOException {

		// location can be either a file path or a URL

		URI uri = URI.create(location);

		URL url = uri.isAbsolute() ? uri.toURL() : new File(location).toURI().toURL();

		try (InputStream in = url.openStream()) {
			return processor.apply(in);
		}
	}
}
