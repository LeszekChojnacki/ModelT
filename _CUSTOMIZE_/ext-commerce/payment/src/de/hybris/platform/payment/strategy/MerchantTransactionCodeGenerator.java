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
package de.hybris.platform.payment.strategy;

import de.hybris.platform.core.model.order.OrderModel;


/**
 * This interface should be used to provide the merchant transaction code based on the specified order.
 */
public interface MerchantTransactionCodeGenerator
{
	/**
	 * 
	 * @param order
	 *           the order
	 * @return the merchant transaction code
	 */
	String getCode(OrderModel order);
}
