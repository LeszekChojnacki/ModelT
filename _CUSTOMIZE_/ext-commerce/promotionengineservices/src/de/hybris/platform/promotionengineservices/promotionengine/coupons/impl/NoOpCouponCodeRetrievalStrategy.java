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
package de.hybris.platform.promotionengineservices.promotionengine.coupons.impl;

import static java.util.Optional.empty;

import de.hybris.platform.promotionengineservices.promotionengine.coupons.CouponCodeRetrievalStrategy;
import de.hybris.platform.promotions.model.PromotionResultModel;

import java.util.Optional;
import java.util.Set;


/**
 * NoOpCouponCodeRetrievalStrategy provides the standard (no-op) implementation of the CouponCodeRetrievalStrategy
 */
public class NoOpCouponCodeRetrievalStrategy implements CouponCodeRetrievalStrategy
{
	@Override
	public Optional<Set<String>> getCouponCodesFromPromotion(final PromotionResultModel promotion)
	{
		return empty();
	}
}
