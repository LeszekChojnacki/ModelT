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
package de.hybris.platform.voucher.jalo;


/**
 * An instance of this class will be created by the <code>VoucherManager</code>
 * 
 * @see VoucherManager#reserveVoucher(String, de.hybris.platform.jalo.order.Order) to create a VoucherInvalidation and
 *      so reserve a voucher code for an order and
 * @see VoucherManager#redeemVoucher(String, de.hybris.platform.jalo.order.Order) to finally invalidate the voucher code
 *      later.
 * 
 */
public class VoucherInvalidation extends GeneratedVoucherInvalidation
{
	public static final String STATUS_CREATED = "created";
	public static final String STATUS_CONFIRMED = "confirmed";
}
