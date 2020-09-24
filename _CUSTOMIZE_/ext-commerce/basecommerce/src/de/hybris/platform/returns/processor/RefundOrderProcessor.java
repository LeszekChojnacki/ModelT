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
package de.hybris.platform.returns.processor;

import de.hybris.platform.core.model.order.OrderModel;



/**
 * By implementing you have the handle your final Replacement Order processing. For example for handling consignment
 * creation
 * 
 */
public interface RefundOrderProcessor
{
	/**
	 * Here you have the chance to handle your final Refund order processing. For example for handling consignment
	 * creation or final payment
	 * 
	 * @param order
	 *           the order to be process
	 */
	void process(final OrderModel order);
}
