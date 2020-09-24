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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents cancel decision that provides information if cancel is allowed by the Order Cancel Service. If cancel is
 * denied, a list of denial reasons is provided.
 *
 * @see OrderCancelService#isCancelPossible(de.hybris.platform.core.model.order.OrderModel,
 *      de.hybris.platform.core.model.security.PrincipalModel, boolean, boolean)
 */
public class CancelDecision implements Serializable
{
	private final boolean allowed;
	private final List<OrderCancelDenialReason> denialReasons;

	/**
	 * @param allowed
	 * @param denialReasons
	 */
	public CancelDecision(final boolean allowed, final List<OrderCancelDenialReason> denialReasons)
	{
		super();
		this.allowed = allowed;
		this.denialReasons = Collections.unmodifiableList(new ArrayList<OrderCancelDenialReason>(denialReasons));
	}

	/**
	 * @return the allowed
	 */
	public boolean isAllowed()
	{
		return allowed;
	}

	/**
	 * @return the denialReasons
	 */
	public List<OrderCancelDenialReason> getDenialReasons()
	{
		return denialReasons;
	}
}
