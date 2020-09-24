//[y]added-start
/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.drools.core.common;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * Provides centralized access to properties defined in META-INF/drools-local.properties. Purpose of this file is to provide
 * means of customizations to how some of the OOTB properties are defined and read,
 * e.g. as a replacement for System level properties.
 */
public class LocalProperties
{
	private static final Logger LOG = LoggerFactory.getLogger(LocalProperties.class);

	private LocalProperties()
	{
		// empty
	}

	public static Properties load()
	{
		final Properties props = new Properties();
		final URL url = Resources.getResource("META-INF/drools-local.properties");
		final ByteSource byteSource = Resources.asByteSource(url);

		try (InputStream inputStream = byteSource.openBufferedStream())
		{
			props.load(inputStream);
		}
		catch (final IOException e)
		{
			LOG.error("openBufferedStream failed!", e);
		}

		return props;
	}
}
//[y]added-end
