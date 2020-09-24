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
package com.hybris.backoffice.solrsearch.setup.impl;

import de.hybris.platform.servicelayer.impex.impl.FileBasedImpExResource;

import java.io.File;


public class FileBasedImpExResourceFactory
{
	protected FileBasedImpExResource createFileBasedImpExResource(final File file, final String encoding)
	{
		return new FileBasedImpExResource(file, encoding);
	}
}
