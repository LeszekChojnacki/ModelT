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
package de.hybris.platform.couponservices.rule.strategies.impl.mappers;

import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Performs mapping between AbstractCouponModel and a String representation of it instance.
 */
public class CouponRuleParameterValueMapper implements RuleParameterValueMapper<AbstractCouponModel>
{
	private CouponDao couponDao;

	@Override
	public AbstractCouponModel fromString(final String value)
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");
		try
		{
			return getCouponById(value);
		}
		catch (final ModelNotFoundException ex)
		{
			throw new RuleParameterValueMapperException("Cannot find coupon with the couponId: " + value, ex);
		}
	}

	protected AbstractCouponModel getCouponById(final String couponId)
	{
		return getCouponDao().findCouponById(couponId);
	}

	@Override
	public String toString(final AbstractCouponModel value)
	{
		ServicesUtil.validateParameterNotNull(value, "CouponModel cannot be null");
		return value.getCouponId();
	}

	protected CouponDao getCouponDao()
	{
		return couponDao;
	}

	@Required
	public void setCouponDao(final CouponDao couponDao)
	{
		this.couponDao = couponDao;
	}
}
