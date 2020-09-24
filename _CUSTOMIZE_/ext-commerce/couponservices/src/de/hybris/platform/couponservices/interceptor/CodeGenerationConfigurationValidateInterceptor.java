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

import static org.apache.commons.lang.StringUtils.isEmpty;

import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.CodeGenerationConfigurationModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.model.ModelContextUtils;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * CodeGenerationConfigurationValidateInterceptor validates the {@code codeSeparator} and that {@code couponPartCount}
 * and {@code couponPartLength} are within the defined limits (both &gt; 0, product of both between 4 and 40 (more
 * formally: {@code 4 <= (codePartCount * codePartLength) <= 40} ).
 */
public class CodeGenerationConfigurationValidateInterceptor implements ValidateInterceptor<CodeGenerationConfigurationModel>
{

	private CouponCodeGenerationService couponCodeGenerationService;

	private CouponDao couponDao;

	@Override
	public void onValidate(final CodeGenerationConfigurationModel model, final InterceptorContext ctx) throws InterceptorException
	{

		if (isEmpty(model.getName()))
		{
			throw new InterceptorException("Name cannot be null or empty");
		}
		validateCodeSeperator(model);

		validateCoupon(model);

		// checks that a code generation configuration is only allowed to be changed if no coupon has it assigned yet.
		if (ctx.isModified(model) && !ctx.isNew(model) && checkModelAttributeValuesChanged(model, ctx))
		{
			final List<MultiCodeCouponModel> coupons = getCouponDao().findMultiCodeCouponsByCodeConfiguration(model);
			if (CollectionUtils.isNotEmpty(coupons)
					&& coupons.stream().anyMatch(coupon -> coupon.getCouponCodeNumber().intValue() > 0))
			{
				throw new InterceptorException("cannot modify coupon code configuration because " + coupons.size()
						+ " coupons are using this configuration already.");
			}

		}
	}

	/**
	 * @throws InterceptorException throws exception if code seperator is not valid
	 */
	protected void validateCodeSeperator(final CodeGenerationConfigurationModel model) throws InterceptorException
	{
		final String codeSeparator = model.getCodeSeparator();
		if (isEmpty(codeSeparator))
		{
			throw new InterceptorException("Code separator cannot be null or empty");
		}

		if (!getCouponCodeGenerationService().isValidCodeSeparator(codeSeparator))
		{
			throw new InterceptorException("Only one special character is allowed (no numbers or letters) ");
		}
	}

	/**
	 * @throws InterceptorException throws exception if coupon is not valid
	 */
	protected void validateCoupon(final CodeGenerationConfigurationModel model) throws InterceptorException
	{
		final int partCount = model.getCouponPartCount();
		if (partCount <= 0)
		{
			throw new InterceptorException("coupon part count must be greater than 0!");
		}

		final int partLength = model.getCouponPartLength();
		if (partLength <= 0)
		{
			throw new InterceptorException("coupon part length must be greater than 0!");
		}

		validateTotalLength(partCount, partLength);
	}

	/**
	 * @throws InterceptorException throws exception of total length of coupon is not valid
	 */
	protected void validateTotalLength(final int partCount, final int partLength) throws InterceptorException
	{
		final int totalLength = partCount * partLength;
		if (totalLength < 4)
		{
			throw new InterceptorException(
					"total length of coupon code (product of 'coupon part length' and 'coupon part count') must be at least 4!");
		}

		if (totalLength % 4 != 0)
		{
			throw new InterceptorException(
					"total length of coupon code (product of 'coupon part length' and 'coupon part count') must be a multiple of 4 (e.g. 4,8,12,16,20..)!");
		}
		if (totalLength > 40)
		{
			throw new InterceptorException(
					"total length of coupon code (product of 'coupon part length' and 'coupon part count') cannot be greater than 40!");
		}
	}

	protected boolean checkModelAttributeValuesChanged(final CodeGenerationConfigurationModel model, final InterceptorContext ctx)
	{
		return isCodeSeparattorChanged(model, ctx) || isCouponPartCountChanged(model, ctx) || isCouponPartLengthChanged(model, ctx);
	}

	protected boolean isCodeSeparattorChanged(final CodeGenerationConfigurationModel model, final InterceptorContext ctx)
	{
		return ctx.isModified(model, CodeGenerationConfigurationModel.CODESEPARATOR)
				&& !model.getCodeSeparator().equals(getPreviousValue(model, CodeGenerationConfigurationModel.CODESEPARATOR));
	}

	protected boolean isCouponPartCountChanged(final CodeGenerationConfigurationModel model, final InterceptorContext ctx)
	{
		final Integer previousValue = getPreviousValue(model, CodeGenerationConfigurationModel.COUPONPARTCOUNT);
		return ctx.isModified(model, CodeGenerationConfigurationModel.COUPONPARTCOUNT)
				&& model.getCouponPartCount() != previousValue.intValue();
	}

	protected boolean isCouponPartLengthChanged(final CodeGenerationConfigurationModel model, final InterceptorContext ctx)
	{
		final Integer previousValue = getPreviousValue(model, CodeGenerationConfigurationModel.COUPONPARTLENGTH);
		return ctx.isModified(model, CodeGenerationConfigurationModel.COUPONPARTLENGTH) && model
				.getCouponPartLength() != previousValue.intValue();
	}

	protected <T extends Object> T getPreviousValue(final CodeGenerationConfigurationModel model, final String attributeName)
	{
		return ModelContextUtils.getItemModelContext(model).getOriginalValue(attributeName);
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


}
