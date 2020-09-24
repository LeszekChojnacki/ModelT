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

import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_ALREADY_EXISTS_ERROR_CODE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_ORDER_RECALCULATION_ERROR_CODE;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.BooleanUtils.isTrue;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.CouponServiceException;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.couponservices.services.CouponManagementService;
import de.hybris.platform.couponservices.services.CouponService;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotions.PromotionsService;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Default CouponService implementation
 */
public class DefaultCouponService implements CouponService
{
	private CouponManagementService couponManagementService;
	private CalculationService calculationService;
	private PromotionsService promotionsService;
	private BaseSiteService baseSiteService;
	private ModelService modelService;
	private CouponCodeGenerationService couponCodeGenerationService;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultCouponService.class);
	private static final String COUPONCODECANNOTBENULL = "Coupon code cannot be NULL here";


	@Override
	public CouponResponse verifyCouponCode(final String couponCode, final AbstractOrderModel order)
	{
		validateParameterNotNullStandardMessage("couponCode", couponCode); // NOSONAR
		validateParameterNotNullStandardMessage("order", order); // NOSONAR

		return getCouponManagementService().verifyCouponCode(couponCode, order);
	}

	@Override
	public CouponResponse validateCouponCode(final String couponCode, final UserModel user)
	{
		validateParameterNotNullStandardMessage("couponCode", couponCode); // NOSONAR

		return getCouponManagementService().validateCouponCode(couponCode, user);
	}

	@Override
	public CouponResponse redeemCoupon(final String couponCode, final CartModel cart)
	{
		validateParameterNotNullStandardMessage("couponCode", couponCode); // NOSONAR
		validateParameterNotNullStandardMessage("cart", cart); // NOSONAR

		final String clearedCouponCode = clearCouponCode(couponCode);
		final CouponResponse response = assertCouponCodeInOrder(clearedCouponCode, cart);

		if (isTrue(response.getSuccess()))
		{
			redeemCouponCode(cart, clearedCouponCode, response);
		}
		return response;
	}

	protected void redeemCouponCode(final CartModel cart, final String clearedCouponCode, final CouponResponse response)
	{
		try
		{
			if (getCouponManagementService().redeem(clearedCouponCode, cart))
			{
				final Set<String> codes = new LinkedHashSet<>();
				if (isNotEmpty(cart.getAppliedCouponCodes()))
				{
					codes.addAll(cart.getAppliedCouponCodes());
				}
				codes.add(clearedCouponCode);
				cart.setAppliedCouponCodes(codes);
				getModelService().save(cart);
				recalculateOrder(cart);
			}

		}
		catch (final CouponServiceException ex)
		{
			LOG.debug(ex.getMessage(), ex);
			response.setSuccess(Boolean.FALSE);
			response.setMessage(ex.getMessage());
		}
	}

	@Override
	public CouponResponse redeemCoupon(final String couponCode, final OrderModel order)
	{
		validateParameterNotNullStandardMessage("couponCode", couponCode); // NOSONAR
		validateParameterNotNullStandardMessage("order", order); // NOSONAR

		final String clearedCouponCode = clearCouponCode(couponCode);
		if (!containsCouponCode(clearedCouponCode, order))
		{
			throw new CouponServiceException(
					"Cannot apply couponCode '" + couponCode + "'. It is already applied to order " + order.getCode());
		}
		return getCouponManagementService().redeem(clearedCouponCode, order);
	}

	protected String clearCouponCode(final String couponCode)
	{
		return couponCode.trim();
	}

	protected CouponResponse assertCouponCodeInOrder(final String couponCode, final AbstractOrderModel order)
	{
		final CouponResponse response = new CouponResponse();
		response.setSuccess(Boolean.TRUE);
		if (containsCouponCode(couponCode, order))
		{
			response.setMessage(COUPON_CODE_ALREADY_EXISTS_ERROR_CODE);
			response.setSuccess(Boolean.FALSE);
		}
		return response;
	}

	/**
	 * returns true if the given couponCode is part of the given order
	 *
	 * @param couponCode
	 *           the couponCode to check
	 * @param order
	 *           the abstract order to check
	 */
	protected boolean containsCouponCode(final String couponCode, final AbstractOrderModel order)
	{
		if (isNotEmpty(order.getAppliedCouponCodes()))
		{
			final Optional<AbstractCouponModel> couponModel = getCouponManagementService().getCouponForCode(couponCode);
			if (couponModel.isPresent())
			{
				return order.getAppliedCouponCodes().stream().anyMatch(checkMatch(couponModel.get(), couponCode));
			}
		}
		return false;
	}

	protected Predicate<String> checkMatch(final AbstractCouponModel coupon, final String couponCode)
	{
		if (coupon instanceof MultiCodeCouponModel)
		{
			return appliedCouponCode -> {
				final String couponPrefix = getCouponCodeGenerationService().extractCouponPrefix(couponCode);
				return couponPrefix != null
						&& couponPrefix.equals(getCouponCodeGenerationService().extractCouponPrefix(appliedCouponCode));
			};
		}
		return appliedCouponCode -> appliedCouponCode.equals(couponCode);
	}

	/**
	 * Recalculates the given order and updates the promotions. (calls
	 * {@link CalculationService#calculate(AbstractOrderModel)} and
	 * {@link PromotionsService#updatePromotions(Collection, AbstractOrderModel)}
	 *
	 * @throws CouponServiceException
	 *            if any errors happen during recalculation
	 */
	protected void recalculateOrder(final AbstractOrderModel order)
	{
		try
		{
			getCalculationService().calculate(order);
			getPromotionsService().updatePromotions(getPromotionGroups(), order);
		}
		catch (final CalculationException e)
		{
			LOG.error("Error re-calculating the order", e);
			throw new CouponServiceException(COUPON_ORDER_RECALCULATION_ERROR_CODE);
		}
	}

	@Override
	public Optional<AbstractCouponModel> getCouponForCode(final String couponCode)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(couponCode), COUPONCODECANNOTBENULL);
		return getCouponManagementService().getCouponForCode(couponCode);
	}

	@Override
	public Optional<AbstractCouponModel> getValidatedCouponForCode(final String couponCode)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(couponCode), COUPONCODECANNOTBENULL);
		return getCouponManagementService().getValidatedCouponForCode(couponCode);
	}

	@Override
	public void releaseCouponCode(final String couponCode, final AbstractOrderModel order)
	{
		validateParameterNotNullStandardMessage("couponCode", couponCode); // NOSONAR
		validateParameterNotNullStandardMessage("order", order); // NOSONAR

		getCouponManagementService().releaseCouponCode(couponCode);
		removeCouponAndTriggerCalculation(couponCode, order);
	}

	protected void removeCouponAndTriggerCalculation(final String couponCode, final AbstractOrderModel order)
	{
		final Collection<String> couponCodes = order.getAppliedCouponCodes();
		if (CollectionUtils.isNotEmpty(couponCodes) && containsCouponCode(couponCode, order))
		{
			final Set<String> couponCodesFiltered = couponCodes.stream().filter(c -> !c.equals(couponCode))
					.collect(Collectors.toSet());
			order.setAppliedCouponCodes(couponCodesFiltered);
			//force recalculation and save order
			order.setCalculated(Boolean.FALSE);
			getModelService().save(order);
			recalculateOrder(order);
		}
	}

	protected Collection<PromotionGroupModel> getPromotionGroups()
	{
		final Collection<PromotionGroupModel> promotionGroupModels = new ArrayList<>();
		final BaseSiteModel currentBaseSite = getBaseSiteService().getCurrentBaseSite();
		if (nonNull(currentBaseSite) && nonNull(currentBaseSite.getDefaultPromotionGroup()))
		{
			promotionGroupModels.add(currentBaseSite.getDefaultPromotionGroup());
		}
		return promotionGroupModels;
	}

	protected CouponManagementService getCouponManagementService()
	{
		return couponManagementService;
	}

	@Required
	public void setCouponManagementService(final CouponManagementService couponManagementService)
	{
		this.couponManagementService = couponManagementService;
	}

	protected CalculationService getCalculationService()
	{
		return calculationService;
	}

	@Required
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}

	protected PromotionsService getPromotionsService()
	{
		return promotionsService;
	}

	@Required
	public void setPromotionsService(final PromotionsService promotionsService)
	{
		this.promotionsService = promotionsService;
	}

	protected BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	@Required
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
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

	protected CouponCodeGenerationService getCouponCodeGenerationService()
	{
		return couponCodeGenerationService;
	}

	@Required
	public void setCouponCodeGenerationService(final CouponCodeGenerationService couponCodeGenerationService)
	{
		this.couponCodeGenerationService = couponCodeGenerationService;
	}
}
