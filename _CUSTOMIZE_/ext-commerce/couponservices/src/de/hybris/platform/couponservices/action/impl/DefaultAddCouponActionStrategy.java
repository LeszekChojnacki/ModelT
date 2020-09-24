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
package de.hybris.platform.couponservices.action.impl;

import static java.util.Objects.isNull;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.couponservices.model.RuleBasedAddCouponActionModel;
import de.hybris.platform.promotionengineservices.action.impl.AbstractRuleActionStrategy;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.AddCouponRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulates logic of adding coupon as rule action.
 */
public class DefaultAddCouponActionStrategy extends AbstractRuleActionStrategy<RuleBasedAddCouponActionModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultAddCouponActionStrategy.class);

	@Override
	public List<PromotionResultModel> apply(final AbstractRuleActionRAO action)
	{
		if (!(action instanceof AddCouponRAO))
		{
			LOG.error("cannot apply {}, action is not of type AddCouponRAO, but {}", getClass().getSimpleName(), action);
			return Collections.emptyList();
		}
		final AddCouponRAO addCouponAction = (AddCouponRAO) action;
		if (!(addCouponAction.getAppliedToObject() instanceof CartRAO))
		{
			LOG.error("cannot apply {}, appliedToObject is not of type CartRAO, but {}", getClass().getSimpleName(),
					action.getAppliedToObject());
			return Collections.emptyList();
		}

		final PromotionResultModel promoResult = getPromotionActionService().createPromotionResult(action);
		if (promoResult == null)
		{
			LOG.error("cannot apply {}, promotionResult could not be created.", getClass().getSimpleName());
			return Collections.emptyList();
		}
		final AbstractOrderModel order = getPromotionResultUtils().getOrder(promoResult);
		if (isNull(order))
		{
			LOG.error("cannot apply {}, order or cart not found: {}", getClass().getSimpleName(), order);
			// detach the promotion result if its not saved yet.
			if (getModelService().isNew(promoResult))
			{
				getModelService().detach(promoResult);
			}
			return Collections.emptyList();
		}
		if (addCouponAction.getCouponId() == null)
		{
			LOG.error("CouponId can not be null!");
			return Collections.emptyList();
		}
		final RuleBasedAddCouponActionModel actionModel = createPromotionAction(promoResult, action);
		handleActionMetadata(action, actionModel);
		actionModel.setCouponId(addCouponAction.getCouponId());
		//	For Give Away MultiCode Coupons the couponCode would be generated and updated later using GiveAwayMultiCodeCouponGenerationHook during the PlaceOrder.
		actionModel.setCouponCode(addCouponAction.getCouponId());
		LOG.debug("Coupon with code {} as a promotion applied", addCouponAction.getCouponId());
		getModelService().saveAll(promoResult, actionModel, order);
		return Collections.singletonList(promoResult);
	}

	@Override
	public void undo(final ItemModel item)
	{
		if (item instanceof RuleBasedAddCouponActionModel)
		{
			handleUndoActionMetadata((RuleBasedAddCouponActionModel) item);
			final RuleBasedAddCouponActionModel action = (RuleBasedAddCouponActionModel) item;
			final String couponId = action.getCouponId();
			final AbstractOrderModel order = getPromotionResultUtils().getOrder(action.getPromotionResult());
			undoInternal(action);
			getModelService().save(order);
			LOG.debug("Coupon with code {} as a promotion removed", couponId);
		}
	}
}
