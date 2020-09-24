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

import de.hybris.platform.couponservices.dao.CodeGenerationConfigurationDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.couponwebservices.CodeGenerationConfigurationNotFoundException;
import de.hybris.platform.couponwebservices.CouponNotFoundException;
import de.hybris.platform.couponwebservices.CouponRequestWsError;
import de.hybris.platform.couponwebservices.InvalidCouponStateException;
import de.hybris.platform.couponwebservices.dto.MultiCodeCouponWsDTO;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.paginated.dao.PaginatedGenericDao;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of CouponWsFacades for multi-code coupon
 */
public class DefaultMultiCodeCouponWsFacades extends AbstractCouponWsFacades<MultiCodeCouponWsDTO, MultiCodeCouponModel>
{
	private CodeGenerationConfigurationDao codeGenerationConfigurationDao;
	private PaginatedGenericDao<MultiCodeCouponModel> multiCodeCouponPaginatedGenericDao;
	private Converter<MultiCodeCouponModel, MultiCodeCouponWsDTO> multiCodeCouponWsDTOConverter;
	private CouponCodeGenerationService couponCodeGenerationService;

	private static final String REASONINVALID = "invalid";

	@Override
	protected Optional<MultiCodeCouponWsDTO> convert(final AbstractCouponModel couponModel)
	{
		Optional<MultiCodeCouponWsDTO> couponWsDTO = Optional.empty();
		if (couponModel instanceof MultiCodeCouponModel)
		{
			couponWsDTO = Optional.ofNullable(getMultiCodeCouponWsDTOConverter().convert((MultiCodeCouponModel) couponModel));
		}
		return couponWsDTO;
	}

	@Override
	protected MultiCodeCouponModel createCouponModel(final MultiCodeCouponWsDTO couponDto)
	{
		final MultiCodeCouponModel couponModel = getModelService().create(MultiCodeCouponModel.class);
		couponModel.setCouponId(couponDto.getCouponId());

		ofNullable(couponDto.getStartDate()).map(getCouponWsUtils().getStringToDateMapper()).ifPresent(couponModel::setStartDate);
		ofNullable(couponDto.getEndDate()).map(getCouponWsUtils().getStringToDateMapper()).ifPresent(couponModel::setEndDate);
		couponModel.setName(couponDto.getName());
		ofNullable(couponDto.getCodeGenerationConfiguration()).ifPresent(n -> getCodeGenerationConfigurationDao()
				.findCodeGenerationConfigurationByName(n).ifPresent(couponModel::setCodeGenerationConfiguration));

		return couponModel;
	}

	@Override
	protected MultiCodeCouponModel updateCouponModel(final MultiCodeCouponWsDTO couponDto)
	{
		ServicesUtil.validateParameterNotNull(couponDto.getCodeGenerationConfiguration(),
				"Code Generation Configuration cannot be empty");
		AbstractCouponModel couponModel = null;
		try
		{
			couponModel = getCouponDao().findCouponById(couponDto.getCouponId());
		}
		catch (final ModelNotFoundException ex)
		{
			throw new CouponNotFoundException("No multi code coupon found for couponId [" + couponDto.getCouponId() + "]",
					REASONINVALID, "couponId");
		}
		assertCouponNotActive(couponModel, "Can't update active coupon");

		getCouponWsUtils().assertValidMultiCodeCoupon(couponModel, couponDto.getCouponId());

		final MultiCodeCouponModel multiCodeCouponModel = (MultiCodeCouponModel) couponModel;
		multiCodeCouponModel
				.setStartDate(ofNullable(couponDto.getStartDate()).map(getCouponWsUtils().getStringToDateMapper()).orElse(null));
		multiCodeCouponModel
				.setEndDate(ofNullable(couponDto.getEndDate()).map(getCouponWsUtils().getStringToDateMapper()).orElse(null));
		multiCodeCouponModel.setName(couponDto.getName());

		if (CollectionUtils.isNotEmpty(multiCodeCouponModel.getGeneratedCodes()))
		{
			throw new InvalidCouponStateException(
					"Multi code coupon already has generated codes for current Code Generation Configuration", REASONINVALID,
					"generatedCodes");
		}
		final CodeGenerationConfigurationModel codeGenerationConfiguration = codeGenerationConfigurationDao
				.findCodeGenerationConfigurationByName(couponDto.getCodeGenerationConfiguration())
				.orElseThrow(() -> new CodeGenerationConfigurationNotFoundException(
						"No Code Generation Configuration found for name [" + couponDto.getCodeGenerationConfiguration() + "]",
						REASONINVALID, "codeGenerationConfiguration"));
		multiCodeCouponModel.setCodeGenerationConfiguration(codeGenerationConfiguration);

		return multiCodeCouponModel;

	}

	@Override
	protected void assertCouponModelType(final AbstractCouponModel couponModel, final String couponId)
	{
		getCouponWsUtils().assertValidMultiCodeCoupon(couponModel, couponId);
	}

	@Override
	protected String getCouponId(final String couponCode)
	{
		// for multi code coupons the coupon id is the prefix of the coupon code
		final String couponId = getCouponCodeGenerationService().extractCouponPrefix(couponCode);
		if (StringUtils.isEmpty(couponId))
		{
			throw new CouponRequestWsError(
					"The generated multi code coupon provided for validation is not valid[" + couponCode + "]", REASONINVALID,
					"Invalid arguments in the request");
		}
		return couponId;
	}

	protected CodeGenerationConfigurationDao getCodeGenerationConfigurationDao()
	{
		return codeGenerationConfigurationDao;
	}

	@Required
	public void setCodeGenerationConfigurationDao(final CodeGenerationConfigurationDao codeGenerationConfigurationDao)
	{
		this.codeGenerationConfigurationDao = codeGenerationConfigurationDao;
	}

	protected Converter<MultiCodeCouponModel, MultiCodeCouponWsDTO> getMultiCodeCouponWsDTOConverter()
	{
		return multiCodeCouponWsDTOConverter;
	}

	@Required
	public void setMultiCodeCouponWsDTOConverter(
			final Converter<MultiCodeCouponModel, MultiCodeCouponWsDTO> multiCodeCouponWsDTOConverter)
	{
		this.multiCodeCouponWsDTOConverter = multiCodeCouponWsDTOConverter;
	}

	protected CouponCodeGenerationService getCouponCodeGenerationService()
	{
		return couponCodeGenerationService;
	}

	@Required
	public void setCouponCodeGenerationService(final CouponCodeGenerationService couponCodeGenerationService)
	{
		this.couponCodeGenerationService = couponCodeGenerationService;
	}

	protected PaginatedGenericDao<MultiCodeCouponModel> getMultiCodeCouponPaginatedGenericDao()
	{
		return multiCodeCouponPaginatedGenericDao;
	}

	@Required
	public void setMultiCodeCouponPaginatedGenericDao(
			final PaginatedGenericDao<MultiCodeCouponModel> multiCodeCouponPaginatedGenericDao)
	{
		this.multiCodeCouponPaginatedGenericDao = multiCodeCouponPaginatedGenericDao;
	}

	@Override
	protected PaginatedGenericDao<MultiCodeCouponModel> getCouponPaginatedGenericDao()
	{
		return getMultiCodeCouponPaginatedGenericDao();
	}
}
