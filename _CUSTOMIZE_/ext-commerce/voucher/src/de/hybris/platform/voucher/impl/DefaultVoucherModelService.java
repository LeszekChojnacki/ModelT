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
package de.hybris.platform.voucher.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.voucher.VoucherModelService;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;
import de.hybris.platform.voucher.jalo.util.VoucherValue;
import de.hybris.platform.voucher.model.RestrictionModel;
import de.hybris.platform.voucher.model.VoucherInvalidationModel;
import de.hybris.platform.voucher.model.VoucherModel;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class DefaultVoucherModelService extends AbstractVoucherService implements VoucherModelService //NOSONAR
{

	@Override
	public boolean checkVoucherCode(final VoucherModel voucher, final String voucherCode)
	{
		return getVoucher(voucher).checkVoucherCode(voucherCode);
	}

	@Override
	public VoucherInvalidationModel createVoucherInvalidation(final VoucherModel voucher, final String voucherCode,
			final OrderModel order)
	{
		return getModelService().get(getVoucher(voucher).createVoucherInvalidation(voucherCode, getOrder(order)));
	}

	@Override
	public String generateVoucherCode(final VoucherModel voucher) throws NoSuchAlgorithmException
	{
		return getVoucher(voucher).generateVoucherCode();
	}

	@Override
	public VoucherEntrySet getApplicableEntries(final VoucherModel voucher, final AbstractOrderModel order)
	{
		return getVoucher(voucher).getApplicableEntries(getAbstractOrder(order));
	}

	@Override
	public VoucherValue getAppliedValue(final VoucherModel voucher, final AbstractOrderModel order)
	{
		return getVoucher(voucher).getAppliedValue(getAbstractOrder(order));
	}

	@Override
	public DiscountValue getDiscountValue(final VoucherModel voucher, final AbstractOrderModel order)
	{
		return getVoucher(voucher).getDiscountValue(getAbstractOrder(order));
	}

	@Override
	public List<RestrictionModel> getViolatedRestrictions(final VoucherModel voucher, final AbstractOrderModel order)
	{
		return getModelService().getAll(getVoucher(voucher).getViolatedRestrictions(getAbstractOrder(order)),
				new ArrayList<RestrictionModel>());
	}

	@Override
	public List<RestrictionModel> getViolatedRestrictions(final VoucherModel voucher, final ProductModel product)
	{
		return getModelService().getAll(getVoucher(voucher).getViolatedRestrictions(getProduct(product)),
				new ArrayList<RestrictionModel>());
	}

	@Override
	public List<String> getViolationMessages(final VoucherModel voucher, final AbstractOrderModel order)
	{
		return getVoucher(voucher).getViolationMessages(getAbstractOrder(order));
	}

	@Override
	public List<String> getViolationMessages(final VoucherModel voucher, final ProductModel product)
	{
		return getVoucher(voucher).getViolationMessages(getProduct(product));
	}

	@Override
	public VoucherValue getVoucherValue(final VoucherModel voucher, final AbstractOrderModel order)
	{
		return getVoucher(voucher).getVoucherValue(getAbstractOrder(order));
	}

	@Override
	public boolean isApplicable(final VoucherModel voucher, final AbstractOrderModel order)
	{
		return getVoucher(voucher).isApplicable(getAbstractOrder(order));
	}

	@Override
	public boolean isApplicable(final VoucherModel voucher, final ProductModel product)
	{
		return getVoucher(voucher).isApplicable(getProduct(product));
	}

	@Override
	public boolean isReservable(final VoucherModel voucher, final String voucherCode, final UserModel user)
	{
		return getVoucher(voucher).isReservable(voucherCode, getUser(user));
	}

	@Override
	public boolean isReservable(final VoucherModel voucher, final String voucherCode, final AbstractOrderModel order)
	{
		return getVoucher(voucher).isReservable(voucherCode, getAbstractOrder(order));
	}

	@Override
	public boolean redeem(final VoucherModel voucher, final String voucherCode, final CartModel cart)
			throws JaloPriceFactoryException
	{
		return getVoucher(voucher).redeem(voucherCode, getCart(cart));
	}

	@Override
	public VoucherInvalidationModel redeem(final VoucherModel voucher, final String voucherCode, final OrderModel order)
	{
		return getModelService().get(getVoucher(voucher).redeem(voucherCode, getOrder(order)));
	}

	@Override
	public void release(final VoucherModel voucher, final String voucherCode, final OrderModel order)
			throws ConsistencyCheckException
	{
		getVoucher(voucher).release(voucherCode, getOrder(order));
	}

	@Override
	public void release(final VoucherModel voucher, final String voucherCode, final CartModel cart)
			throws JaloPriceFactoryException
	{
		getVoucher(voucher).release(voucherCode, getCart(cart));
	}

	@Override
	public VoucherInvalidationModel reserve(final VoucherModel voucher, final String voucherCode, final OrderModel order)
	{
		return getModelService().get(getVoucher(voucher).reserve(voucherCode, getOrder(order)));
	}

}
