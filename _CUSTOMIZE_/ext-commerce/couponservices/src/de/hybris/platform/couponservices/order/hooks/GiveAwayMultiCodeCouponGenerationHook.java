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
package de.hybris.platform.couponservices.order.hooks;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commerceservices.order.hook.CommercePlaceOrderMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeGenerationException;
import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.dao.RuleBasedCouponActionDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.model.RuleBasedAddCouponActionModel;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link CommercePlaceOrderMethodHook}
 */
public class GiveAwayMultiCodeCouponGenerationHook implements CommercePlaceOrderMethodHook
{
	private CouponDao couponDao;
	private RuleBasedCouponActionDao ruleBasedCouponActionDao;
	private CouponCodeGenerationService couponCodeGenerationService;
	private ModelService modelService;

	private static final Logger LOG = LoggerFactory.getLogger(GiveAwayMultiCodeCouponGenerationHook.class);

	@Override
	public void beforeSubmitOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult result)
			throws InvalidCartException
	{
		validateParameterNotNullStandardMessage("order", result.getOrder());

		final List<RuleBasedAddCouponActionModel> couponActionList = getRuleBasedCouponActionDao()
				.findRuleBasedCouponActionByOrder(result.getOrder());

		for (final RuleBasedAddCouponActionModel couponAction : couponActionList)
		{
			if (StringUtils.isNotEmpty(couponAction.getCouponId()))
			{
				generateGiveAwayMultiCodeCoupon(couponAction);
			}
			else
			{
				LOG.warn("Cannot generate Give Away Multi Code Coupon as coupon id is empty");
			}
		}
	}

	protected void generateGiveAwayMultiCodeCoupon(final RuleBasedAddCouponActionModel couponAction)
	{
		try
		{
			final AbstractCouponModel coupon = getCouponDao().findCouponById(couponAction.getCouponId());
			if (coupon instanceof MultiCodeCouponModel)
			{
				generateGiveAwayMultiCodeCoupon((MultiCodeCouponModel) coupon, couponAction);
			}
		}
		catch (ModelNotFoundException | AmbiguousIdentifierException ex)
		{
			LOG.error("Cannot generate Give Away Multi Code Coupon for coupon id: {}, not able to find the coupon object",
					couponAction.getCouponId(), ex);

			setMultiCodeCouponToAction(couponAction, StringUtils.EMPTY);
		}
	}

	protected void generateGiveAwayMultiCodeCoupon(final MultiCodeCouponModel coupon,
			final RuleBasedAddCouponActionModel couponAction)
	{
		try
		{
			final String multiCodeCouponCode = getCouponCodeGenerationService().generateCouponCode(coupon);
			setMultiCodeCouponToAction(couponAction, multiCodeCouponCode);
			getModelService().save(coupon);
		}
		catch (final CouponCodeGenerationException ex)
		{
			LOG.error("Cannot generate Give Away Multi Code Coupon due to Coupon Code Generation Exception", ex);
			setMultiCodeCouponToAction(couponAction, StringUtils.EMPTY);
		}
	}

	protected void setMultiCodeCouponToAction(final RuleBasedAddCouponActionModel couponAction, final String multiCodeCouponCode)
	{
		couponAction.setCouponCode(multiCodeCouponCode);
		getModelService().save(couponAction);
	}

	@Override
	public void afterPlaceOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult orderModel)
			throws InvalidCartException
	{
		// nothing to do here

	}

	@Override
	public void beforePlaceOrder(final CommerceCheckoutParameter parameter) throws InvalidCartException
	{
		// nothing to do here

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

	protected CouponCodeGenerationService getCouponCodeGenerationService()
	{
		return couponCodeGenerationService;
	}

	@Required
	public void setCouponCodeGenerationService(final CouponCodeGenerationService couponCodeGenerationService)
	{
		this.couponCodeGenerationService = couponCodeGenerationService;
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

	protected RuleBasedCouponActionDao getRuleBasedCouponActionDao()
	{
		return ruleBasedCouponActionDao;
	}

	@Required
	public void setRuleBasedCouponActionDao(final RuleBasedCouponActionDao ruleBasedCouponActionDao)
	{
		this.ruleBasedCouponActionDao = ruleBasedCouponActionDao;
	}
}
