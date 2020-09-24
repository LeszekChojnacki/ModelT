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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionActionService;
import de.hybris.platform.promotionengineservices.util.PromotionResultUtils;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.action.impl.DefaultRuleActionService;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Promotionengine specific implementation of the RuleActionService.
 */
public class DefaultPromotionRuleActionService extends DefaultRuleActionService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPromotionRuleActionService.class);

	private PromotionActionService promotionActionService;
	private PromotionResultUtils promotionResultUtils;

	@Override
	public List<ItemModel> applyAllActions(final RuleEngineResultRAO ruleEngineResultRAO)
	{
		final List<ItemModel> actionResults = super.applyAllActions(ruleEngineResultRAO);

		recalculateTotals(actionResults);

		return actionResults;
	}

	/**
	 * Gets the order where actions are applied and recalculate it's totals.
	 *
	 * @param actionResults
	 *           List of applied actions results
	 */
	protected void recalculateTotals(final List<ItemModel> actionResults)
	{
		if (CollectionUtils.isNotEmpty(actionResults))
		{
			final ItemModel item = actionResults.get(0);
			if (!(item instanceof PromotionResultModel))
			{
				LOG.error("Can not recalculate totals. Action result is not PromotionResultModel {}", item);
				return;
			}
			final PromotionResultModel promotionResult = (PromotionResultModel) item;
			final AbstractOrderModel order = getPromotionResultUtils().getOrder(promotionResult);
			if (order == null)
			{
				LOG.error("Can not recalculate totals. No order found for PromotionResult: {}", promotionResult);
				return;
			}
			getPromotionActionService().recalculateTotals(order);

		}
	}

	protected PromotionActionService getPromotionActionService()
	{
		return promotionActionService;
	}

	@Required
	public void setPromotionActionService(final PromotionActionService promotionActionService)
	{
		this.promotionActionService = promotionActionService;
	}

	protected PromotionResultUtils getPromotionResultUtils()
	{
		return promotionResultUtils;
	}

	@Required
	public void setPromotionResultUtils(final PromotionResultUtils promotionResultUtils)
	{
		this.promotionResultUtils = promotionResultUtils;
	}
}
