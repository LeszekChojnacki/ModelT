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

import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link CouponDao}.
 */
public class DefaultCouponDao extends AbstractItemDao implements CouponDao
{
	private static final String COUPONID = "couponId";

	private static final String GET_COUPON_BY_ID_QUERY = "SELECT {" + Item.PK + "} " + "FROM   {" + AbstractCouponModel._TYPECODE // NOSONAR
			+ "} " + "WHERE  {" + AbstractCouponModel.COUPONID + "} = ?couponId"; // NOSONAR

	private static final String GET_SINGLECODE_COUPON_QUERY = "SELECT {" + Item.PK + "} " + "FROM   {" // NOSONAR
			+ SingleCodeCouponModel._TYPECODE + "} " + "WHERE  {" + SingleCodeCouponModel.COUPONID + "} = ?couponId"; // NOSONAR

	private static final String GET_MULTICODE_COUPON_QUERY = "SELECT {" + MultiCodeCouponModel.PK + "} " + "FROM   {" // NOSONAR
			+ MultiCodeCouponModel._TYPECODE + "} " + "WHERE  {" + MultiCodeCouponModel.COUPONID + "} = ?couponId"; // NOSONAR

	private static final String GET_MULTICODE_COUPON_BY_CONFIG_QUERY = "SELECT {" + MultiCodeCouponModel.PK + "} " + "FROM   {" // NOSONAR
			+ MultiCodeCouponModel._TYPECODE + "} " + "WHERE  {" + MultiCodeCouponModel.CODEGENERATIONCONFIGURATION + "} = ?config"; // NOSONAR

	@Override
	public AbstractCouponModel findCouponById(final String couponId)
	{
		ServicesUtil.validateParameterNotNull(couponId, "String couponId cannot be null");
		final Map<String, String> params = Collections.singletonMap(COUPONID, couponId);

		return getFlexibleSearchService().searchUnique(new FlexibleSearchQuery(GET_COUPON_BY_ID_QUERY, params));
	}

	@Override
	public SingleCodeCouponModel findSingleCodeCouponById(final String couponId)
	{
		ServicesUtil.validateParameterNotNull(couponId, "String couponId must not be null");
		final Map<String, String> params = Collections.singletonMap(COUPONID, couponId);

		return getFlexibleSearchService().searchUnique(new FlexibleSearchQuery(GET_SINGLECODE_COUPON_QUERY, params));
	}

	@Override
	public MultiCodeCouponModel findMultiCodeCouponById(final String couponId)
	{
		ServicesUtil.validateParameterNotNull(couponId, "String couponId must not be null");
		final Map<String, String> params = Collections.singletonMap(COUPONID, couponId);

		return getFlexibleSearchService().searchUnique(new FlexibleSearchQuery(GET_MULTICODE_COUPON_QUERY, params));
	}

	@Override
	public List<MultiCodeCouponModel> findMultiCodeCouponsByCodeConfiguration(final CodeGenerationConfigurationModel config)
	{
		ServicesUtil.validateParameterNotNull(config, "CouponCodeGenerationConfiguration config must not be null");
		final Map<String, Object> params = Collections.singletonMap("config", config);
		return getFlexibleSearchService().<MultiCodeCouponModel> search(GET_MULTICODE_COUPON_BY_CONFIG_QUERY, params).getResult();
	}

}
