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
package de.hybris.platform.voucher;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.order.OrderManager;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.voucher.jalo.PromotionVoucher;
import de.hybris.platform.voucher.jalo.SerialVoucher;
import de.hybris.platform.voucher.jalo.Voucher;
import de.hybris.platform.voucher.model.PromotionVoucherModel;
import de.hybris.platform.voucher.model.SerialVoucherModel;
import de.hybris.platform.voucher.model.VoucherInvalidationModel;
import de.hybris.platform.voucher.model.VoucherModel;

import java.util.Collection;


/**
 * The service for vouchers.
 */
public interface VoucherService
{

	/**
	 * Transfers vouchers form cart to newly created order. Please make sure to call this always after
	 * {@link OrderManager#createOrder(de.hybris.platform.jalo.order.AbstractOrder)}.
	 *
	 * @param order
	 * 		the newly created order
	 * @param cart
	 * 		the cart which this order has been created from
	 */
	void afterOrderCreation(final OrderModel order, final CartModel cart);

	/**
	 * @return a Collection with all found {@link Voucher}s
	 */
	Collection<VoucherModel> getAllVouchers();

	/**
	 * Searches for all applied vouchers for the specific cart
	 *
	 * @param cart
	 * 		cart for which the vouchers are applied
	 * @return all applied voucher codes
	 */
	Collection<String> getAppliedVoucherCodes(final CartModel cart);

	Collection<String> getAppliedVoucherCodes(final OrderModel order); //NOSONAR

	/**
	 * Given the order get a list of applied vouchers
	 *
	 * @param order
	 * 		the given order
	 * @return a Collection with all applied {@link Voucher}s for this order
	 */
	Collection<DiscountModel> getAppliedVouchers(final AbstractOrderModel order);

	/**
	 * Get a voucher for a given voucher code
	 *
	 * @param voucherCode
	 * 		the code
	 * @return the first {@link Voucher} for the given code. This includes {@link PromotionVoucher} and
	 * {@link SerialVoucher}.
	 */
	VoucherModel getVoucher(final String voucherCode);

	/**
	 * given the voucher code get a collection of related promotion vouchers
	 *
	 * @param voucherCode
	 * 		the code
	 * @return a Collection of {@link PromotionVoucher}s
	 */
	Collection<PromotionVoucherModel> getPromotionVouchers(final String voucherCode);

	/**
	 * given the voucher code get a collection of related serial vouchers
	 *
	 * @param voucherCode
	 * 		the code
	 * @return a Collection of {@link SerialVoucher}s
	 */
	Collection<SerialVoucherModel> getSerialVouchers(final String voucherCode);

	/**
	 * For the given {@link CartModel} the given {@link Voucher} will be redeem.
	 *
	 * @param voucherCode
	 * 		code of the Voucher
	 * @param cart
	 * 		the cart
	 * @return true if the redemption was successful
	 * @throws JaloPriceFactoryException
	 * 		in the case of exception on Jalo layer
	 */
	boolean redeemVoucher(final String voucherCode, final CartModel cart) throws JaloPriceFactoryException;

	VoucherInvalidationModel redeemVoucher(final String voucherCode, final OrderModel order); //NOSONAR

	/**
	 * Creates {@link de.hybris.platform.voucher.jalo.VoucherInvalidation} after the voucher is redeemed for the order
	 *
	 * @param voucherCode
	 * 		voucher code to be redeemed
	 * @param order
	 * 		order for which the voucher will be redeemed
	 * @return true if the voucher has been redeemed for the given order
	 */
	boolean createVoucherInvalidation(String voucherCode, OrderModel order);

	/**
	 * Releases the voucher for the cart
	 *
	 * @param voucherCode
	 * 		voucher code to be released
	 * @param cart
	 * 		cart for which the voucher will be released
	 * @throws JaloPriceFactoryException
	 * 		in the case of exception on jalo layer
	 */
	void releaseVoucher(final String voucherCode, final CartModel cart) throws JaloPriceFactoryException;

	void releaseVoucher(final String voucherCode, final OrderModel order) throws ConsistencyCheckException; //NOSONAR

	VoucherInvalidationModel reserveVoucher(final String voucherCode, final OrderModel order); //NOSONAR

	void save(VoucherModel voucher); //NOSONAR

	void delete(VoucherModel voucher); //NOSONAR

}
