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
package de.hybris.platform.promotionengineservices.promotionengine.impl;

import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionMessageParameterResolutionStrategy;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * DefaultCategoryResolutionStrategy resolves the given {@link RuleParameterData#getValue()} into a category code, looks
 * up the category via and invokes {@link #getCategoryRepresentation(CategoryModel)} to display the category.
 */
public class DefaultCategoryResolutionStrategy implements PromotionMessageParameterResolutionStrategy
{

	private CategoryService categoryService;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultCategoryResolutionStrategy.class);

	@Override
	public String getValue(final RuleParameterData data, final PromotionResultModel promotionResult, final Locale locale)
	{
		final String categoryCode = data.getValue();
		final CategoryModel category = getCategory(categoryCode);
		if (category != null)
		{
			return getCategoryRepresentation(category);
		}
		// if not resolved, just return the input
		return categoryCode;
	}

	/**
	 * retrieves a Category based on the given {@code categoryCode}. This method uses the
	 * {@link CategoryService#getCategoryForCode(String)} method.
	 * @param categoryCode
	 *           the category code
	 * @return the category or null if none (or multiple) are found.
	 */
	protected CategoryModel getCategory(final String categoryCode)
	{
		try
		{
			return getCategoryService().getCategoriesForCode(categoryCode).stream().filter(this::isSupportedCategory).findFirst()
					.orElse(null);
		}
		catch (final IllegalArgumentException e)
		{
			LOG.error("Cannot resolve category code: " + categoryCode + " to a category.", e);
			return null;
		}
	}

	protected boolean isSupportedCategory(final CategoryModel categoryModel)
	{
		return !(categoryModel.getCatalogVersion() instanceof ClassificationSystemVersionModel);
	}

	/**
	 * returns the {@link CategoryModel#getName()} for the given {@code category}.
	 * @param category
	 *           the category
	 * @return the name of the category
	 */
	protected String getCategoryRepresentation(final CategoryModel category)
	{
		return category.getName();
	}

	protected CategoryService getCategoryService()
	{
		return categoryService;
	}

	@Required
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}
}