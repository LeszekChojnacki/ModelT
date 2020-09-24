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
package de.hybris.platform.promotions.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.promotions.PromotionResultService;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;



public class DefaultPromotionResultService extends AbstractPromotionsService implements PromotionResultService //NOSONAR
{

	@Override
	public boolean apply(final PromotionResultModel promotionResult)
	{
		final boolean result = getResult(promotionResult).apply();
		refreshModifiedModelsAfter(Arrays.asList((ItemModel) promotionResult));
		return result;
	}

	@Override
	public long getConsumedCount(final PromotionResultModel promotionResult, final boolean includeCouldFirePromotions)
	{
		return getResult(promotionResult).getConsumedCount(includeCouldFirePromotions);
	}

	@Override
	public boolean getCouldFire(final PromotionResultModel promotionResult)
	{
		return getResult(promotionResult).getCouldFire();
	}

	@Override
	public String getDescription(final PromotionResultModel promotionResult)
	{
		return getResult(promotionResult).getDescription();
	}

	@Override
	public String getDescription(final PromotionResultModel promotionResult, final Locale locale)
	{
		return getResult(promotionResult).getDescription(locale);
	}

	@Override
	public boolean getFired(final PromotionResultModel promotionResult)
	{
		return getResult(promotionResult).getFired();
	}

	@Override
	public double getTotalDiscount(final PromotionResultModel promotionResult)
	{
		return getResult(promotionResult).getTotalDiscount();
	}

	@Override
	public boolean isApplied(final PromotionResultModel promotionResult)
	{
		return getResult(promotionResult).isApplied();
	}

	@Override
	public boolean isAppliedToOrder(final PromotionResultModel promotionResult)
	{
		return getResult(promotionResult).isAppliedToOrder(getSessionContext());
	}

	@Override
	public boolean undo(final PromotionResultModel promotionResult)
	{
		final boolean result = getResult(promotionResult).undo();
		refreshModifiedModelsAfter(Arrays.asList((ItemModel) promotionResult));
		return result;

	}

	@Override
	public List<PromotionResultModel> getPotentialProductPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		return getModelService().getAll(promoResult.getPotentialProductPromotions(), new ArrayList<PromotionResultModel>());
	}

	@Override
	public List<PromotionResultModel> getPotentialOrderPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		return getModelService().getAll(promoResult.getPotentialOrderPromotions(), new ArrayList<PromotionResultModel>());
	}

	@Override
	public List<PromotionResultModel> getFiredProductPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		return getModelService().getAll(promoResult.getFiredProductPromotions(), new ArrayList<PromotionResultModel>());
	}

	@Override
	public List<PromotionResultModel> getFiredOrderPromotions(final PromotionOrderResults promoResult,
			final AbstractPromotionModel promotion)
	{
		return getModelService().getAll(promoResult.getFiredOrderPromotions(), new ArrayList<PromotionResultModel>());
	}

	@Override
	public Optional<Set<String>> getCouponCodesFromPromotion(final PromotionResultModel promotionResult)
	{
		return Optional.empty();
	}

}
