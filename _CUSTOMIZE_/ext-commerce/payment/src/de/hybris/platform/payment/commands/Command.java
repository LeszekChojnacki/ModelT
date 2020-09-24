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
package de.hybris.platform.payment.commands;

public interface Command<R, O>
{
	/**
	 * perform command for a given request R
	 *
	 * @param request
	 *           request to perform
	 * @return outcome O of command
	 */
	O perform(R request);
}
