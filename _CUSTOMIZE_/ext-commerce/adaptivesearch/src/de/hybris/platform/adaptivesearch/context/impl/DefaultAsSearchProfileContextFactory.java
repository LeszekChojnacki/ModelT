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

import de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContextFactory;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link AsSearchProfileContextFactory}.
 */
public class DefaultAsSearchProfileContextFactory implements AsSearchProfileContextFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultAsSearchProfileContextFactory.class);

	@Override
	public AsSearchProfileContext createContext(final String indexConfiguration, final String indexType,
			final List<CatalogVersionModel> catalogVersions, final List<CategoryModel> categoryPath)
	{
		final DefaultAsSearchProfileContext context = new DefaultAsSearchProfileContext();
		context.setIndexConfiguration(indexConfiguration);
		context.setIndexType(indexType);
		context.setCatalogVersions(catalogVersions);
		context.setSessionCatalogVersions(catalogVersions);
		context.setCategoryPath(categoryPath);

		updateQualifiers(context);

		return context;
	}

	@Override
	public AsSearchProfileContext createContext(final String indexConfiguration, final String indexType,
			final List<CatalogVersionModel> catalogVersions, final List<CategoryModel> categoryPath, final LanguageModel language,
			final CurrencyModel currency)
	{
		final DefaultAsSearchProfileContext context = new DefaultAsSearchProfileContext();
		context.setIndexConfiguration(indexConfiguration);
		context.setIndexType(indexType);
		context.setCatalogVersions(catalogVersions);
		context.setSessionCatalogVersions(catalogVersions);
		context.setCategoryPath(categoryPath);
		context.setLanguage(language);
		context.setCurrency(currency);

		updateQualifiers(context);

		return context;
	}

	@Override
	public AsSearchProfileContext createContext(final String indexConfiguration, final String indexType,
			final List<CatalogVersionModel> catalogVersions, final List<CatalogVersionModel> sessionCatalogVersions,
			final List<CategoryModel> categoryPath, final LanguageModel language, final CurrencyModel currency)
	{
		final DefaultAsSearchProfileContext context = new DefaultAsSearchProfileContext();
		context.setIndexConfiguration(indexConfiguration);
		context.setIndexType(indexType);
		context.setCatalogVersions(catalogVersions);
		context.setSessionCatalogVersions(sessionCatalogVersions);
		context.setCategoryPath(categoryPath);
		context.setLanguage(language);
		context.setCurrency(currency);

		updateQualifiers(context);

		return context;
	}

	protected void updateQualifiers(final DefaultAsSearchProfileContext context)
	{
		final List<String> categoryQualifiers = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(context.getCategoryPath()))
		{
			for (final CategoryModel category : context.getCategoryPath())
			{
				try
				{
					categoryQualifiers.add(category.getCode());
				}
				catch (final Exception e)
				{
					// we ignore the category if we cannot get the qualifier (e.g. the category was removed)
					LOG.error("Could not read category code", e);
				}
			}
		}

		context.getQualifiers().put(AdaptivesearchConstants.CATEGORY_QUALIFIER_TYPE, categoryQualifiers);
	}
}
