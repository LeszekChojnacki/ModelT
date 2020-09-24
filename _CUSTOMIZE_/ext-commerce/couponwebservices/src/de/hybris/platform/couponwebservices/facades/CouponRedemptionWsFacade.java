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
package de.hybris.platform.couponwebservices.facades;

import de.hybris.platform.couponwebservices.dto.CouponRedemptionWsDTO;


/**
 *
 * Facade for coupon redemption WS
 */
public interface CouponRedemptionWsFacade
{
	/**
	 * Provides single code coupon redemption information
	 * @param couponId
	 *           coupon Id for which redemption DTO is to be populated
	 * @param customerId
	 *           customer Id for which redemption DTO is to be populated
	 * @return instance of {@link CouponRedemptionWsDTO} the coupon redemption
	 *
	 */
	CouponRedemptionWsDTO getSingleCodeCouponRedemption(String couponId, String customerId);
}
