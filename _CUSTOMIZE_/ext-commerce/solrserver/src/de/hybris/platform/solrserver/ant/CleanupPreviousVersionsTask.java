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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * Ant Task that based on current server/customization version identifies and removes previous ones.
 */
public class CleanupPreviousVersionsTask extends Task
{
	private static final Logger LOG = Logger.getLogger(CleanupPreviousVersionsTask.class.getName());

	public enum VersionTypes
	{
		server, customizations
	}

	private String currentVersion;
	private String searchDirectory;
	private String versionType;

	public String getCurrentVersion()
	{
		return currentVersion;
	}

	public void setCurrentVersion(final String currentVersion)
	{
		this.currentVersion = currentVersion;
	}

	public String getSearchDirectory()
	{
		return searchDirectory;
	}

	public void setSearchDirectory(final String searchDirectory)
	{
		this.searchDirectory = searchDirectory;
	}

	public String getVersionType()
	{
		return versionType;
	}

	public void setVersionType(final String versionType)
	{
		this.versionType = versionType;
	}

	@Override
	public void execute()
	{
		validateAttributes();

		final ArrayList<String> currentVersions = getPrunedCurrentVerions(currentVersion);

		for (final File versionDirectory : getExistingVersionDirectories())
		{
			deleteEmptyVersionDirectory(versionDirectory);

			if (isUnusedVersionDirectory(currentVersions, versionDirectory))
			{
				final File[] subDirectories = versionDirectory.listFiles(File::isDirectory);
				for (final File subDir : subDirectories)
				{
					deleteObsoleteDirectory(versionDirectory, subDirectories, subDir);
				}
			}
		}
	}

	protected boolean isUnusedVersionDirectory(final ArrayList<String> currentVersions, final File versionDirectory)
	{
		return !currentVersions.contains(versionDirectory.getName());
	}

	protected void deleteObsoleteDirectory(final File versionDirectory, final File[] subDirectories, final File subDir)
	{
		if (versionType.equals(subDir.getName()))
		{
			if (subDirectories.length == 1)
			{
				// nothing left in version directory, delete it
				LOG.log(Level.INFO, "Deleting obsolete solr directory {0}", versionDirectory.getAbsoluteFile());
				deleteDirectory(versionDirectory);
			}
			else
			{
				// version directory contains other directories, delete only specific sub directory
				LOG.log(Level.INFO, "Deleting obsolete solr directory {0}", subDir.getAbsoluteFile());
				deleteDirectory(subDir);
			}
		}
	}

	protected void deleteDirectory(final File directory)
	{
		try
		{
			FileUtils.deleteDirectory(directory);
		}
		catch (final IOException e)
		{
			final String msg = MessageFormat.format("Failed to delete obsolete solr version directory {0}", directory.getAbsoluteFile());
			LOG.log(Level.WARNING, msg, e);
		}
	}

	protected void deleteEmptyVersionDirectory(final File versionDirectory)
	{
		if (isDirectoryEmpty(versionDirectory))
		{
			deleteDirectory(versionDirectory);
		}
	}

	protected void validateAttributes()
	{
		if (currentVersion == null || currentVersion.isEmpty())
		{
			throw new BuildException("Unknown current version");
		}

		if (searchDirectory == null || searchDirectory.isEmpty())
		{
			throw new BuildException("Unknown search directory");
		}

		if (versionType == null || versionType.isEmpty() || !EnumUtils.isValidEnum(VersionTypes.class, versionType))
		{
			throw new BuildException("Unknown version type, allowed version types: " + Arrays.toString(VersionTypes.values()));
		}
	}

	protected File[] getExistingVersionDirectories()
	{
		final File dir = FileUtils.getFile(searchDirectory);
		if (dir.exists() && dir.isDirectory())
		{
			return dir.listFiles(File::isDirectory);
		}
		else
		{
			throw new BuildException("Search directory [" + dir.getAbsolutePath() + "] not found");
		}
	}

	protected boolean isDirectoryEmpty(final File file)
	{
		boolean result = false;
		if (file.isDirectory())
		{
			final String[] files = file.list();
			if (files.length == 0)
			{
				result = true;
			}
		}
		return result;
	}

	protected ArrayList<String> getPrunedCurrentVerions(final String versionEntry)
	{
		final ArrayList<String> versions = new ArrayList<String>();
		for (final String version : StringUtils.split(versionEntry, ","))
		{
			versions.add(version.replaceFirst("(.*\\..*)\\..*", "$1"));
		}
		return versions;
	}
}
