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
package de.hybris.platform.solrserver.strategies.impl;

import static de.hybris.platform.solrserver.constants.SolrserverConstants.EXTENSIONNAME;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_CONFIG_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_DATA_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_LOG_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.SOLR_SERVER_PATH_PROPERTY;

import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.PlatformConfig;
import de.hybris.bootstrap.config.SystemConfig;
import de.hybris.platform.solrserver.constants.SolrserverConstants;
import de.hybris.platform.solrserver.strategies.SolrServerConfigurationProvider;
import de.hybris.platform.solrserver.util.VersionUtils;
import de.hybris.platform.util.Utilities;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation of {@link SolrServerConfigurationProvider}.
 */
public class DefaultSolrServerConfigurationProvider implements SolrServerConfigurationProvider
{
	@Override
	public Map<String, String> getConfiguration()
	{
		final Map<String, String> configuration = new HashMap<String, String>();

		configuration.putAll(Utilities.getConfig().getAllParameters());

		final PlatformConfig platformConfig = Utilities.getPlatformConfig();
		final SystemConfig systemConfig = ConfigUtil.getSystemConfig(platformConfig.getPlatformHome().getAbsolutePath());

		configuration.put(HYBRIS_CONFIG_PATH_PROPERTY, systemConfig.getConfigDir().getAbsolutePath());
		configuration.put(HYBRIS_DATA_PATH_PROPERTY, systemConfig.getDataDir().getAbsolutePath());
		configuration.put(HYBRIS_LOG_PATH_PROPERTY, systemConfig.getLogDir().getAbsolutePath());

		final String extDir = Utilities.getExtensionInfo(EXTENSIONNAME).getExtensionDirectory().getAbsolutePath();
		final String solrServerVersion = configuration.get(SolrserverConstants.SOLR_SERVER_VERSION_PROPERTY);
		final String versionPath = VersionUtils.getVersionPath(solrServerVersion);
		final Path solrServerPath = Paths.get(extDir, "resources", "solr", versionPath, "server");

		configuration.put(SOLR_SERVER_PATH_PROPERTY, solrServerPath.toString());

		return configuration;
	}
}
