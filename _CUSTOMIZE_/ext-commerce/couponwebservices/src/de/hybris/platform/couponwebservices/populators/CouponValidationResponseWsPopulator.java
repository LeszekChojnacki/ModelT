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
/**
 *
 */
package de.hybris.platform.couponwebservices.populators;

import static org.springframework.util.Assert.notNull;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponwebservices.dto.CouponValidationResponseWsDTO;
import de.hybris.platform.util.localization.Localization;

import org.apache.commons.lang.StringUtils;


/**
 * Populator for CouponResponse object
 *
 */
public class CouponValidationResponseWsPopulator implements Populator<CouponResponse, CouponValidationResponseWsDTO>
{
	@Override
	public void populate(final CouponResponse source, final CouponValidationResponseWsDTO target)
	{
		notNull(source, "Parameter source cannot be null.");
		notNull(target, "Parameter target cannot be null.");

		target.setCouponId(source.getCouponId());
		target.setValid(source.getSuccess());
		if (StringUtils.isNotEmpty(source.getMessage()))
		{
			target.setMessage(Localization.getLocalizedString(source.getMessage()));
		}
	}
}
