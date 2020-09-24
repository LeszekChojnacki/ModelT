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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.solrsearch.setup.BackofficeSolrSearchSystemSetupConfig;


public class DefaultBackofficeSolrSearchSystemSetupConfig implements BackofficeSolrSearchSystemSetupConfig
{
	private static final String BACKOFFICE_SOLR_SEARCH_NON_LOCALIZED_ROOTS_KEY = "backoffice.solr.search.nonlocalized.files";
	private static final String BACKOFFICE_SOLR_SEARCH_LOCALIZED_ROOTS_KEY = "backoffice.solr.search.localized.roots";
	private static final String BACKOFFICE_SOLR_SEARCH_ROOTS_SEPARATOR_KEY = "backoffice.solr.search.roots.separator";
	private static final String BACKOFFICE_SOLR_SEARCH_ROOTS_FILE_ENCODING = "backoffice.solr.search.roots.file.encoding";
	private static final String BACKOFFICE_SOLR_SEARCH_ROOTS_LANGUAGE_SEPARATOR = "backoffice.solr.search.roots.language.separator";
	private static final String LIST_SEPARATOR = ",";
	private static final String FILE_ENCODING = "UTF-8";
	private static final String LANGUAGE_SEPARATOR = "_";

	private final ConfigStringResolver configStringResolver;

	public DefaultBackofficeSolrSearchSystemSetupConfig(final ConfigStringResolver configStringResolver)
	{
		this.configStringResolver = configStringResolver;
	}

	@Override
	public Collection<String> getLocalizedRootNames()
	{
		return readRoots(BACKOFFICE_SOLR_SEARCH_LOCALIZED_ROOTS_KEY);
	}

	@Override
	public Collection<String> getNonLocalizedRootNames()
	{
		return readRoots(BACKOFFICE_SOLR_SEARCH_NON_LOCALIZED_ROOTS_KEY);
	}

	@Override
	public String getFileEncoding()
	{
		final String fileEncoding = getConfigStringResolver()
				.resolveConfigStringParameter(BACKOFFICE_SOLR_SEARCH_ROOTS_FILE_ENCODING);
		if (StringUtils.isEmpty(fileEncoding))
		{
			return FILE_ENCODING;
		}
		return fileEncoding;
	}

	@Override
	public String getRootNameLanguageSeparator()
	{
		final String languageSeparator = getConfigStringResolver()
				.resolveConfigStringParameter(BACKOFFICE_SOLR_SEARCH_ROOTS_LANGUAGE_SEPARATOR);
		if (StringUtils.isEmpty(languageSeparator))
		{
			return LANGUAGE_SEPARATOR;
		}
		return languageSeparator;
	}

	@Override
	public String getListSeparator()
	{
		final String listSeparator = getConfigStringResolver()
				.resolveConfigStringParameter(BACKOFFICE_SOLR_SEARCH_ROOTS_SEPARATOR_KEY);
		if (StringUtils.isEmpty(listSeparator))
		{
			return LIST_SEPARATOR;
		}
		return listSeparator;
	}

	protected ConfigStringResolver getConfigStringResolver()
	{
		return configStringResolver;
	}

	protected Collection<String> readRoots(final String backofficeSolrSearchImpexRootsKey)
	{
		final String configurationRoots = getConfigStringResolver().resolveConfigStringParameter(backofficeSolrSearchImpexRootsKey);
		if (StringUtils.isEmpty(configurationRoots))
		{
			return Collections.emptyList();
		}
		final String[] roots = configurationRoots.split(getListSeparator());
		return Arrays.asList(roots);
	}
}
