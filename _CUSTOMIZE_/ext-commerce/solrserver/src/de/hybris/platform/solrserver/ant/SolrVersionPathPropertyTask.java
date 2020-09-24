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
package de.hybris.platform.solrserver.ant;

import de.hybris.platform.solrserver.util.VersionUtils;

import org.apache.tools.ant.BuildException;


/**
 * Ant Task that extracts the path for a specific version and puts it in a property.
 */
public class SolrVersionPathPropertyTask extends org.apache.tools.ant.Task
{
	private String name;
	private String version;

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(final String version)
	{
		this.version = version;
	}

	@Override
	public void execute()
	{
		if (name == null || name.isEmpty())
		{
			throw new BuildException("Unknown name");
		}

		if (version == null || version.isEmpty())
		{
			throw new BuildException("Unknown version");
		}

		final String versionPath = VersionUtils.getVersionPath(version);
		getProject().setProperty(name, versionPath);
	}
}
