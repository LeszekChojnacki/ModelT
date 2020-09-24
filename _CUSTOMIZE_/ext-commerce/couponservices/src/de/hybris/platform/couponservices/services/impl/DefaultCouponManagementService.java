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
package de.hybris.platform.couponservices.services.impl;

import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_ALREADY_REDEEMED_ERROR_CODE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_INVALID_ERROR_CODE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_GENERAL_ERROR_CODE;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.BooleanUtils.isTrue;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.CouponServiceException;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.CouponRedemptionModel;
import de.hybris.platform.couponservices.redemption.strategies.CouponRedemptionStrategy;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.CouponManagementService;
import de.hybris.platform.couponservices.strategies.FindCouponStrategy;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Default implementation of {@link CouponManagementService}.
 */
public class DefaultCouponManagementService implements CouponManagementService
{

	private ModelService modelService;

	private Map<String, CouponRedemptionStrategy> redemptionStrategyMap;

	private List<FindCouponStrategy> findCouponStrategiesList;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultCouponManagementService.class);

	private static final String COUPONCODENOTPROVIDED = "Coupon code is not provided";

	@Override
	public Optional<AbstractCouponModel> getCouponForCode(final String couponCode)
	{
		return getCouponForCode(couponCode, findCouponStrategy -> findCouponStrategy.findCouponForCouponCode(couponCode));
	}

	@Override
	public Optional<AbstractCouponModel> getValidatedCouponForCode(final String couponCode)
	{
		try
		{
			return getCouponForCode(couponCode,
					findCouponStrategy -> findCouponStrategy.findValidatedCouponForCouponCode(couponCode));
		}
		catch (final CouponServiceException ex)
		{
			LOG.debug(ex.getMessage(), ex);
		}
		return Optional.empty();

	}

	protected Optional<AbstractCouponModel> getCouponForCode(final String couponCode,
			final Function<FindCouponStrategy, Optional<AbstractCouponModel>> findCouponFunction)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(couponCode), COUPONCODENOTPROVIDED);
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(getFindCouponStrategiesList()),
				"Find Coupon Strategies is not provided");

		return getFindCouponStrategiesList().stream().map(findCouponFunction).filter(Optional::isPresent).map(Optional::get)
				.findFirst();
	}


	@Override
	public CouponResponse verifyCouponCode(final String couponCode, final AbstractOrderModel abstractOrder)
	{
		return validateCouponCode(couponCode, abstractOrder.getUser());
	}

	@Override
	public CouponResponse validateCouponCode(final String couponCode, final UserModel user)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(couponCode), COUPONCODENOTPROVIDED);

		final CouponResponse response = new CouponResponse();
		try
		{
			final Optional<AbstractCouponModel> optional = findValidatedCoupon(couponCode, user);
			if (optional.isPresent())
			{
				response.setSuccess(Boolean.TRUE);
				response.setCouponId(optional.get().getCouponId());
				return response;
			}
			response.setCouponId(couponCode);
			response.setSuccess(Boolean.FALSE);
			response.setMessage(COUPON_GENERAL_ERROR_CODE);
		}
		catch (final CouponServiceException ex)
		{

			LOG.debug(ex.getMessage(), ex);
			response.setCouponId(couponCode);
			response.setSuccess(Boolean.FALSE);
			response.setMessage(ex.getMessage());
		}
		return response;
	}

	protected Optional<AbstractCouponModel> findCoupon(final String couponCode, final AbstractOrderModel order)
	{
		return findValidatedCoupon(couponCode, order.getUser());
	}

	protected Optional<AbstractCouponModel> findValidatedCoupon(final String couponCode, final UserModel user)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(couponCode), COUPONCODENOTPROVIDED);

		for (final FindCouponStrategy findCouponStrategy : getFindCouponStrategiesList())
		{
			final Optional<AbstractCouponModel> optional = findCouponStrategy.findValidatedCouponForCouponCode(couponCode);
			if (optional.isPresent())
			{
				return checkCouponRedeemability(optional.get(), user, couponCode);
			}
		}
		throw new CouponServiceException(COUPON_CODE_INVALID_ERROR_CODE);
	}


	protected Optional<AbstractCouponModel> checkRedeemability(final AbstractCouponModel coupon, final AbstractOrderModel order,
			final String couponCode)
	{
		return checkCouponRedeemability(coupon, order.getUser(), couponCode);
	}

	protected Optional<AbstractCouponModel> checkCouponRedeemability(final AbstractCouponModel coupon, final UserModel user,
			final String couponCode)
	{
		validateParameterNotNullStandardMessage("coupon", coupon);

		final CouponRedemptionStrategy strategy = getRedemptionStrategyMap().get(coupon.getItemtype());
		if (isNull(strategy))
		{
			throw new IllegalArgumentException(
					"coupon " + coupon.getCouponId() + " of type:" + coupon.getItemtype() + " has no redemption strategy defined.");
		}
		if (strategy.isCouponRedeemable(coupon, user, couponCode))
		{
			return Optional.of(coupon);
		}
		throw new CouponServiceException(COUPON_CODE_ALREADY_REDEEMED_ERROR_CODE);
	}

	@Override
	public void releaseCouponCode(final String couponCode)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(couponCode), "Coupon code to be released is not provided");
		return;
	}

	@Override
	public boolean redeem(final String couponCode, final CartModel cart)
	{
		final CouponResponse response = verifyCouponCode(couponCode, cart);

		if (isTrue(response.getSuccess()))
		{
			return true;
		}
		throw new CouponServiceException(response.getMessage());
	}

	@Override
	public CouponResponse redeem(final String couponCode, final OrderModel order)
	{
		final CouponResponse response = verifyCouponCode(couponCode, order);
		if (isTrue(response.getSuccess()))
		{
			createCouponRedemption(couponCode, order);
		}

		return response;
	}


	protected void createCouponRedemption(final String couponCode, final OrderModel order)
	{
		final CouponRedemptionModel couponRedemption = getModelService().create(CouponRedemptionModel.class);
		findCoupon(couponCode, order).ifPresent(c -> couponRedemption.setCoupon(c));
		couponRedemption.setCouponCode(couponCode);
		couponRedemption.setOrder(order);
		couponRedemption.setUser(order.getUser());
		getModelService().save(couponRedemption);
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

	@Required
	public void setRedemptionStrategyMap(final Map<String, CouponRedemptionStrategy> redemptionStrategyMap)
	{
		this.redemptionStrategyMap = redemptionStrategyMap;
	}

	protected Map<String, CouponRedemptionStrategy> getRedemptionStrategyMap()
	{
		return redemptionStrategyMap;
	}

	protected List<FindCouponStrategy> getFindCouponStrategiesList()
	{
		return findCouponStrategiesList;
	}

	@Required
	public void setFindCouponStrategiesList(final List<FindCouponStrategy> findCouponStrategiesList)
	{
		this.findCouponStrategiesList = findCouponStrategiesList;
	}
}
