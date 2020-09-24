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

import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SortData;
import de.hybris.platform.couponwebservices.CouponNotFoundException;
import de.hybris.platform.couponwebservices.dto.AbstractCouponWsDTO;
import de.hybris.platform.couponwebservices.dto.CouponValidationResponseWsDTO;

import java.util.List;


/**
 * Interface declaring basic web-services facade operations on coupons
 */
public interface CouponWsFacades<T extends AbstractCouponWsDTO>
{
	/**
	 * Given the couponId string, returns the coupon DTO, otherwise throws exception
	 *
	 * @param couponId
	 *           a string to be used to find a coupon
	 * @return - an instance of coupon DTO
	 */
	T getCouponWsDTO(String couponId);

	/**
	 * Creates the coupon, given the coupon data. Throws exception otherwise
	 *
	 * @param coupon
	 *           - data to create coupon with
	 * @return - a new instance of coupon
	 */
	T createCoupon(T coupon);

	/**
	 * Updates the coupon identified by it couponId
	 *
	 * @param coupon
	 *           - data to update coupon with
	 */
	void updateCoupon(T coupon);

	/**
	 * Updates status of a coupon identified by it couponId
	 *
	 * @param couponId
	 *           - identifier of a coupon to set status
	 * @param active
	 *           - coupon status to set
	 */
	void updateCouponStatus(String couponId, Boolean active);

	/**
	 * Validates the given couponId, throws an exception if coupon id is invalid
	 *
	 * @param couponId
	 *           a string to be used to validate a coupon
	 * @throws CouponNotFoundException
	 * @return - an instance of {@link CouponValidationResponseWsDTO}
	 */
	CouponValidationResponseWsDTO validateCoupon(String couponId);

	/**
	 * Validates the given couponId for the given customer, throws an exception if coupon id or customer id is invalid
	 *
	 * @param couponId
	 *           a string to be used to validate a coupon
	 * @param customerId
	 *           the user id
	 * @throws CouponNotFoundException
	 * @return - an instance of {@link CouponValidationResponseWsDTO}
	 */
	CouponValidationResponseWsDTO validateCoupon(String couponId, String customerId);

	/**
	 * Returns a restricted size list (page) of available coupons
	 *
	 * @param pagination - pagination instructions
	 * @param sort - sort instructions
	 * @return - an instance of {@link de.hybris.platform.core.servicelayer.data.SearchPageData} containing a list of coupons, registered in the system
	 */
	de.hybris.platform.core.servicelayer.data.SearchPageData getCoupons(PaginationData pagination, List<SortData> sort);
}
