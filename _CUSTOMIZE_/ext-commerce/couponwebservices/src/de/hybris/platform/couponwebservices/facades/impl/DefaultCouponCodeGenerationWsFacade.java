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
import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.couponwebservices.CouponCodesNotFoundWsException;
import de.hybris.platform.couponwebservices.facades.CouponCodeGenerationWsFacade;
import de.hybris.platform.couponwebservices.util.CouponWsUtils;
import de.hybris.platform.servicelayer.media.MediaService;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link CouponCodeGenerationWsFacade} WS facade interface
 */
public class DefaultCouponCodeGenerationWsFacade implements CouponCodeGenerationWsFacade
{

	private static final String NO_COUPON_CODE_FOUND_TEMPLATE = "No codes found for couponId [%s] and batchCode [%s]";

	private CouponCodeGenerationService couponCodeGenerationService;
	private CouponDao couponDao;
	private MediaService mediaService;
	private CouponWsUtils couponWsUtils;

	@Override
	public Optional<MediaModel> generateCouponCodes(final String couponId, final int batchsize)
	{
		validateParameterNotNullStandardMessage("couponId", couponId); // NOSONAR

		final MultiCodeCouponModel multiCodeCoupon = getCouponWsUtils().getValidMultiCodeCoupon(couponId);
		return getCouponCodeGenerationService().generateCouponCodes(multiCodeCoupon, batchsize);
	}

	@Override
	public Collection<MediaModel> getCouponCodeBatches(final String couponId)
	{
		validateParameterNotNullStandardMessage("couponId", couponId); // NOSONAR

		return getCouponWsUtils().getValidMultiCodeCoupon(couponId).getGeneratedCodes();
	}

	@Override
	public byte[] getCouponCodes(final String couponId, final String batchCode)
	{
		validateParameterNotNullStandardMessage("couponId", couponId); // NOSONAR
		validateParameterNotNullStandardMessage("batchCode", batchCode);

		final Collection<MediaModel> couponCodeBatches = getCouponCodeBatches(couponId);
		final MediaModel couponCodesMediaModel;
		if (isNotEmpty(couponCodeBatches))
		{
			couponCodesMediaModel = couponCodeBatches.stream().filter(b -> batchCode.equals(b.getCode())).findFirst()
					.orElseThrow(() -> new CouponCodesNotFoundWsException(format(NO_COUPON_CODE_FOUND_TEMPLATE, couponId, batchCode)));
			return getMediaService().getDataFromMedia(couponCodesMediaModel);
		}
		throw new CouponCodesNotFoundWsException(format(NO_COUPON_CODE_FOUND_TEMPLATE, couponId, batchCode));
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

	protected CouponDao getCouponDao()
	{
		return couponDao;
	}

	@Required
	public void setCouponDao(final CouponDao couponDao)
	{
		this.couponDao = couponDao;
	}

	protected MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
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
