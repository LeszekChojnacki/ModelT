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
package de.hybris.platform.couponservices.dao.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.dao.CouponRedemptionDao;
import de.hybris.platform.couponservices.model.CouponRedemptionModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of the {@link CouponRedemptionDao} interface
 */
public class DefaultCouponRedemptionDao extends DefaultGenericDao<CouponRedemptionModel> implements CouponRedemptionDao
{
	private static final String COUPONCODENOTNULL = "String couponCode cannot be null";

	public DefaultCouponRedemptionDao()
	{
		super(CouponRedemptionModel._TYPECODE);
	}

	@Override
	public List<CouponRedemptionModel> findCouponRedemptionsByCode(final String couponCode)
	{
		ServicesUtil.validateParameterNotNull(couponCode, COUPONCODENOTNULL);
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(CouponRedemptionModel.COUPONCODE, couponCode);
		return find(params);
	}

	@Override
	public List<CouponRedemptionModel> findCouponRedemptionsByCodeAndOrder(final String couponCode,
			final AbstractOrderModel abstractOrder)
	{
		ServicesUtil.validateParameterNotNull(couponCode, COUPONCODENOTNULL);
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(CouponRedemptionModel.COUPONCODE, couponCode);
		params.put(CouponRedemptionModel.ORDER, abstractOrder);
		return find(params);
	}

	@Override
	public List<CouponRedemptionModel> findCouponRedemptionsByCodeAndUser(final String couponCode, final UserModel user)
	{
		ServicesUtil.validateParameterNotNull(couponCode, COUPONCODENOTNULL);
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(CouponRedemptionModel.COUPONCODE, couponCode);
		params.put(CouponRedemptionModel.USER, user);
		return find(params);
	}

	@Override
	public List<CouponRedemptionModel> findCouponRedemptionsByCodeOrderAndUser(final String couponCode,
			final AbstractOrderModel abstractOrder, final UserModel user)
	{
		ServicesUtil.validateParameterNotNull(couponCode, COUPONCODENOTNULL);
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(CouponRedemptionModel.COUPONCODE, couponCode);
		params.put(CouponRedemptionModel.ORDER, abstractOrder);
		params.put(CouponRedemptionModel.USER, user);
		return find(params);
	}


}
