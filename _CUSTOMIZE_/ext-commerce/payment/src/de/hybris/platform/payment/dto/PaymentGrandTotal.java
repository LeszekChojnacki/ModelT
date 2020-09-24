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
package de.hybris.platform.payment.dto;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * @deprecated since 6.4 not needed
 *
 *             many information
 */
@Deprecated
public interface PaymentGrandTotal
{
	Currency getCurrency();

	BigDecimal getTotalAmount();
}
