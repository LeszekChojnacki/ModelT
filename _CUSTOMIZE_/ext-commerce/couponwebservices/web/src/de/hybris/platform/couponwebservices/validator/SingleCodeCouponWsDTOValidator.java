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
package de.hybris.platform.couponwebservices.validator;

import de.hybris.platform.couponwebservices.dto.SingleCodeCouponWsDTO;


/**
 * Single-code coupon validator for WS
 */
public class SingleCodeCouponWsDTOValidator extends AbstractCouponWsDTOValidator<SingleCodeCouponWsDTO>
{

	@Override
	protected Class<SingleCodeCouponWsDTO> getSupportingClass()
	{
		return SingleCodeCouponWsDTO.class;
	}

}
