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
package de.hybris.platform.couponwebservices.facades.impl;

import static java.util.Optional.ofNullable;

import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.couponwebservices.CouponNotFoundException;
import de.hybris.platform.couponwebservices.dto.SingleCodeCouponWsDTO;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.paginated.dao.PaginatedGenericDao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of CouponWsFacades for single-code coupon
 */
public class DefaultSingleCodeCouponWsFacades extends AbstractCouponWsFacades<SingleCodeCouponWsDTO, SingleCodeCouponModel>
{
	private PaginatedGenericDao<SingleCodeCouponModel> singleCodeCouponPaginatedGenericDao;
	private Converter<SingleCodeCouponModel, SingleCodeCouponWsDTO> singleCodeCouponWsDTOConverter;

	@Override
	protected Optional<SingleCodeCouponWsDTO> convert(final AbstractCouponModel couponModel)
	{
		Optional<SingleCodeCouponWsDTO> couponWsDTO = Optional.empty();
		if (couponModel instanceof SingleCodeCouponModel)
		{
			couponWsDTO = Optional.ofNullable(getSingleCodeCouponWsDTOConverter().convert((SingleCodeCouponModel) couponModel));
		}
		return couponWsDTO;
	}

	@Override
	protected SingleCodeCouponModel createCouponModel(final SingleCodeCouponWsDTO couponDto)
	{
		final SingleCodeCouponModel couponModel = getModelService().create(SingleCodeCouponModel.class);
		couponModel.setCouponId(couponDto.getCouponId());
		ofNullable(couponDto.getStartDate()).map(getCouponWsUtils().getStringToDateMapper()).ifPresent(couponModel::setStartDate);
		ofNullable(couponDto.getEndDate()).map(getCouponWsUtils().getStringToDateMapper()).ifPresent(couponModel::setEndDate);
		couponModel.setName(couponDto.getName());
		couponModel.setMaxRedemptionsPerCustomer(couponDto.getMaxRedemptionsPerCustomer());
		couponModel.setMaxTotalRedemptions(couponDto.getMaxTotalRedemptions());
		return couponModel;
	}

	@Override
	protected SingleCodeCouponModel updateCouponModel(final SingleCodeCouponWsDTO couponDto)
	{
		AbstractCouponModel couponModel = null;
		try
		{
			couponModel = getCouponDao().findCouponById(couponDto.getCouponId());
		}
		catch (final ModelNotFoundException ex)
		{
			throw new CouponNotFoundException("No single code coupon found for couponId [" + couponDto.getCouponId() + "]",
					"invalid", "couponId");
		}

		assertCouponNotActive(couponModel, "Can't update active coupon");

		assertCouponModelType(couponModel, couponDto.getCouponId());

		final SingleCodeCouponModel singleCodeCouponModel = (SingleCodeCouponModel) couponModel;
		singleCodeCouponModel
				.setStartDate(ofNullable(couponDto.getStartDate()).map(getCouponWsUtils().getStringToDateMapper()).orElse(null));
		singleCodeCouponModel
				.setEndDate(ofNullable(couponDto.getEndDate()).map(getCouponWsUtils().getStringToDateMapper()).orElse(null));
		singleCodeCouponModel.setName(couponDto.getName());
		singleCodeCouponModel.setMaxRedemptionsPerCustomer(couponDto.getMaxRedemptionsPerCustomer());
		singleCodeCouponModel.setMaxTotalRedemptions(couponDto.getMaxTotalRedemptions());
		return singleCodeCouponModel;
	}

	@Override
	protected void assertCouponModelType(final AbstractCouponModel couponModel, final String couponId)
	{
		getCouponWsUtils().assertValidSingleCodeCoupon(couponModel, couponId);
	}

	@Override
	protected String getCouponId(final String couponCode)
	{
		// for SingleCodeCoupons the coupon code is the coupon id
		return couponCode;
	}

	protected Converter<SingleCodeCouponModel, SingleCodeCouponWsDTO> getSingleCodeCouponWsDTOConverter()
	{
		return singleCodeCouponWsDTOConverter;
	}

	@Required
	public void setSingleCodeCouponWsDTOConverter(
			final Converter<SingleCodeCouponModel, SingleCodeCouponWsDTO> singleCodeCouponWsDTOConverter)
	{
		this.singleCodeCouponWsDTOConverter = singleCodeCouponWsDTOConverter;
	}

	protected PaginatedGenericDao<SingleCodeCouponModel> getSingleCodeCouponPaginatedGenericDao()
	{
		return singleCodeCouponPaginatedGenericDao;
	}

	@Required
	public void setSingleCodeCouponPaginatedGenericDao(
			final PaginatedGenericDao<SingleCodeCouponModel> singleCodeCouponPaginatedGenericDao)
	{
		this.singleCodeCouponPaginatedGenericDao = singleCodeCouponPaginatedGenericDao;
	}

	@Override
	protected PaginatedGenericDao<SingleCodeCouponModel> getCouponPaginatedGenericDao()
	{
		return getSingleCodeCouponPaginatedGenericDao();
	}
}
