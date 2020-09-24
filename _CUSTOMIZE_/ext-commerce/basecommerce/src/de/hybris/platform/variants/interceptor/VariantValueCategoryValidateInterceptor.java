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

import de.hybris.platform.variants.model.VariantCategoryModel;
import de.hybris.platform.variants.model.VariantValueCategoryModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;


/**
 * This interceptor ensures the following rules are fulfilled for a given
 * {@link de.hybris.platform.variants.jalo.VariantValueCategory} - there can be only one super category of type
 * VariantCategory and this super category is not optional - it can't have any other Categories (normal ones) as super
 * categories.
 */
public class VariantValueCategoryValidateInterceptor implements ValidateInterceptor<VariantValueCategoryModel>
{
	private static final String CATALOG_SYNC_ACTIVE_ATTRIBUTE = "catalog.sync.active";

	private L10NService l10NService;
	private SessionService sessionService;

	@Override
	public void onValidate(final VariantValueCategoryModel variantValueCategory, final InterceptorContext ctx)
			throws InterceptorException
	{
		// validator doesn't work during the sync!
		final Boolean isSyncActive = sessionService.getCurrentSession().getAttribute(CATALOG_SYNC_ACTIVE_ATTRIBUTE);
		if (BooleanUtils.isNotTrue(isSyncActive))
		{
			final List<CategoryModel> variantCategories = variantValueCategory.getSupercategories();

			if (CollectionUtils.isEmpty(variantCategories))
			{
				throw new InterceptorException(getL10NService().getLocalizedString("error.variantvaluecategory.nosupercategoryfound"));
			}

			if (variantCategories.size() > 1)
			{
				throw new InterceptorException(getL10NService().getLocalizedString("error.variantvaluecategory.maxonesupercategory"));
			}

			if (variantCategories.size() == 1)
			{
				final CategoryModel variantCategory = variantCategories.iterator().next();
				if (variantCategory instanceof VariantCategoryModel)
				{
					validateVariantValueCategory(variantValueCategory);
					final List<CategoryModel> siblings = variantCategory.getCategories();
					validateSequenceWithinSiblings(siblings, variantValueCategory);
				}
				else
				{
					throw new InterceptorException(getL10NService().getLocalizedString("error.variantvaluecategory.wrongcategorytype"));
				}
			}
		}
	}

	protected void validateVariantValueCategory(final VariantValueCategoryModel variantValueCategory) throws InterceptorException
	{
		if (variantValueCategory.getSequence() == null)
		{
			throw new InterceptorException(getL10NService().getLocalizedString(
					"error.variantvaluecategory.nosequencenumberprovided"));
		}

		if (variantValueCategory.getSequence() != null && variantValueCategory.getSequence().intValue() < 0)
		{
			throw new InterceptorException(getL10NService().getLocalizedString(
					"error.variantvaluecategory.negativesequencenumber"));
		}
	}

	protected void validateSequenceWithinSiblings(final List<CategoryModel> siblings,
			final VariantValueCategoryModel currentCategory) throws InterceptorException
	{
		final HashSet<Integer> sequences = Sets.newHashSet();

		if (!siblings.contains(currentCategory))
		{
			sequences.add(currentCategory.getSequence());
		}

		for (CategoryModel c : siblings)
		{
			if (c instanceof VariantValueCategoryModel)
			{
				final VariantValueCategoryModel variantValueCat = (VariantValueCategoryModel) c;

				if (sequences.contains(variantValueCat.getSequence()))
				{
					throw new InterceptorException(getL10NService().getLocalizedString(
							"error.genericvariantproduct.morethenonecategorywithsamesequence", new Object[]
							{ variantValueCat.getSequence() }));
				}
				else
				{
					sequences.add(variantValueCat.getSequence());
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
	public void setSessionService(SessionService sessionService)
	{
		this.sessionService = sessionService;
	}
}
