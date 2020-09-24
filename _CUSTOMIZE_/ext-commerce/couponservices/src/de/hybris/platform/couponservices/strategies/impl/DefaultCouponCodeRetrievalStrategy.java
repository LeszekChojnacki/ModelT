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
package de.hybris.platform.couponservices.strategies.impl;

import static java.util.Optional.empty;

import de.hybris.platform.couponservices.model.RuleBasedAddCouponActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.promotionengine.coupons.CouponCodeRetrievalStrategy;
import de.hybris.platform.promotions.model.PromotionResultModel;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * DefaultCouponCodeRetrievalStrategy is the default implementation of CouponCodeRetrievalStrategy (defined in extension
 * promotionengineservices)
 */
public class DefaultCouponCodeRetrievalStrategy implements CouponCodeRetrievalStrategy
{

	@Override
	public Optional<Set<String>> getCouponCodesFromPromotion(final PromotionResultModel promotionResult)
	{
		if (promotionResult.getPromotion() instanceof RuleBasedPromotionModel)
		{
			return Optional.of(Optional.ofNullable(promotionResult.getAllPromotionActions()).orElse(Collections.emptySet()).stream()
					.filter(action -> action instanceof RuleBasedAddCouponActionModel)
					.map(action -> ((RuleBasedAddCouponActionModel) action).getCouponCode()).collect(Collectors.toSet()));
		}
		return empty();
	}
}
