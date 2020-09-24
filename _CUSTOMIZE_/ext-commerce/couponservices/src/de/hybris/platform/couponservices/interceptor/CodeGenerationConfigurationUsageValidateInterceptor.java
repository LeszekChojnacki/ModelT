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
package de.hybris.platform.couponservices.interceptor;


import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;

import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * CodeGenerationConfigurationUsageValidateInterceptor validates whether {@link CodeGenerationConfigurationModel} is
 * assigned to at least once @{link {@link de.hybris.platform.couponservices.model.AbstractCouponModel}. If the check is
 * evaluated positively then configuration cannot be removed
 */
public class CodeGenerationConfigurationUsageValidateInterceptor implements RemoveInterceptor<CodeGenerationConfigurationModel>
{
	private static final String EXCEPTION_MESSAGE_KEY = "exception.codegenerationconfigurationusagevalidateinterceptor.cannot.delete";
	private L10NService l10NService;
	private CouponDao couponDao;

	@Override
	public void onRemove(final CodeGenerationConfigurationModel configuration, final InterceptorContext ctx) throws InterceptorException
	{
		final List<MultiCodeCouponModel> result = getCouponDao().findMultiCodeCouponsByCodeConfiguration(configuration);
		if (!result.isEmpty())
		{
			final String names = result.stream().map(coupon -> Optional.ofNullable(coupon.getName()).orElse(coupon.getCouponId())).collect(Collectors.joining(","));
			throw new InterceptorException(getL10NService().getLocalizedString(EXCEPTION_MESSAGE_KEY, new Object[]{names}));
		}
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

	protected L10NService getL10NService()
	{
		return l10NService;
	}
	@Required
	public void setL10NService(final L10NService l10NService)
	{
		this.l10NService = l10NService;
	}
}
