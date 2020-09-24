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
package de.hybris.platform.adaptivesearchbackoffice.facades.impl;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContextFactory;
import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import de.hybris.platform.adaptivesearchbackoffice.data.CategoryData;
import de.hybris.platform.adaptivesearchbackoffice.data.NavigationContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsSearchProfileContextFacade;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for {@link AsSearchProfileContextFacade}
 */
public class DefaultAsSearchProfileContextFacade implements AsSearchProfileContextFacade
{
	private AsSearchProfileContextFactory searchProfileContextFactory;

	private CatalogVersionService catalogVersionService;
	private CategoryService categoryService;
	private CommonI18NService commonI18NService;

	@Override
	public AsSearchProfileContext createSearchProfileContext(final NavigationContextData navigationContext)
	{
		final String indexConfiguration = navigationContext.getIndexConfiguration();
		final String indexType = navigationContext.getIndexType();

		final List<CatalogVersionModel> catalogVersions = resolveCatalogVersions(navigationContext);
		final List<CategoryModel> categoryPath = resolveCategoryPath(navigationContext, catalogVersions);

		return searchProfileContextFactory.createContext(indexConfiguration, indexType, catalogVersions, categoryPath);
	}

	@Override
	public AsSearchProfileContext createSearchProfileContext(final NavigationContextData navigationContext,
			final SearchContextData searchContext)
	{
		final String indexConfiguration = navigationContext.getIndexConfiguration();
		final String indexType = navigationContext.getIndexType();

		final List<CatalogVersionModel> catalogVersions = resolveCatalogVersions(navigationContext);
		final List<CategoryModel> categoryPath = resolveCategoryPath(navigationContext, catalogVersions);
		final LanguageModel language = resolveLanguage(searchContext);
		final CurrencyModel currency = resolveCurrency(searchContext);

		return searchProfileContextFactory.createContext(indexConfiguration, indexType, catalogVersions, categoryPath, language,
				currency);
	}

	protected List<CatalogVersionModel> resolveCatalogVersions(final NavigationContextData navigationContext)
	{
		final CatalogVersionData catalogVersion = navigationContext.getCatalogVersion();
		if (catalogVersion == null)
		{
			return Collections.emptyList();
		}

		final CatalogVersionModel catalogVersionModel = catalogVersionService.getCatalogVersion(catalogVersion.getCatalogId(),
				catalogVersion.getVersion());

		return Collections.singletonList(catalogVersionModel);
	}

	protected List<CategoryModel> resolveCategoryPath(final NavigationContextData navigationContext,
			final List<CatalogVersionModel> catalogVersions)
	{
		final CategoryData category = navigationContext.getCategory();
		if (category == null || CollectionUtils.isEmpty(category.getPath()) || CollectionUtils.isEmpty(catalogVersions))
		{
			return Collections.emptyList();
		}

		final CatalogVersionModel catalogVersion = catalogVersions.get(0);

		return category.getPath().stream().map(code -> categoryService.getCategoryForCode(catalogVersion, code))
				.collect(Collectors.toList());
	}

	protected LanguageModel resolveLanguage(final SearchContextData searchContext)
	{
		if (searchContext == null || searchContext.getLanguage() == null)
		{
			return null;
		}

		return commonI18NService.getLanguage(searchContext.getLanguage());
	}

	protected CurrencyModel resolveCurrency(final SearchContextData searchContext)
	{
		if (searchContext == null || searchContext.getCurrency() == null)
		{
			return null;
		}

		return commonI18NService.getCurrency(searchContext.getCurrency());
	}

	public AsSearchProfileContextFactory getSearchProfileContextFactory()
	{
		return searchProfileContextFactory;
	}

	@Required
	public void setSearchProfileContextFactory(final AsSearchProfileContextFactory searchProfileContextFactory)
	{
		this.searchProfileContextFactory = searchProfileContextFactory;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public CategoryService getCategoryService()
	{
		return categoryService;
	}

	@Required
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
