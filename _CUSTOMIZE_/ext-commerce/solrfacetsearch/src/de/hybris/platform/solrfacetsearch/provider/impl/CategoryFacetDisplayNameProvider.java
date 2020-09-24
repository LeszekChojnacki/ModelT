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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.solrfacetsearch.provider.FacetDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;
import java.util.Locale;


public class CategoryFacetDisplayNameProvider implements FacetDisplayNameProvider
{

	private CategoryService categoryService;

	/**
	 * Gets the locale based on two or one part ISO code.
	 *
	 * @param isoCode
	 *           the iso code
	 *
	 * @return the locale
	 */
	protected Locale getLocale(final String isoCode)
	{
		final String[] splittedCode = isoCode.split("_");
		Locale result;
		if (splittedCode.length == 1)
		{
			result = new Locale(splittedCode[0]);
		}
		else
		{
			result = new Locale(splittedCode[0], splittedCode[1]);
		}
		return result;
	}

	@Override
	public String getDisplayName(final SearchQuery query, final String name)
	{
		final Locale locale = getLocale(query.getLanguage());
		CategoryModel category = null;

		if (query.getCatalogVersions() != null)
		{
			category = getCategoryForCatalogVersions(query.getCatalogVersions(), name);
		}

		//search in all active catalog versions in the session
		if (category == null)
		{
			category = getCategory(name);
		}

		return category != null ? category.getName(locale) : null;
	}

	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}

	protected CategoryModel getCategoryForCatalogVersions(final List<CatalogVersionModel> catalogVersions, final String code)
	{
		for (final CatalogVersionModel catalogVersion : catalogVersions)
		{
			try
			{
				//search for the category in the specific catalog version first
				if (catalogVersion != null)
				{
					return categoryService.getCategoryForCode(catalogVersion, code);
				}
			}
			catch (final UnknownIdentifierException uie)
			{
				//do nothing, because we can still search in active session catalog versions
				continue;
			}
		}

		return null;
	}

	protected CategoryModel getCategory(final String code)
	{
		CategoryModel category = null;
		try
		{
			category = categoryService.getCategoryForCode(code);
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.error(e.getMessage());
		}
		return category;
	}
}
