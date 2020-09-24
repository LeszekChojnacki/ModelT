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
package de.hybris.platform.adaptivesearchsolr.strategies.impl;

import static de.hybris.platform.adaptivesearchsolr.constants.AdaptivesearchsolrConstants.ALL_CATEGORIES_FIELD;

import de.hybris.platform.adaptivesearchsolr.strategies.SolrAsCategoryPathResolver;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class DefaultSolrAsCategoryPathResolver implements SolrAsCategoryPathResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSolrAsCategoryPathResolver.class);

	private CategoryService categoryService;

	@Override
	public List<CategoryModel> resolveCategoryPath(final SearchQuery searchQuery, final List<CatalogVersionModel> catalogVersions)
	{
		if (CollectionUtils.isEmpty(catalogVersions))
		{
			return Collections.emptyList();
		}

		final String categoryCode = resolveCategoryCode(searchQuery);

		if (StringUtils.isBlank(categoryCode))
		{
			return Collections.emptyList();
		}

		final CategoryModel category = resolveCategory(categoryCode, catalogVersions);

		if (category == null)
		{
			return Collections.emptyList();
		}

		final List<CategoryModel> categoryPath = buildCategoryPath(category);

		if (LOG.isDebugEnabled())
		{
			LOG.debug(categoryPath.stream().map(CategoryModel::getName).collect(Collectors.joining(",")));
		}

		return categoryPath;
	}

	protected String resolveCategoryCode(final SearchQuery searchQuery)
	{
		final String categoryCode = resolveCategoryCodeFromFilterQueries(searchQuery);

		if (StringUtils.isNotBlank(categoryCode))
		{
			return categoryCode;
		}

		return resolveCategoryCodeFromFacetValues(searchQuery);
	}

	protected String resolveCategoryCodeFromFilterQueries(final SearchQuery searchQuery)
	{
		final List<QueryField> filterQueries = searchQuery.getFilterQueries();
		if (CollectionUtils.isNotEmpty(filterQueries))
		{
			final Optional<QueryField> fieldOptional = filterQueries.stream()
					.filter(queryField -> ALL_CATEGORIES_FIELD.equals(queryField.getField())).findAny();

			if (fieldOptional.isPresent())
			{
				final Set<String> values = fieldOptional.get().getValues();

				return CollectionUtils.isEmpty(values) || values.size() > 1 ? null : values.iterator().next();
			}
		}

		return null;
	}

	protected String resolveCategoryCodeFromFacetValues(final SearchQuery searchQuery)
	{
		final List<FacetValueField> facetValues = searchQuery.getFacetValues();
		if (CollectionUtils.isNotEmpty(facetValues))
		{
			final Optional<FacetValueField> fieldOptional = facetValues.stream()
					.filter(facetValueField -> ALL_CATEGORIES_FIELD.equals(facetValueField.getField())).findAny();

			if (fieldOptional.isPresent())
			{
				final Set<String> values = fieldOptional.get().getValues();

				return CollectionUtils.isEmpty(values) || values.size() > 1 ? null : values.iterator().next();
			}
		}

		return null;
	}

	protected CategoryModel resolveCategory(final String categoryCode, final List<CatalogVersionModel> catalogVersions)
	{
		// YTODO: categoryService should contain a method to filter categories by code and catalog versions
		final Collection<CategoryModel> categories = categoryService.getCategoriesForCode(categoryCode);
		if (CollectionUtils.isEmpty(categories))
		{
			return null;
		}

		final Collection<CategoryModel> matchingCategories = categories.stream()
				.filter(category -> isMatchingCategory(category, catalogVersions)).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(matchingCategories))
		{
			return null;
		}

		// finds the first category that belongs to an active catalog version
		final Optional<CategoryModel> activeCategory = matchingCategories.stream().filter(this::isActiveCategory).findFirst();
		if (activeCategory.isPresent())
		{
			return activeCategory.get();
		}

		// finds the first category that belongs to a non active catalog version
		final Optional<CategoryModel> nonActiveCategory = matchingCategories.stream().filter(this::isNonActiveCategory).findFirst();
		if (nonActiveCategory.isPresent())
		{
			return nonActiveCategory.get();
		}

		return null;
	}

	protected boolean isMatchingCategory(final CategoryModel category, final List<CatalogVersionModel> catalogVersions)
	{
		return catalogVersions.contains(category.getCatalogVersion());
	}

	protected boolean isActiveCategory(final CategoryModel category)
	{
		return BooleanUtils.isTrue(category.getCatalogVersion().getActive());
	}

	protected boolean isNonActiveCategory(final CategoryModel category)
	{
		return !isActiveCategory(category);
	}

	protected List<CategoryModel> buildCategoryPath(final CategoryModel category)
	{
		final Set<CategoryModel> categoryPathHelper = new LinkedHashSet<>();
		buildCategoryPathHelper(category, categoryPathHelper);

		final List<CategoryModel> categoryPath = new ArrayList<>(categoryPathHelper);
		Collections.reverse(categoryPath);

		return categoryPath;
	}

	protected void buildCategoryPathHelper(final CategoryModel category, final Set<CategoryModel> categoryPath)
	{
		final List<CategoryModel> supercategories = category.getSupercategories();

		categoryPath.add(category);

		if (!CollectionUtils.isEmpty(supercategories))
		{
			for (final CategoryModel supercategory : supercategories)
			{
				if (isSupportedCategory(supercategory))
				{
					buildCategoryPathHelper(supercategory, categoryPath);
				}
			}
		}
	}

	protected boolean isSupportedCategory(final CategoryModel categoryModel)
	{
		return !(categoryModel instanceof ClassificationClassModel);
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
