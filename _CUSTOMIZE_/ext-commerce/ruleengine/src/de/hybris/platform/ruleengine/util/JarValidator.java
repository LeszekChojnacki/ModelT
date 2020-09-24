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
package de.hybris.platform.ruleengine.util;

import de.hybris.platform.util.zip.SafeZipEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;


/**
 * The class provides some methods to check validity of jar files.
 *
 */
public class JarValidator
{
	private JarValidator()
	{

	}

	/**
	 * Checks if the jar file is not malformed and thus is not zip slip vulnerable
	 * (doesn't have any entry name with traversal directory like "../file.dat")
	 *
	 * @param jarFileInputStream
	 *           jar file {@link InputStream} to check
	 */
	public static void validateZipSlipSecure(final InputStream jarFileInputStream) throws IOException
	{
		try (final JarInputStream jarInputStream = new JarInputStream(jarFileInputStream))
		{
			ZipEntry entry;
			while ((entry = jarInputStream.getNextEntry()) != null)
			{
				new SafeZipEntry(entry).getName();
			}
		}
	}
}
