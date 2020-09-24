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
package de.hybris.platform.couponservices.rao.providers.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOFactsExtractor;

import java.util.HashSet;
import java.util.Set;


/**
 * extension of Default cart RAO provider, adding the coupon-awareness functionality
 */
public class CouponCartRaoExtractor implements RAOFactsExtractor
{
	public static final String EXPAND_COUPONS = "EXPAND_COUPONS";

	@Override
	public Set expandFact(final Object fact)
	{
		checkArgument(fact instanceof CartRAO, "CartRAO type is expected here");
		final Set<Object> facts = new HashSet<>();
		final CartRAO cartRAO = (CartRAO) fact;
		if (isNotEmpty(cartRAO.getCoupons()))
		{
			facts.addAll(cartRAO.getCoupons());
		}
		return facts;
	}

	@Override
	public String getTriggeringOption()
	{
		return EXPAND_COUPONS;
	}

	@Override
	public boolean isMinOption()
	{
		return false;
	}

	@Override
	public boolean isDefault()
	{
		return true;
	}



}
