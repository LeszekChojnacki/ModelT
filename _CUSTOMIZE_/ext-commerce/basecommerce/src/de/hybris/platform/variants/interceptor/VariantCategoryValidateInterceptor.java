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
package de.hybris.platform.variants.interceptor;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.variants.model.VariantCategoryModel;
import de.hybris.platform.variants.model.VariantValueCategoryModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * This interceptor validates that the {@link de.hybris.platform.variants.jalo.VariantCategory} that you try to save has
 * at most one supercategory and this supercategory is of type {@link de.hybris.platform.variants.jalo.VariantCategory}.
 * Also it validates that all subcategories are either of type {@link de.hybris.platform.variants.jalo.VariantCategory}
 * or {@link de.hybris.platform.variants.jalo.VariantValueCategory}. If the subcategories include
 * {@link de.hybris.platform.variants.jalo.VariantCategory} it may not be in there only once.
 */
public class VariantCategoryValidateInterceptor implements ValidateInterceptor<VariantCategoryModel>
{
	private static final String CATALOG_SYNC_ACTIVE_ATTRIBUTE = "catalog.sync.active";

	private L10NService l10NService;
	private SessionService sessionService;

	@Override
	public void onValidate(final VariantCategoryModel variantCategory, final InterceptorContext ctx) throws InterceptorException
	{
		// validator doesn't work during the sync!
		final Boolean isSyncActive = sessionService.getCurrentSession().getAttribute(CATALOG_SYNC_ACTIVE_ATTRIBUTE);
		if (BooleanUtils.isNotTrue(isSyncActive))
		{
			final List<CategoryModel> subcategories = variantCategory.getCategories();

			validateIntegrityBetweenVariantCategories(variantCategory);
			validateVariantValueCategories(variantCategory, subcategories);
		}
	}

	private void validateVariantValueCategories(final VariantCategoryModel variantCategoryToValidate,
			final List<CategoryModel> subcategories) throws InterceptorException
	{
		if (CollectionUtils.isNotEmpty(subcategories))
		{
			// Collect all not VariantValueCategory type categories as they are allowed to be part of sub categories in any count.
			final List<CategoryModel> categoriesToCheck = new ArrayList<>();
			CollectionUtils.select(subcategories,
					PredicateUtils.notPredicate(PredicateUtils.instanceofPredicate(VariantValueCategoryModel.class)),
					categoriesToCheck);

			if (CollectionUtils.isNotEmpty(categoriesToCheck))
			{
				final List<VariantCategoryModel> variantCategories = new ArrayList<>();
				final List<CategoryModel> otherCategories = new ArrayList<>();

				CollectionUtils.select(categoriesToCheck, PredicateUtils.instanceofPredicate(VariantCategoryModel.class),
						variantCategories);
				CollectionUtils.select(categoriesToCheck,
						PredicateUtils.notPredicate(PredicateUtils.instanceofPredicate(VariantCategoryModel.class)), otherCategories);

				if (CollectionUtils.isNotEmpty(variantCategories) && variantCategories.size() > 1)
				{
					throw new InterceptorException(getL10NService()
							.getLocalizedString("error.variantcategory.onlyonevariantcategoryassubcategoryallowed", new Object[]
					{ variantCategoryToValidate.getCode() }));
				}

				if (CollectionUtils.isNotEmpty(otherCategories))
				{
					throw new InterceptorException(getL10NService()
							.getLocalizedString("error.variantcategory.onlyvariantcategoryassubcategoryallowed", new Object[]
					{ variantCategoryToValidate.getCode() }));
				}
			}
		}
	}

	private void validateIntegrityBetweenVariantCategories(final VariantCategoryModel variantCategoryToValidate)
			throws InterceptorException
	{
		// Get the variant category that has higher prio. (should be at most one)
		final Collection<CategoryModel> supercategories = variantCategoryToValidate.getSupercategories();

		if (CollectionUtils.isNotEmpty(supercategories))
		{
			if (supercategories.size() > 1) // only one category is allowed to be set as super category (must be type VariantCategory)
			{
				throw new InterceptorException(
						getL10NService().getLocalizedString("error.variantcategory.onlyonesupercategoryallowed", new Object[]
				{ variantCategoryToValidate.getCode() }));
			}
			else
			{
				// Only one super category was set. Check if it's of type VariantCategory
				CollectionUtils.filter(supercategories, PredicateUtils.instanceofPredicate(VariantCategoryModel.class));

				// If we don't have any categories anymore, it wasn't a VariantCategory
				if (CollectionUtils.isEmpty(supercategories))
				{
					throw new InterceptorException(getL10NService()
							.getLocalizedString("error.variantcategory.onlyvariantcategoryassupercategoryallowed", new Object[]
					{ variantCategoryToValidate.getCode() }));
				}
			}
		}
	}

	protected L10NService getL10NService()
	{
		return l10NService;
	}

	@Required
	public void setL10NService(final L10NService l10NService)
	{
		this.l10NService = l10NService;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}
}
