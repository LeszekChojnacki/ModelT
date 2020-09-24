/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.onhold.service;

import de.hybris.platform.core.model.order.OrderModel;


/**
 * Service for putting {@link OrderModel} on hold.
 */
public interface OrderOnHoldService
{
	/**
	 * Puts the provided {@link OrderModel} on hold. Meaning all of its consignments are cancelled as well as the associated
	 * task assignment workflows.
	 *
	 * @param order
	 * 		the {@link OrderModel} to be put on hold
	 */
	void processOrderOnHold(OrderModel order);
}
