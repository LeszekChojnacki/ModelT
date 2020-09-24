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
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.voucher.VoucherRestrictionService;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;
import de.hybris.platform.voucher.model.RestrictionModel;


public class DefaultVoucherRestrictionService extends AbstractVoucherService implements VoucherRestrictionService //NOSONAR
{

	@Override
	public VoucherEntrySet getApplicableEntries(final RestrictionModel restriction, final AbstractOrderModel order)
	{
		return getRestriction(restriction).getApplicableEntries(getAbstractOrder(order));
	}

	@Override
	public boolean isFulfilled(final RestrictionModel restriction, final AbstractOrderModel order)
	{
		return getRestriction(restriction).isFulfilled(getAbstractOrder(order));
	}

	@Override
	public boolean isFulfilled(final RestrictionModel restriction, final ProductModel product)
	{
		return getRestriction(restriction).isFulfilled(getProduct(product));
	}

}
