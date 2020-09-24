/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ticket.resolver;

import de.hybris.platform.core.model.order.AbstractOrderModel;


/**
 * This interface is to make sure to get the relevant objects associated to the customer tickets.
 *
 */
public interface TicketAssociatedObjectResolver
{
	/**
	 *
	 * @param code
	 *           mandatory
	 * @param userUid
	 *           optional
	 * @param siteUid
	 *           optional
	 * @return
	 */
	AbstractOrderModel getObject(final String code, final String userUid, final String siteUid);
}
