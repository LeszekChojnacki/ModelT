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
package de.hybris.platform.promotionengineservices.action.impl;

import static java.math.BigDecimal.valueOf;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderEntryAdjustActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulates logic of Cart total calculation after Line Item Discount as a Promotion Rule is applied.
 */
public class DefaultOrderEntryAdjustActionStrategy extends AbstractRuleActionStrategy<RuleBasedOrderEntryAdjustActionModel>
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultOrderEntryAdjustActionStrategy.class);

	/**
	 * If the parameter action is of type {@link DiscountRAO} and is applied to Order Entry the discount is added to
	 * corresponding Order Entry model.
	 *
	 * @return list of {@link PromotionResultModel} as a result of the {@link DiscountRAO} application.
	 */
	@Override
	public List<PromotionResultModel> apply(final AbstractRuleActionRAO action)
	{
		if (!(action instanceof DiscountRAO))
		{
			LOG.error("cannot apply {}, action is not of type DiscountRAO", this.getClass().getSimpleName());
			return Collections.emptyList();
		}

		// the action.getAppliedToObject() must contain an OrderEntryRAO
		final AbstractOrderEntryModel entry = getPromotionActionService().getOrderEntry(action);
		if (entry == null)
		{
			LOG.error("cannot apply {}, orderEntry could not be found.", this.getClass().getSimpleName());
			return Collections.emptyList();
		}

		final PromotionResultModel promoResult = getPromotionActionService().createPromotionResult(action);
		if (promoResult == null)
		{
			LOG.error("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
			return Collections.emptyList();
		}

		final AbstractOrderModel order = entry.getOrder();
		if (order == null)
		{
			LOG.error("cannot apply {}, order does not exist for order entry", this.getClass().getSimpleName());
			// detach the promotion result if its not saved yet.
			if (getModelService().isNew(promoResult))
			{
				getModelService().detach(promoResult);
			}
			return Collections.emptyList();
		}

		final DiscountRAO discountRao = (DiscountRAO) action;
		final BigDecimal discountAmount = discountRao.getValue();

		adjustDiscountRaoValue(entry, discountRao, discountAmount);

		final RuleBasedOrderEntryAdjustActionModel actionModel = createOrderEntryAdjustAction(promoResult, action, entry,
				discountAmount);
		handleActionMetadata(action, actionModel);


		getPromotionActionService().createDiscountValue(discountRao, actionModel.getGuid(), entry);

		getModelService().saveAll(promoResult, actionModel, order, entry);
		recalculateIfNeeded(order);
		return Collections.singletonList(promoResult);
	}

	protected void adjustDiscountRaoValue(final AbstractOrderEntryModel entry, final DiscountRAO discountRao,
			final BigDecimal discountAmount)
	{
		BigDecimal amount = discountAmount;
		if (discountRao.isPerUnit())
		{
			final long appliedToQuantity = discountRao.getAppliedToQuantity();
			final BigDecimal fraction = valueOf((double)appliedToQuantity/entry.getQuantity().longValue());
			amount = amount.multiply(fraction);
		}
		discountRao.setValue(amount);
	}

	protected RuleBasedOrderEntryAdjustActionModel createOrderEntryAdjustAction(final PromotionResultModel promoResult,
			final AbstractRuleActionRAO action, final AbstractOrderEntryModel entry, final BigDecimal discountAmount)
	{
		final RuleBasedOrderEntryAdjustActionModel actionModel = createPromotionAction(promoResult, action);
		actionModel.setAmount(discountAmount);
		actionModel.setOrderEntryNumber(entry.getEntryNumber());
		actionModel.setOrderEntryProduct(entry.getProduct());
		actionModel.setOrderEntryQuantity(Long.valueOf(getConsumedQuantity(promoResult)));
		return actionModel;
	}

	@Override
	public void undo(final ItemModel action)
	{
		if (action instanceof RuleBasedOrderEntryAdjustActionModel)
		{
			handleUndoActionMetadata((RuleBasedOrderEntryAdjustActionModel) action);
			final AbstractOrderModel order = undoInternal((RuleBasedOrderEntryAdjustActionModel) action);
			recalculateIfNeeded(order);
		}
	}

	/**
	 * Sums up quantities of all consumed entries of given order entry.
	 *
	 * @param promoResult
	 * 		AbstractOrderEntryModel to find consumed quantity for
	 * @return consumed quantity of given order entry
	 */
	protected long getConsumedQuantity(final PromotionResultModel promoResult)
	{
		long consumedQuantity = 0;
		if (CollectionUtils.isNotEmpty(promoResult.getConsumedEntries()))
		{
			consumedQuantity = promoResult.getConsumedEntries().stream()
					.mapToLong(consumedEntry -> consumedEntry.getQuantity().longValue()).sum();
		}
		return consumedQuantity;
	}
	
}
