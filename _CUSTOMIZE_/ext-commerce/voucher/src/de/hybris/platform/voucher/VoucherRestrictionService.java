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
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;
import de.hybris.platform.voucher.model.RestrictionModel;


/**
 * The [y] hybris Platform <i>voucher extension </i> enables users to assign a set of restrictions to a voucher for
 * confining the usage of it. A combination of none, one, or many of the restrictions is possible. Developers can
 * implement other restrictions in addition to those already there.
 * <p />
 * In an order containing multiple items, (percentage) discounts will apply only to the products that match all of the
 * criteria given by its assigned restrictions. The other products in the order are not discounted. Vouchers provide an
 * interface for getting the eligible entries within an given order.
 *
 */
public interface VoucherRestrictionService
{

	/**
	 * Returns <tt>true</tt> if the specified abstract order is not null and fulfills this restriction.
	 *
	 * @param restriction
	 *           the restriction
	 * @param order
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order is not null and fulfills this restriction, <tt>false</tt>
	 *         else.
	 */
	boolean isFulfilled(final RestrictionModel restriction, final AbstractOrderModel order);

	VoucherEntrySet getApplicableEntries(final RestrictionModel restriction, final AbstractOrderModel order); //NOSONAR

	/**
	 * Returns <tt>true</tt> if the specified product is not null and fulfills this restriction.
	 *
	 * @param restriction
	 *           the restriction
	 * @param product
	 *           the product to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified product is not null and fulfills this restriction, <tt>false</tt> else.
	 */
	boolean isFulfilled(final RestrictionModel restriction, final ProductModel product);

}
