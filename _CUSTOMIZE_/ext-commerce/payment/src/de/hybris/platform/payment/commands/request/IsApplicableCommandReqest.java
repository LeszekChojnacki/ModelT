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
package de.hybris.platform.payment.commands.request;


import de.hybris.platform.payment.commands.IsApplicableCommand;
import de.hybris.platform.payment.dto.BasicCardInfo;


/**
 * request for {@link IsApplicableCommand}
 */
public class IsApplicableCommandReqest
{
	private final BasicCardInfo card;
	private final boolean threeD;


	public IsApplicableCommandReqest(final BasicCardInfo card, final boolean threeD)
	{
		this.card = card;
		this.threeD = threeD;
	}

	public BasicCardInfo getCard()
	{
		return card;
	}

	/**
	 * @return the threeD
	 */
	public boolean isThreeD()
	{
		return threeD;
	}

}
