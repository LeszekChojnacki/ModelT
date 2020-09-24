/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
/*

 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 *
 */
package de.hybris.platform.solrserver.util;


/**
 * Provides helper methods for Solr multi version compatibility.
 */
public final class VersionUtils
{
	private VersionUtils()
	{
		// utility class
	}

	/**
	 * Extracts the path for a specific version.
	 *
	 * @param version
	 *           -the version
	 *
	 * @return the version path
	 */

	public static final String getVersionPath(final String version)
	{
		if (version == null)
		{
			throw new IllegalArgumentException("Version cannot be null");
		}

		final String[] tokens = version.split("\\.");
		if (tokens.length != 3)
		{
			throw new IllegalArgumentException("Invalid version: " + version);
		}

		final String majorVersion = tokens[0];
		final String minorVersion = tokens[1];

		return majorVersion + "." + minorVersion;
	}
}
