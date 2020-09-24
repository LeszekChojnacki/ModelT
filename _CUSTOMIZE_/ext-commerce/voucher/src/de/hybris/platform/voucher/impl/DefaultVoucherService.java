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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.voucher.VoucherService;
import de.hybris.platform.voucher.jalo.Voucher;
import de.hybris.platform.voucher.jalo.VoucherInvalidation;
import de.hybris.platform.voucher.jalo.VoucherManager;
import de.hybris.platform.voucher.model.PromotionVoucherModel;
import de.hybris.platform.voucher.model.SerialVoucherModel;
import de.hybris.platform.voucher.model.VoucherInvalidationModel;
import de.hybris.platform.voucher.model.VoucherModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;


public class DefaultVoucherService extends AbstractVoucherService implements VoucherService //NOSONAR
{

	@Override
	public void afterOrderCreation(final OrderModel order, final CartModel cart)
	{
		VoucherManager.getInstance().afterOrderCreation(getOrder(order), getCart(cart));
	}

	@Override
	public boolean createVoucherInvalidation(final String voucherCode, final OrderModel order)
	{
		final Voucher voucher = VoucherManager.getInstance().getVoucher(voucherCode);
		final Order jaloOrder = getModelService().getSource(order);
		final VoucherInvalidation vi = voucher.createVoucherInvalidation(voucherCode, jaloOrder);
		if (vi != null)
		{
			vi.setStatus(VoucherInvalidation.STATUS_CONFIRMED);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public Collection<VoucherModel> getAllVouchers()
	{
		return getModelService().getAll(VoucherManager.getInstance().getAllVouchers(), new ArrayList<VoucherModel>());
	}

	@Override
	public Collection<String> getAppliedVoucherCodes(final CartModel cart)
	{
		return VoucherManager.getInstance().getAppliedVoucherCodes(getCart(cart));
	}

	@Override
	public Collection<String> getAppliedVoucherCodes(final OrderModel order)
	{
		return VoucherManager.getInstance().getAppliedVoucherCodes(getOrder(order));
	}

	@Override
	public Collection<DiscountModel> getAppliedVouchers(final AbstractOrderModel order)
	{
		return getModelService().getAll(VoucherManager.getInstance().getAppliedVouchers(getAbstractOrder(order)),
				new ArrayList<DiscountModel>());
	}

	@Override
	public Collection<PromotionVoucherModel> getPromotionVouchers(final String voucherCode)
	{
		return getModelService().getAll(VoucherManager.getInstance().getPromotionVouchers(voucherCode),
				new ArrayList<PromotionVoucherModel>());
	}

	@Override
	public Collection<SerialVoucherModel> getSerialVouchers(final String voucherCode)
	{
		return getModelService().getAll(VoucherManager.getInstance().getSerialVouchers(voucherCode),
				new ArrayList<SerialVoucherModel>());
	}

	@Override
	public VoucherModel getVoucher(final String voucherCode)
	{
		final Voucher voucher = VoucherManager.getInstance().getVoucher(voucherCode);
		if (voucher == null)
		{
			return null;
		}
		else
		{
			return getModelService().get(voucher);
		}
	}

	@Override
	public boolean redeemVoucher(final String voucherCode, final CartModel cart) throws JaloPriceFactoryException
	{
		saveIfModified(cart);
		final boolean changed = VoucherManager.getInstance().redeemVoucher(voucherCode, getCart(cart));
		if (changed)
		{
			refresh(cart);
		}
		return changed;
	}

	@Override
	public VoucherInvalidationModel redeemVoucher(final String voucherCode, final OrderModel order)
	{
		saveIfModified(order);
		final VoucherInvalidation voucherInvalidation = VoucherManager.getInstance().redeemVoucher(voucherCode, getOrder(order));
		if (voucherInvalidation == null)
		{
			return null;
		}
		else
		{
			refresh(order);
			return getModelService().get(voucherInvalidation);
		}
	}

	@Override
	public void releaseVoucher(final String voucherCode, final CartModel cart) throws JaloPriceFactoryException
	{
		saveIfModified(cart);
		VoucherManager.getInstance().releaseVoucher(voucherCode, getCart(cart));
		refresh(cart);
	}

	@Override
	public void releaseVoucher(final String voucherCode, final OrderModel order) throws ConsistencyCheckException
	{
		saveIfModified(order);
		VoucherManager.getInstance().releaseVoucher(voucherCode, getOrder(order));
		refresh(order);
	}

	@Override
	public VoucherInvalidationModel reserveVoucher(final String voucherCode, final OrderModel order)
	{
		saveIfModified(order);
		final VoucherInvalidation reserveVoucher = VoucherManager.getInstance().reserveVoucher(voucherCode, getOrder(order));
		if (reserveVoucher == null)
		{
			return null;
		}
		else
		{
			refresh(order);
			return getModelService().get(reserveVoucher);
		}
	}

	@Override
	public void delete(final VoucherModel voucher)
	{
		getModelService().remove(voucher);
	}

	@Override
	public void save(final VoucherModel voucher)
	{
		getModelService().save(voucher);
	}

	protected void saveIfModified(final AbstractOrderModel order)
	{
		if (getModelService().isModified(order))
		{
			getModelService().save(order);
		}
		if (order.getEntries() != null)
		{
			final Collection<AbstractOrderEntryModel> orderEntries = order.getEntries().stream()
					.filter(oe -> getModelService().isModified(oe)).collect(Collectors.toList());
			if (!orderEntries.isEmpty())
			{
				getModelService().saveAll(orderEntries);
			}
		}

	}

	protected void refresh(final AbstractOrderModel order)
	{
		getModelService().refresh(order);
	}
}
