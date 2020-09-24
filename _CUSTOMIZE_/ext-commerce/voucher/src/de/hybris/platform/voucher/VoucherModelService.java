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
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;
import de.hybris.platform.voucher.jalo.util.VoucherValue;
import de.hybris.platform.voucher.model.RestrictionModel;
import de.hybris.platform.voucher.model.VoucherInvalidationModel;
import de.hybris.platform.voucher.model.VoucherModel;

import java.security.NoSuchAlgorithmException;
import java.util.List;


/**
 * The vouchers are redeemed on the total value of an order.
 * <p />
 * The calculation of the discount is done on the total value of the applicable product's prices,
 * <p />
 * inclusive of VAT.
 * <p />
 * Non-applicable products in the order are not subject to the discount rules.
 * <p />
 * To discover to which products in the order the voucher is applicable one could assign
 * <p />
 * various restrictions to the voucher.
 *
 */
public interface VoucherModelService
{

	VoucherInvalidationModel createVoucherInvalidation(VoucherModel voucher, String voucherCode, OrderModel order); //NOSONAR

	/**
	 * Returns <tt>true</tt> if the specified voucher code is valid for this voucher.
	 *
	 * @param voucher
	 *           the voucher
	 * @param voucherCode
	 *           the voucher code to check validity of.
	 * @return <tt>true</tt> if the specified voucher code is valid for this voucher, <tt>false</tt> else.
	 */
	boolean checkVoucherCode(VoucherModel voucher, String voucherCode);

	/**
	 * Returns all positions or parts of positions of the specified abstract order that are eligible for this voucher.
	 *
	 * @param voucher
	 *           the voucher
	 * @param order
	 *           the abstract order to get eligible positions of.
	 * @return a <tt>VoucherEntrySet</tt> containing a <tt>VoucherEntry</tt> object for every position that is fully or
	 *         partly eligible for this voucher.
	 */
	VoucherEntrySet getApplicableEntries(VoucherModel voucher, AbstractOrderModel order);

	/**
	 * Returns a <tt>VoucherValue</tt> object representing the discount value of this voucher. If the voucher is
	 * applicable to the specified abstract order this value is calculated in consideration of the applicable value
	 * returned by <tt>getApplicableValue(AbstractOrder)</tt>.
	 *
	 * @param voucher
	 *           the voucher
	 * @param order
	 *           the abstract order to get discount value of.
	 * @return a <tt>VoucherValue</tt> representing the discount value of this voucher.
	 */
	VoucherValue getAppliedValue(VoucherModel voucher, AbstractOrderModel order);

	DiscountValue getDiscountValue(VoucherModel voucher, AbstractOrderModel order); //NOSONAR

	/**
	 * Returns all restrictions that are not fulfilled by the specified abstract order.
	 *
	 * @param voucher
	 *           the voucher
	 * @param order
	 *           the abstract order to return violated restrictions for.
	 * @return a <tt>List</tt> object containing all <tt>Restriction</tt> objects associated with this voucher that the
	 *         specified abstract order does not fulfill.
	 */
	List<RestrictionModel> getViolatedRestrictions(VoucherModel voucher, AbstractOrderModel order);

	/**
	 * Returns all restrictions that are not fulfilled by the specified product.
	 *
	 * @param voucher
	 *           the voucher
	 * @param product
	 *           the product to return violated restrictions for.
	 * @return a <tt>List</tt> object containing all <tt>Restriction</tt> objects associated with this voucher that the
	 *         specified product does not fulfill.
	 */
	List<RestrictionModel> getViolatedRestrictions(VoucherModel voucher, ProductModel product);

	List<String> getViolationMessages(VoucherModel voucher, AbstractOrderModel order); //NOSONAR

	List<String> getViolationMessages(VoucherModel voucher, ProductModel product); //NOSONAR

	VoucherValue getVoucherValue(VoucherModel voucher, AbstractOrderModel order); //NOSONAR

	/**
	 * Returns <tt>true</tt> if the specified abstract order is eligible for this voucher. More formally, returns
	 * <tt>true</tt> if the specified abstract order fulfills all restrictions associated with this voucher.
	 *
	 * @param voucher
	 *           the voucher
	 * @param order
	 *           the abstract order to check whether it is eligible for this voucher.
	 * @return <tt>true</tt> if the specified abstract order is eligible for this voucher, <tt>false</tt> else.
	 */
	boolean isApplicable(VoucherModel voucher, AbstractOrderModel order);

	/**
	 * Returns <tt>true</tt> if the specified product is eligible for this voucher. More formally, returns <tt>true</tt>
	 * if the specified product fulfills all restrictions associated with this voucher.
	 *
	 * @param voucher
	 *           the voucher
	 * @param product
	 *           the product to check whether it is eligible for this voucher.
	 * @return <tt>true</tt> if the specified product is eligible for this voucher, <tt>false</tt> else.
	 */
	boolean isApplicable(VoucherModel voucher, ProductModel product);

	boolean isReservable(VoucherModel voucher, String voucherCode, UserModel user); //NOSONAR

	boolean isReservable(VoucherModel voucher, String voucherCode, AbstractOrderModel order); //NOSONAR

	/**
	 * <p>
	 * <b>WARNING!</b> <br>
	 * If some methods for checking voucher availability are called before this method, all these methods have to be in
	 * one synchronize block! Suggested synchronization object is cart.
	 * </p>
	 */
	boolean redeem(VoucherModel voucher, String voucherCode, CartModel cart) throws JaloPriceFactoryException; //NOSONAR

	VoucherInvalidationModel redeem(VoucherModel voucher, String voucherCode, OrderModel order); //NOSONAR

	void release(VoucherModel voucher, String voucherCode, OrderModel order) throws ConsistencyCheckException; //NOSONAR

	void release(VoucherModel voucher, String voucherCode, CartModel cart) throws JaloPriceFactoryException; //NOSONAR

	VoucherInvalidationModel reserve(VoucherModel voucher, String voucherCode, OrderModel order); //NOSONAR

	String generateVoucherCode(VoucherModel voucher) throws NoSuchAlgorithmException; //NOSONAR

}
