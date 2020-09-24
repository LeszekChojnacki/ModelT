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

import de.hybris.platform.payment.AdapterException;
import de.hybris.platform.payment.dto.BasicCardInfo;



/**
 * register of payment providers
 */
public interface CommandFactoryRegistry
{
	/**
	 * Return Factory of command for payment provider that start to serve transaction
	 *
	 * @param paymentProvider
	 *           provider that start to work on transaction
	 * @return the command factory
	 * @throws AdapterException
	 */
	CommandFactory getFactory(String paymentProvider);

	/**
	 * Return Factory of command for payment provider that can serve card.
	 *
	 * @param card
	 *           Card to serve.
	 * @param threeD
	 *           is 3D transaction
	 * @return the command factory
	 * @throws AdapterException
	 */
	CommandFactory getFactory(BasicCardInfo card, boolean threeD);

}
