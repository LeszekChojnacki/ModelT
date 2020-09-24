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
package de.hybris.platform.couponservices;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.services.CouponService;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.util.PromotionResultUtils;
import de.hybris.platform.ruleengine.RuleActionMetadataHandler;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Action metadata processor to handle coupon metadata.
 */
public class RuleActionCouponMetadataHandler implements RuleActionMetadataHandler<AbstractRuleBasedPromotionActionModel>
{
	private ModelService modelService;
	private String metadataId;
	private CouponService couponService;
	private PromotionResultUtils promotionResultUtils;

	protected Set<String> getUsedCouponCodes(final AbstractOrderModel order)
	{
		return order.getAllPromotionResults().stream().flatMap(p -> p.getActions().stream())
				.filter(a -> a instanceof AbstractRuleBasedPromotionActionModel).map(a -> (AbstractRuleBasedPromotionActionModel) a)
				.filter(a -> CollectionUtils.isNotEmpty(a.getUsedCouponCodes())).flatMap(a -> a.getUsedCouponCodes().stream())
				.collect(Collectors.toSet());
	}

	@Override
	public void handle(final AbstractRuleBasedPromotionActionModel actionModel, final String metadataValue)
	{
		validateParameterNotNull(actionModel, "ActionModel can't be null");
		validateParameterNotNull(actionModel.getPromotionResult(), "PromotionResult of ActionModel can't be null");
		final AbstractOrderModel order = getPromotionResultUtils().getOrder(actionModel.getPromotionResult());
		validateParameterNotNull(order, "Order of ActionModel can't be null");
		if (CollectionUtils.isNotEmpty(order.getAppliedCouponCodes()))
		{
			final Set<String> couponIdsFromActionMetadata = Arrays.asList(metadataValue.split(",")).stream()
					.map(s -> s.replaceAll("\"", "").trim()).collect(Collectors.toSet());

			final Set<String> usedCouponCodes = getUsedCouponCodes(order);

			final List<String> orderCouponCodesToUse = order.getAppliedCouponCodes().stream()
					.filter(cc -> !usedCouponCodes.contains(cc) && isCouponPresentInActionMetadata(couponIdsFromActionMetadata, cc))
					.collect(Collectors.toList());

			usedCouponCodes.addAll(orderCouponCodesToUse);
			actionModel.setUsedCouponCodes(orderCouponCodesToUse);

			final List<String> actionMetadataHandlers = nonNull(actionModel.getMetadataHandlers()) ? new ArrayList<>(
					actionModel.getMetadataHandlers()) : new ArrayList<>();
			if (!actionMetadataHandlers.contains(getMetadataId()))
			{
				actionMetadataHandlers.add(getMetadataId());
				actionModel.setMetadataHandlers(actionMetadataHandlers);
			}
		}
	}

	protected boolean isCouponPresentInActionMetadata(final Set<String> couponIdsFromActionMetadata, final String couponCode)
	{
		final String couponId = getCouponIdByCouponCode(couponCode);
		return nonNull(couponId) && couponIdsFromActionMetadata.contains(couponId);
	}

	protected String getCouponIdByCouponCode(final String couponCode)
	{
		return getCouponService().getCouponForCode(couponCode).map(AbstractCouponModel::getCouponId).orElse(null);
	}

	@Override
	public void undoHandle(final AbstractRuleBasedPromotionActionModel actionModel)
	{
		validateParameterNotNull(actionModel, "ActionModel can't be null");
		if (CollectionUtils.isNotEmpty(actionModel.getMetadataHandlers()))
		{
			final List<String> actionMetadataHandlers = actionModel.getMetadataHandlers().stream()
					.filter(mdid -> !mdid.equals(getMetadataId())).collect(Collectors.toList());
			actionModel.setMetadataHandlers(actionMetadataHandlers);
		}
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected String getMetadataId()
	{
		return metadataId;
	}

	@Required
	public void setMetadataId(final String metadataId)
	{
		this.metadataId = metadataId;
	}

	protected CouponService getCouponService()
	{
		return couponService;
	}

	@Required
	public void setCouponService(final CouponService couponService)
	{
		this.couponService = couponService;
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