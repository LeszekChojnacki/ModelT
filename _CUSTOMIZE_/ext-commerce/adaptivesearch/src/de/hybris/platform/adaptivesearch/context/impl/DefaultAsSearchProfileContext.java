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
package de.hybris.platform.adaptivesearch.context.impl;

import de.hybris.platform.adaptivesearch.context.AsKeyword;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link AsSearchProfileContext}.
 */
public class DefaultAsSearchProfileContext implements AsSearchProfileContext
{
	private String indexConfiguration;
	private String indexType;
	private List<CatalogVersionModel> catalogVersions;
	private List<CatalogVersionModel> sessionCatalogVersions;
	private List<CategoryModel> categoryPath;
	private LanguageModel language;
	private CurrencyModel currency;
	private List<AsKeyword> keywords;
	private String query;

	private final Map<String, List<String>> qualifiers;
	private final Map<String, Object> attributes;

	public DefaultAsSearchProfileContext()
	{
		keywords = new ArrayList<>();
		attributes = new HashMap<>();
		qualifiers = new HashMap<>();
	}

	@Override
	public String getIndexConfiguration()
	{
		return indexConfiguration;
	}

	public void setIndexConfiguration(final String indexConfiguration)
	{
		this.indexConfiguration = indexConfiguration;
	}

	@Override
	public String getIndexType()
	{
		return indexType;
	}

	public void setIndexType(final String indexType)
	{
		this.indexType = indexType;
	}

	@Override
	public List<CatalogVersionModel> getCatalogVersions()
	{
		return catalogVersions;
	}

	public void setCatalogVersions(final List<CatalogVersionModel> catalogVersions)
	{
		this.catalogVersions = catalogVersions;
	}

	@Override
	public List<CatalogVersionModel> getSessionCatalogVersions()
	{
		return sessionCatalogVersions;
	}

	public void setSessionCatalogVersions(final List<CatalogVersionModel> sessionCatalogVersions)
	{
		this.sessionCatalogVersions = sessionCatalogVersions;
	}

	@Override
	public List<CategoryModel> getCategoryPath()
	{
		return categoryPath;
	}

	public void setCategoryPath(final List<CategoryModel> categoryPath)
	{
		this.categoryPath = categoryPath;
	}

	@Override
	public LanguageModel getLanguage()
	{
		return language;
	}

	public void setLanguage(final LanguageModel language)
	{
		this.language = language;
	}

	@Override
	public CurrencyModel getCurrency()
	{
		return currency;
	}

	public void setCurrency(final CurrencyModel currency)
	{
		this.currency = currency;
	}

	@Override
	public List<AsKeyword> getKeywords()
	{
		return keywords;
	}

	@Override
	public void setKeywords(final List<AsKeyword> keywords)
	{
		this.keywords = keywords;
	}

	@Override
	public String getQuery()
	{
		return query;
	}

	@Override
	public void setQuery(final String query)
	{
		this.query = query;
	}

	@Override
	public Map<String, List<String>> getQualifiers()
	{
		return qualifiers;
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		return attributes;
	}
}
