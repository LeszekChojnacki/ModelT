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

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.exists;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.variants.model.GenericVariantProductModel;
import de.hybris.platform.variants.model.VariantCategoryModel;
import de.hybris.platform.variants.model.VariantValueCategoryModel;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * This interceptor ensures the following rules are fulfilled for a given
 * {@link de.hybris.platform.variants.jalo.VariantValueCategory} - there can be only one super category of type
 * VariantCategory and this super category is not optional - it can't have any other Categories (normal ones) as super
 * categories.
 */
public class GenericVariantProductValidateInterceptor implements ValidateInterceptor<GenericVariantProductModel>
{
	private static final String CATALOG_SYNC_ACTIVE_ATTRIBUTE = "catalog.sync.active";

	private L10NService l10NService;
	private SessionService sessionService;

	@Override
	public void onValidate(final GenericVariantProductModel genericVariant, final InterceptorContext ctx)
			throws InterceptorException
	{
		// validator doesn't work during the sync!
		final Boolean isSyncActive = sessionService.getCurrentSession().getAttribute(CATALOG_SYNC_ACTIVE_ATTRIBUTE);
		if (BooleanUtils.isNotTrue(isSyncActive))
		{
			final Collection<CategoryModel> variantValueCategories = genericVariant.getSupercategories();

			if (isEmpty(variantValueCategories))
			{
				throw new InterceptorException(localizeForKey("error.genericvariantproduct.wrongsupercategory"));
			}
			else
			{
				validateBaseProductSuperCategories(genericVariant, variantValueCategories);
				validateSupercategories(variantValueCategories);
			}
		}
	}

	protected void validateBaseProductSuperCategories(final GenericVariantProductModel genericVariant,
			final Collection<CategoryModel> variantValueCategories) throws InterceptorException
	{
		final ProductModel baseProduct = genericVariant.getBaseProduct();
		if (baseProduct == null)
		{
			throw new InterceptorException(
					getL10NService().getLocalizedString("error.genericvariantproduct.nobaseproduct", new Object[]
					{ genericVariant.getCode() }));
		}
		final Collection<CategoryModel> superCategories = baseProduct.getSupercategories();
		if (isNotEmpty(superCategories))
		{
			List<CategoryModel> variantCategoriesOfVariantValueCategories = Lists.newArrayList();
			if (isNotEmpty(variantValueCategories))
			{
				variantCategoriesOfVariantValueCategories = variantValueCategories.stream()
						.filter(v -> v instanceof VariantValueCategoryModel)
						.flatMap(v -> ((VariantValueCategoryModel) v).getSupercategories().stream())
						.filter(c -> c instanceof VariantCategoryModel).collect(toList());
			}
			final Collection<CategoryModel> baseVariantCategories = superCategories.stream()
					.filter(c -> c instanceof VariantCategoryModel).collect(toList());
			if (baseVariantCategories.size() != variantCategoriesOfVariantValueCategories.size())
			{
				throw new InterceptorException(
						getL10NService().getLocalizedString("error.genericvariantproduct.nosameamountofvariantcategories", new Object[]
						{ genericVariant.getCode(), Integer.valueOf(variantCategoriesOfVariantValueCategories.size()), baseProduct.getCode(), Integer.valueOf(baseVariantCategories.size()), }));
			}
			for (final CategoryModel varCategory : variantCategoriesOfVariantValueCategories)
			{
				if (!baseVariantCategories.contains(varCategory))
				{
					throw new InterceptorException(getL10NService()
							.getLocalizedString("error.genericvariantproduct.variantcategorynotinbaseproduct", new Object[]
					{ varCategory.getCode(), genericVariant.getCode(), baseProduct.getCode() }));
				}
			}
		}
	}

	protected void validateSupercategories(final Collection<CategoryModel> superCategories) throws InterceptorException
	{
		// verify that the super categories of the variantProduct are VariantValueCategories
		// and their super categories are only the VariantCategories
		final boolean wrongCategoryExists = exists(superCategories, (final Object object) ->
		{
			if (object instanceof VariantValueCategoryModel)
			{
				final VariantValueCategoryModel variantValueCategoryModel = (VariantValueCategoryModel) object;
				if (isEmpty(variantValueCategoryModel.getSupercategories()))
				{
					return true;
				}
				else
				{
					return !(variantValueCategoryModel.getSupercategories().iterator().next() instanceof VariantCategoryModel);
				}
			}
			else
			{
				return true;
			}
		});

		if (wrongCategoryExists)
		{
			throw new InterceptorException(localizeForKey("error.genericvariantproduct.wrongsupercategory"));
		}
	}

	private String localizeForKey(final String key)
	{
		return getL10NService().getLocalizedString(key);
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
