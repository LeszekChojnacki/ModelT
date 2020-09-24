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
package de.hybris.platform.couponwebservices.populators;

import static org.springframework.util.Assert.notNull;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.couponservices.dao.CouponRedemptionDao;
import de.hybris.platform.couponservices.model.CouponRedemptionModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.couponwebservices.dto.CouponRedemptionWsDTO;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populator for {@link CouponRedemptionWsDTO}
 *
 */
public class CouponRedemptionWsPopulator implements Populator<SingleCodeCouponModel, CouponRedemptionWsDTO>
{
	private CouponRedemptionDao couponRedemptionDao;

	@Override
	public void populate(final SingleCodeCouponModel source, final CouponRedemptionWsDTO target)
	{
		notNull(source, "Parameter source cannot be null.");
		notNull(target, "Parameter target cannot be null.");

		target.setCouponId(source.getCouponId());
		target.setMaxRedemptionsLimitPerCustomer(source.getMaxRedemptionsPerCustomer());
		target.setMaxTotalRedemptionsLimit(source.getMaxTotalRedemptions());

		final List<CouponRedemptionModel> totalCouponRedemptions = getCouponRedemptionDao()
				.findCouponRedemptionsByCode(source.getCouponId());
		target.setTotalRedemptions(totalCouponRedemptions.size());
	}

	protected CouponRedemptionDao getCouponRedemptionDao()
	{
		return couponRedemptionDao;
	}

	@Required
	public void setCouponRedemptionDao(final CouponRedemptionDao couponRedemptionDao)
	{
		this.couponRedemptionDao = couponRedemptionDao;
	}
}
