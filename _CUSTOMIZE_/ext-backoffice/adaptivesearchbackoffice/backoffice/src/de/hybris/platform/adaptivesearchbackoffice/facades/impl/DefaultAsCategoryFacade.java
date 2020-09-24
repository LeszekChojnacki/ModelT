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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.GLOBAL_CATEGORY_LABEL;

import de.hybris.platform.adaptivesearchbackoffice.data.AsCategoryData;
import de.hybris.platform.adaptivesearchbackoffice.facades.AsCategoryFacade;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.i18n.L10NService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsCategoryFacade}.
 */
public class DefaultAsCategoryFacade implements AsCategoryFacade
{
	private L10NService l10nService;
	private CatalogVersionService catalogVersionService;
	private CategoryService categoryService;

	@Override
	public AsCategoryData getCategoryHierarchy()
	{
		return getCategoryHierarchy(null, null);
	}

	@Override
	public AsCategoryData getCategoryHierarchy(final String catalogId, final String catalogVersionName)
	{
		final AsCategoryData globalCategoryData = createGlobalCategory();

		if (StringUtils.isNotBlank(catalogId) && StringUtils.isNotBlank(catalogVersionName))
		{
			final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(catalogId, catalogVersionName);
			final List<CategoryModel> rootCategories = catalogVersion.getRootCategories();
			if (CollectionUtils.isNotEmpty(rootCategories))
			{
				for (final CategoryModel rootCategory : rootCategories)
				{
					populateCategoryHierarchy(globalCategoryData, rootCategory);
				}
			}
		}

		return globalCategoryData;
	}

	protected void populateCategoryHierarchy(final AsCategoryData parentCategoryData, final CategoryModel category)
	{
		final AsCategoryData categoryData = createCategory(category);

		parentCategoryData.getChildren().add(categoryData);

		final List<CategoryModel> subcategories = category.getCategories();
		if (CollectionUtils.isNotEmpty(subcategories))
		{
			for (final CategoryModel subcategory : subcategories)
			{
				populateCategoryHierarchy(categoryData, subcategory);
			}
		}
	}

	@Override
	public List<AsCategoryData> buildCategoryBreadcrumbs(final List<String> categoryPath)
	{
		return buildCategoryBreadcrumbs(null, null, categoryPath);
	}

	@Override
	public List<AsCategoryData> buildCategoryBreadcrumbs(final String catalogId, final String catalogVersionName,
			final List<String> categoryPath)
	{
		final List<AsCategoryData> categoryBreadcrumbs = new ArrayList<>();
		categoryBreadcrumbs.add(createGlobalCategory());

		if (catalogId != null && catalogVersionName != null)
		{
			final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(catalogId, catalogVersionName);
			for (final String category : categoryPath)
			{
				final CategoryModel categoryModel = categoryService.getCategoryForCode(catalogVersion, category);
				categoryBreadcrumbs.add(createCategory(categoryModel));
			}
		}

		return categoryBreadcrumbs;
	}

	protected AsCategoryData createCategory(final CategoryModel categoryModel)
	{
		final AsCategoryData category = new AsCategoryData();
		category.setCode(categoryModel.getCode());

		if (StringUtils.isNotBlank(categoryModel.getName()))
		{
			category.setName(categoryModel.getName());
		}
		else
		{
			category.setName("[" + categoryModel.getCode() + "]");
		}

		category.setChildren(new ArrayList<>());

		return category;
	}

	protected AsCategoryData createGlobalCategory()
	{
		final AsCategoryData globalCategoryData = new AsCategoryData();
		globalCategoryData.setCode(null);
		globalCategoryData.setName(l10nService.getLocalizedString(GLOBAL_CATEGORY_LABEL));
		globalCategoryData.setChildren(new ArrayList<>());

		return globalCategoryData;
	}

	public L10NService getL10nService()
	{
		return l10nService;
	}

	@Required
	public void setL10nService(final L10NService l10nService)
	{
		this.l10nService = l10nService;
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
}
