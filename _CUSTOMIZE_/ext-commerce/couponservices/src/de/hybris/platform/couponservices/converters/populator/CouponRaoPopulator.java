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
package de.hybris.platform.couponservices.converters.populator;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.rao.CouponRAO;
import de.hybris.platform.couponservices.services.CouponService;
import de.hybris.platform.ruleengineservices.rao.CartRAO;

import java.util.Collection;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Populates CartRAO.coupons from AbstractOrderModel.appliedCouponCodes.
 *
 */
public class CouponRaoPopulator implements Populator<AbstractOrderModel, CartRAO>
{
	private CouponService couponService;

	@Override
	public void populate(final AbstractOrderModel cartModel, final CartRAO cartRao)
	{
		Preconditions.checkNotNull(cartModel, "Cart model is not expected to be NULL here");
		Preconditions.checkNotNull(cartRao, "Cart RAO is not expected to be NULL here");

		final Collection<String> appliedCouponCodes = cartModel.getAppliedCouponCodes();
		if (isNotEmpty(appliedCouponCodes))
		{
			cartRao.setCoupons(appliedCouponCodes.stream().map(this::getCouponRAO).filter(Objects::nonNull).collect(toList()));
		}
	}

	protected CouponRAO getCouponRAO(final String couponCode)
	{
		return getCouponService().getValidatedCouponForCode(couponCode).map(this::toCouponRAO).map(couponRao -> {
			couponRao.setCouponCode(couponCode);
			return couponRao;
		}).orElse(null);
	}

	protected CouponRAO toCouponRAO(final AbstractCouponModel couponModel)
	{
		final CouponRAO couponRao = new CouponRAO();
		couponRao.setCouponId(couponModel.getCouponId());
		return couponRao;
	}

	protected CouponService getCouponService()
	{
		return couponService;
	}

	@Required
	public void setCouponService(final CouponService couponService)
	{
		this.couponService = couponService;
	}


}
