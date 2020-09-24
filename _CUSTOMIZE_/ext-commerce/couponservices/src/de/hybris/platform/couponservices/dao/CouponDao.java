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
package de.hybris.platform.couponservices.dao;

import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.List;


/**
 * Data Access Object for AbstractCoupon Model.
 */
public interface CouponDao extends Dao
{
	/**
	 * Finds the AbstractCouponModel by its couponId.
	 *
	 * @param couponId
	 *           the couponId
	 *
	 * @return AbstractCouponModel by its couponId.
	 */
	AbstractCouponModel findCouponById(final String couponId);

	/**
	 * Finds the SingleCodeCouponModel by its couponId.
	 *
	 * @param couponId
	 *           the couponId
	 *
	 * @return SingleCodeCouponModel by its couponId.
	 */
	SingleCodeCouponModel findSingleCodeCouponById(final String couponId);

	/**
	 * Returns the MultiCodeCoupon for the given couponId.
	 *
	 * @param couponId
	 *           the coupon id
	 *
	 * @return The MultiCodeCoupon for the given coupon id or null if none is found
	 */
	MultiCodeCouponModel findMultiCodeCouponById(final String couponId);

	/**
	 * Returns the list of MultiCodeCoupons that have the given configuration.
	 *
	 * @param config
	 *           the configuration
	 *
	 * @return The list of muli code coupons that use the given configuration
	 */
	List<MultiCodeCouponModel> findMultiCodeCouponsByCodeConfiguration(CodeGenerationConfigurationModel config);

}
