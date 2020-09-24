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
package de.hybris.platform.couponfacades.facades.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.BooleanUtils.isTrue;

import de.hybris.platform.commercefacades.voucher.VoucherFacade;
import de.hybris.platform.commercefacades.voucher.data.VoucherData;
import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.converters.impl.AbstractPopulatingConverter;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.couponfacades.CouponFacadeIllegalStateException;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.CouponService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of VoucherFacade that uses CouponService.
 */
public class DefaultCouponFacade implements VoucherFacade
{
	private CouponService couponService;
	private CartService cartService;
	private AbstractPopulatingConverter<String, VoucherData> couponCodeModelConverter;
	private static final String CARTNOTFOUND = "No cart was found in session";

	private Converter<AbstractCouponModel, VoucherData> couponModelConverter;

	@Override
	public boolean checkVoucherCode(final String voucherCode)
	{
		validateParameterNotNullStandardMessage("coupon code", voucherCode); // NOSONAR

		final CouponResponse couponResponse = applyIfCartExists(c -> getCouponService().verifyCouponCode(voucherCode, c));
		return isTrue(couponResponse.getSuccess());
	}

	@Override
	public VoucherData getVoucher(final String voucherCode) throws VoucherOperationException
	{
		validateParameterNotNullStandardMessage("voucher code", voucherCode);

		return getCouponService().getValidatedCouponForCode(voucherCode).map(getCouponModelConverter()::convert).map(coupon -> {
			coupon.setVoucherCode(voucherCode);
			return coupon;
		}).orElseThrow(() -> new VoucherOperationException("cannot create voucher data for given code:" + voucherCode));
	}

	@Override
	public void applyVoucher(final String voucherCode) throws VoucherOperationException
	{
		validateParameterNotNullStandardMessage("coupon code", voucherCode);

		final CouponResponse couponResponse = applyIfCartExists(voucherCode, getCouponService()::redeemCoupon);
		if (BooleanUtils.isNotTrue(couponResponse.getSuccess()))
		{
			throw new VoucherOperationException(couponResponse.getMessage());
		}
	}

	@Override
	public void releaseVoucher(final String voucherCode) throws VoucherOperationException
	{
		validateParameterNotNullStandardMessage("coupon code", voucherCode);

		acceptIfCartExists(voucherCode, getCouponService()::releaseCouponCode);
	}

	@Override
	public List<VoucherData> getVouchersForCart()
	{
		return applyIfCartExists(this::getCouponsForOrder);
	}

	protected List<VoucherData> getCouponsForOrder(final AbstractOrderModel order)
	{
		final Collection<String> couponCodesForOrder = order.getAppliedCouponCodes();
		if (isNotEmpty(couponCodesForOrder))
		{
			return couponCodesForOrder.stream().map(getCouponCodeModelConverter()::convert).collect(toList());
		}
		return Collections.emptyList();
	}

	protected void acceptIfCartExists(final String code, final BiConsumer<String, AbstractOrderModel> orderConsumer)
			throws VoucherOperationException
	{

		final CartModel cart = getCartService().getSessionCart();
		if (nonNull(cart))
		{
			orderConsumer.accept(code, cart);
		}
		else
		{
			throw new VoucherOperationException(CARTNOTFOUND);
		}
	}

	protected <R> R applyIfCartExists(final String code, final BiFunction<String, CartModel, R> orderConsumer)
			throws VoucherOperationException
	{

		final CartModel cart = getCartService().getSessionCart();
		if (nonNull(cart))
		{
			return orderConsumer.apply(code, cart);
		}
		else
		{
			throw new VoucherOperationException(CARTNOTFOUND);
		}
	}

	protected <R> R applyIfCartExists(final Function<AbstractOrderModel, R> orderFunction)
	{
		final CartModel cart = getCartService().getSessionCart();
		if (nonNull(cart))
		{
			return orderFunction.apply(cart);
		}
		else
		{
			throw new CouponFacadeIllegalStateException(CARTNOTFOUND);
		}
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

	protected CartService getCartService()
	{
		return cartService;
	}

	@Required
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}

	protected AbstractPopulatingConverter<String, VoucherData> getCouponCodeModelConverter()
	{
		return couponCodeModelConverter;
	}

	@Required
	public void setCouponCodeModelConverter(final AbstractPopulatingConverter<String, VoucherData> couponCodeModelConverter)
	{
		this.couponCodeModelConverter = couponCodeModelConverter;
	}

	protected Converter<AbstractCouponModel, VoucherData> getCouponModelConverter()
	{
		return couponModelConverter;
	}

	@Required
	public void setCouponModelConverter(final Converter<AbstractCouponModel, VoucherData> couponModelConverter)
	{
		this.couponModelConverter = couponModelConverter;
	}
}
