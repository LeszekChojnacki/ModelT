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
package de.hybris.platform.ordercancel;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;


/**
 * Strategy that determines if cancel is possible for given conditions.
 */
public interface OrderCancelDenialStrategy
{
	/**
	 * Determines cancel possibility for given conditions.
	 * 
	 * @param configuration
	 * @param order
	 *           Order that is subject to cancel
	 * @param requestor
	 *           Principal that originates the request ("issuer of the request"). It might be different from current
	 *           session user.
	 * @param partialCancel
	 *           if true, the method determines possibility of doing partial cancel. If false, it tests for complete
	 *           cancel.
	 * @param partialEntryCancel
	 *           only valid if partialCancel is true. If true, the method determines possibility of doing partial entry
	 *           cancel (decreasing OrderEntry quantity). If false, the method determines possibility of doing whole
	 *           entry cancel (discarding the whole OrderEntry)
	 * @return Returns an OrderCancelDenialReason object if cancel is denied for given conditions. Returns null, if
	 *         cancel is not denied (i.e. it is allowed.)
	 */
	OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel configuration, OrderModel order,
			PrincipalModel requestor, boolean partialCancel, boolean partialEntryCancel);
}
