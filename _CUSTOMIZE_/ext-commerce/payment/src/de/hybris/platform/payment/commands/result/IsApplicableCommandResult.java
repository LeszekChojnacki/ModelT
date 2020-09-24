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
package de.hybris.platform.payment.commands.result;

import de.hybris.platform.payment.commands.IsApplicableCommand;


/**
 * result of {@link IsApplicableCommand}
 */
public class IsApplicableCommandResult
{
	private final boolean applicable;

	public IsApplicableCommandResult(final boolean applicable)
	{
		this.applicable = applicable;
	}

	public boolean isApplicable()
	{
		return applicable;
	}

}
