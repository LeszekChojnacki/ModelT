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
package de.hybris.platform.couponfacades.converters.populator;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commercefacades.voucher.data.VoucherData;
import de.hybris.platform.converters.Populator;


/**
 * Populator implementation for {@link java.lang.String} as source and
 * {@link de.hybris.platform.commercefacades.voucher.data.VoucherData} as target type.
 *
 */
public class CouponCodeToVoucherDataPopulator implements Populator<String, VoucherData>
{
	@Override
	public void populate(final String source, final VoucherData target)
	{
		validateParameterNotNullStandardMessage("source", source);
		validateParameterNotNullStandardMessage("target", target);

		target.setCode(source);
	}
}
