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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.daos.CategoryDao;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;


public class CategoryRuleParameterValueMapper implements RuleParameterValueMapper<CategoryModel>
{
	private CategoryDao categoryDao;

	private CatalogVersionService catalogVersionService;

	private RuleParameterValueMapper<CatalogModel> catalogRuleParameterValueMapper;

	private String delimiter;

	private String catalogVersionName;

	@Override
	public String toString(final CategoryModel category)
	{
		ServicesUtil.validateParameterNotNull(category, "Object cannot be null");
		return String.join(getDelimiter(), category.getCode(),
				getCatalogRuleParameterValueMapper().toString(category.getCatalogVersion().getCatalog()));
	}

	@Override
	public CategoryModel fromString(final String value)
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");

		final Collection<CategoryModel> categories;
		String categoryCode = value;

		if (value.contains(getDelimiter()))
		{
			final String catalogIdentifier = substringAfter(value, getDelimiter());
			final CatalogModel catalog = getCatalogRuleParameterValueMapper().fromString(catalogIdentifier);
			final CatalogVersionModel catalogVersion =
					getCatalogVersionService().getCatalogVersion(catalog.getId(), getCatalogVersionName());

			categoryCode = substringBefore(value, getDelimiter());

			categories = getCategoryDao().findCategoriesByCode(catalogVersion, categoryCode);
		}
		else
		{
			categories = getCategoryDao().findCategoriesByCode(value);
		}

		if (CollectionUtils.isEmpty(categories))
		{
			throw new RuleParameterValueMapperException("Cannot find category with the code: " + categoryCode);
		}

		return categories.iterator().next();
	}

	protected CategoryDao getCategoryDao()
	{
		return categoryDao;
	}

	@Required
	public void setCategoryDao(final CategoryDao categoryDao)
	{
		this.categoryDao = categoryDao;
	}

	protected RuleParameterValueMapper<CatalogModel> getCatalogRuleParameterValueMapper()
	{
		return catalogRuleParameterValueMapper;
	}

	@Required
	public void setCatalogRuleParameterValueMapper(
			final RuleParameterValueMapper<CatalogModel> catalogRuleParameterValueMapper)
	{
		this.catalogRuleParameterValueMapper = catalogRuleParameterValueMapper;
	}

	protected String getDelimiter()
	{
		return delimiter;
	}

	@Required
	public void setDelimiter(final String delimiter)
	{
		this.delimiter = delimiter;
	}

	protected String getCatalogVersionName()
	{
		return catalogVersionName;
	}

	@Required
	public void setCatalogVersionName(final String catalogVersionName)
	{
		this.catalogVersionName = catalogVersionName;
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}
}
