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

import static java.util.Optional.ofNullable;
import static org.springframework.util.Assert.notNull;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponwebservices.dto.MultiCodeCouponWsDTO;
import de.hybris.platform.couponwebservices.util.CouponWsUtils;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populator for {@link MultiCodeCouponWsDTO} data model
 *
 */
public class MultiCodeCouponWsPopulator implements Populator<MultiCodeCouponModel, MultiCodeCouponWsDTO>
{

	private CouponWsUtils couponWsUtils;

	@Override
	public void populate(final MultiCodeCouponModel source, final MultiCodeCouponWsDTO target)
	{
		notNull(source, "Parameter source cannot be null.");
		notNull(target, "Parameter target cannot be null.");

		ofNullable(source.getStartDate()).map(getCouponWsUtils().getDateToStringMapper()).ifPresent(target::setStartDate);
		ofNullable(source.getEndDate()).map(getCouponWsUtils().getDateToStringMapper()).ifPresent(target::setEndDate);

		convertMultiCodeCoupon(source, target);
	}

	protected void convertMultiCodeCoupon(final MultiCodeCouponModel source, final MultiCodeCouponWsDTO target)
	{
		target.setCouponId(source.getCouponId());
		target.setName(source.getName());
		target.setActive(source.getActive());
		target.setCouponCodeNumber(source.getCouponCodeNumber());
		ofNullable(source.getCodeGenerationConfiguration()).map(c -> c.getName()).ifPresent(target::setCodeGenerationConfiguration);
	}

	protected CouponWsUtils getCouponWsUtils()
	{
		return couponWsUtils;
	}

	@Required
	public void setCouponWsUtils(final CouponWsUtils couponWsUtils)
	{
		this.couponWsUtils = couponWsUtils;
	}

}
