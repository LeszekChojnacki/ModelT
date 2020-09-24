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
package de.hybris.platform.payment.commands.factory;

import de.hybris.platform.payment.dto.CardInfo;


/**
 * this exception signals that card is not supported
 *
 * @deprecated since 6.4
 */
@Deprecated
public class CardNotSupported extends Exception // NOSONAR
{
	private CardInfo card; // NOSONAR

	public void setCard(final CardInfo card)
	{
		this.card = card;
	}

	public CardInfo getCard()
	{
		return card;
	}
}
