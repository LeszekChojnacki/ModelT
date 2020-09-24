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

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.stream.Collectors.toList;

import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SortData;
import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.CouponService;
import de.hybris.platform.couponwebservices.CouponNotFoundException;
import de.hybris.platform.couponwebservices.InvalidCouponStateException;
import de.hybris.platform.couponwebservices.dto.AbstractCouponWsDTO;
import de.hybris.platform.couponwebservices.dto.CouponValidationResponseWsDTO;
import de.hybris.platform.couponwebservices.facades.CouponWsFacades;
import de.hybris.platform.couponwebservices.util.CouponWsUtils;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.paginated.dao.PaginatedGenericDao;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.cache.annotation.Cacheable;


/**
 * Abstract implementation of CouponWsFacades interface
 */
public abstract class AbstractCouponWsFacades<D extends AbstractCouponWsDTO, M extends AbstractCouponModel>
		implements CouponWsFacades<D>
{
	private CouponDao couponDao;
	private CouponService couponService;
	private ModelService modelService;
	private UserService userService;
	private CouponWsUtils couponWsUtils;
	private Converter<CouponResponse, CouponValidationResponseWsDTO> couponValidationResponseWsDTOConverter;


	@Override
	@Cacheable(value = "couponWsCache", key = "T(de.hybris.platform.webservicescommons.cache.CacheKeyGenerator).generateKey(false,false,'getCouponWsDTO',#value)")
	public D getCouponWsDTO(final String couponId)
	{
		final AbstractCouponModel couponModel = getCouponDao().findCouponById(couponId);

		return convert(couponModel)
				.orElseThrow(() -> new CouponNotFoundException("No coupon found for couponId [" + couponId + "]"));
	}

	@Override
	public D createCoupon(final D coupon)
	{
		ServicesUtil.validateParameterNotNull(coupon, "coupon data object cannot be empty");
		final M couponModel = createCouponModel(coupon);
		getModelService().save(couponModel);

		return convert(couponModel).orElse(null);
	}

	@Override
	public void updateCoupon(final D coupon)
	{
		ServicesUtil.validateParameterNotNull(coupon, "coupon data object cannot be empty");
		final M couponModel = updateCouponModel(coupon);
		getModelService().save(couponModel);
	}


	@Override
	public void updateCouponStatus(final String couponId, final Boolean active)
	{
		ServicesUtil.validateParameterNotNull(couponId, "couponId cannot be empty");
		ServicesUtil.validateParameterNotNull(active, "active cannot be empty");
		final AbstractCouponModel couponModel = getCouponWsUtils().getCouponById(couponId);

		assertCouponModelType(couponModel, couponId);

		if (!active.equals(couponModel.getActive()))
		{
			couponModel.setActive(active);
			getModelService().save(couponModel);
		}
	}

	@Override
	public CouponValidationResponseWsDTO validateCoupon(final String couponCode)
	{
		return validateCoupon(couponCode, StringUtils.EMPTY);
	}

	@Override
	public CouponValidationResponseWsDTO validateCoupon(final String couponCode, final String customerId)
	{
		ServicesUtil.validateParameterNotNull(couponCode, "couponId cannot be empty");
		final String couponId = getCouponId(couponCode);
		final AbstractCouponModel couponModel = getCouponWsUtils().getCouponById(couponId);

		assertCouponModelType(couponModel, couponId);

		final UserModel customer = StringUtils.isNotEmpty(customerId) ? getUserService().getUserForUID(customerId) : null;
		final CouponResponse couponResponse = getCouponService().validateCouponCode(couponCode, customer);

		final CouponValidationResponseWsDTO couponValidationResponseWsDTO = getCouponValidationResponseWsDTOConverter()
				.convert(couponResponse);
		couponValidationResponseWsDTO.setCouponId(couponId);
		if (!couponId.equals(couponCode))
		{
			couponValidationResponseWsDTO.setGeneratedCouponCode(couponCode);
		}
		return couponValidationResponseWsDTO;
	}

	protected abstract void assertCouponModelType(AbstractCouponModel couponModel, String couponId);

	protected abstract M updateCouponModel(D couponWsDTO);

	protected abstract Optional<D> convert(AbstractCouponModel couponModel);

	protected abstract M createCouponModel(D couponWsDTO);

	protected abstract PaginatedGenericDao<M> getCouponPaginatedGenericDao();

	protected abstract String getCouponId(final String couponCode);

	/**
	 * Converts the result of {@link de.hybris.platform.commerceservices.search.pagedata.SearchPageData}
	 *
	 * @param source
	 *           searchPageData containing original results
	 * @param <S>
	 *           original type of searchPageData's results
	 * @return converted SearchPageData
	 *
	 */
	protected <S extends AbstractCouponModel> SearchPageData<D> convertSearchPageData(final SearchPageData<S> source)
	{
		final SearchPageData<D> result = new SearchPageData<D>();
		result.setPagination(source.getPagination());
		result.setSorts(source.getSorts());
		result.setResults(source.getResults().stream().map(this::convert).filter(Optional::isPresent).map(Optional::get).collect(toList()));
		return result;
	}

	/**
	 * Converts the result of {@link de.hybris.platform.commerceservices.search.pagedata.SearchPageData}
	 *
	 * @param source
	 *           searchPageData containing original results
	 * @param <S>
	 *           original type of searchPageData's results
	 * @return converted SearchPageData
	 */
	protected <S extends AbstractCouponModel> de.hybris.platform.core.servicelayer.data.SearchPageData<D> convertSearchPageData(final de.hybris.platform.core.servicelayer.data.SearchPageData<S> source)
	{
		final de.hybris.platform.core.servicelayer.data.SearchPageData<D> result = buildSearchPageData(source.getPagination(),
				source.getSorts());
		result.setResults(source.getResults().stream().map(this::convert).map(Optional::get).collect(toList()));
		return result;
	}

	protected void assertCouponNotActive(final AbstractCouponModel couponModel, final String message)
	{
		if (BooleanUtils.isTrue(couponModel.getActive()))
		{
			throw new InvalidCouponStateException(message, "active", "active");
		}
	}

	@Override
	public de.hybris.platform.core.servicelayer.data.SearchPageData getCoupons(final PaginationData pagination,
			final List<SortData> sorts)
	{
		validateParameterNotNullStandardMessage("pagination",pagination);
		validateParameterNotNullStandardMessage("sorts", sorts);

		final de.hybris.platform.core.servicelayer.data.SearchPageData searchPageData = buildSearchPageData(pagination, sorts);

		final de.hybris.platform.core.servicelayer.data.SearchPageData<M> result = getCouponPaginatedGenericDao()
				.find(searchPageData);
		return convertSearchPageData(result);
	}

	protected de.hybris.platform.core.servicelayer.data.SearchPageData buildSearchPageData(final PaginationData pagination,
			final List<SortData> sorts)
	{
		final de.hybris.platform.core.servicelayer.data.SearchPageData searchPageData = new de.hybris.platform.core.servicelayer.data.SearchPageData();
		searchPageData.setPagination(pagination);
		searchPageData.setSorts(sorts);
		return searchPageData;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected CouponDao getCouponDao()
	{
		return couponDao;
	}

	@Required
	public void setCouponDao(final CouponDao couponDao)
	{
		this.couponDao = couponDao;
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

	protected CouponService getCouponService()
	{
		return couponService;
	}

	@Required
	public void setCouponService(final CouponService couponService)
	{
		this.couponService = couponService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected Converter<CouponResponse, CouponValidationResponseWsDTO> getCouponValidationResponseWsDTOConverter()
	{
		return couponValidationResponseWsDTOConverter;
	}

	@Required
	public void setCouponValidationResponseWsDTOConverter(
			final Converter<CouponResponse, CouponValidationResponseWsDTO> couponValidationResponseWsDTOConverter)
	{
		this.couponValidationResponseWsDTOConverter = couponValidationResponseWsDTOConverter;
	}

}
