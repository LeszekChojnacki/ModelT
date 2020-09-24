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
package de.hybris.platform.couponfacades.converters.populator;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commercefacades.coupon.data.CouponData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.couponservices.model.AbstractCouponModel;


/**
 * Populator implementation for {@link AbstractCouponModel} as source and {@link CouponData} as target type.
 *
 */
public class CouponDataPopulator implements Populator<AbstractCouponModel, CouponData>
{
	@Override
	public void populate(final AbstractCouponModel source, final CouponData target)
	{
		validateParameterNotNullStandardMessage("AbstractCouponModel", source);
		validateParameterNotNullStandardMessage("CouponData", target);

		target.setCouponId(source.getCouponId());
		target.setName(source.getName());
		target.setActive(source.getActive().booleanValue());
		target.setStartDate(source.getStartDate());
		target.setEndDate(source.getEndDate());
	}
}
