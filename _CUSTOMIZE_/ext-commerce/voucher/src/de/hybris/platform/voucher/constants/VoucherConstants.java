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
package de.hybris.platform.voucher.constants;

/**
 * Global class for all Voucher constants. <br />
 * This includes typecodes for new items, managers and JNDI names. <br />
 * 
 */
public final class VoucherConstants extends GeneratedVoucherConstants //NOSONAR
{
	public static final String CODES = "codes";
	public static final String VOUCHERCODES = "vouchercodes";

	/**
	 * @deprecated Since ages
	 */
	@Deprecated
	public static final String LICENCE_VOUCHER = "extension.voucher"; //NOSONAR

	private VoucherConstants()
	{
		//empty
	}
}
