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
package de.hybris.platform.payment.commands.factory.impl;

import de.hybris.platform.payment.commands.Command;
import de.hybris.platform.payment.commands.factory.CommandFactory;
import de.hybris.platform.payment.commands.factory.CommandNotSupportedException;
import de.hybris.platform.payment.constants.PaymentConstants;

import java.util.Map;


/**
 *
 */
public class DefaultCommandFactoryImpl implements CommandFactory
{
	private Map<Class<Command>, Command> commands;
	private String paymentProvider;

	@Override
	public <T extends Command> T createCommand(final Class<T> commandInterface) throws CommandNotSupportedException
	{
		final Command command = commands.get(commandInterface);

		if (command == null)
		{
			throw new CommandNotSupportedException(PaymentConstants.COMMAND_NOT_IMPL + commandInterface.getCanonicalName());
		}

		return (T) command;
	}

	/**
	 * @param commands
	 *           the commands to set
	 */
	public void setCommands(final Map<Class<Command>, Command> commands)
	{
		this.commands = commands;
	}


	@Override
	public String getPaymentProvider()
	{
		return paymentProvider;
	}

	/**
	 * @param paymentProvider
	 *           the paymentProvider to set
	 */
	public void setPaymentProvider(final String paymentProvider)
	{
		this.paymentProvider = paymentProvider;
	}

}
