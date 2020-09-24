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

import static java.util.Collections.emptyList;

import de.hybris.platform.payment.AdapterException;
import de.hybris.platform.payment.commands.IsApplicableCommand;
import de.hybris.platform.payment.commands.factory.CommandFactory;
import de.hybris.platform.payment.commands.factory.CommandFactoryRegistry;
import de.hybris.platform.payment.commands.factory.CommandNotSupportedException;
import de.hybris.platform.payment.commands.request.IsApplicableCommandReqest;
import de.hybris.platform.payment.constants.PaymentConstants;
import de.hybris.platform.payment.dto.BasicCardInfo;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * default implementation of command factory register
 */
public class DefaultCommandFactoryRegistryImpl implements CommandFactoryRegistry, ApplicationContextAware, InitializingBean
{
	private static final Logger LOG = Logger.getLogger(DefaultCommandFactoryRegistryImpl.class.getName());

	private ApplicationContext applicationContext;

	private Collection<CommandFactory> commandFactoryList;

	@Override
	public CommandFactory getFactory(final String paymentProvider)
	{
		for (final CommandFactory commandFactory : commandFactoryList)
		{
			if (commandFactory.getPaymentProvider().equals(paymentProvider))
			{
				return commandFactory;
			}
		}
		throw new AdapterException(PaymentConstants.CARD_CANNOT_BE_SERVED);
	}

	@Override
	public CommandFactory getFactory(final BasicCardInfo card, final boolean threeD)
	{
		for (final CommandFactory commandFactory : commandFactoryList)
		{
			if (isApplicable(card, threeD, commandFactory))
			{
				return commandFactory;
			}
		}

		throw new AdapterException(PaymentConstants.CARD_CANNOT_BE_SERVED);
	}

	private boolean isApplicable(final BasicCardInfo card, final boolean threeD, final CommandFactory commandFactory)
	{
		IsApplicableCommand command;
		try
		{
			command = commandFactory.createCommand(IsApplicableCommand.class);
		}
		catch (final CommandNotSupportedException e)
		{
			LOG.info("command not supported", e);
			return false;
		}
		return command.perform(new IsApplicableCommandReqest(card, threeD)).isApplicable();
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet()
	{
		commandFactoryList = applicationContext.getBeansOfType(CommandFactory.class).values();

		if (commandFactoryList == null || commandFactoryList.isEmpty())
		{
			//PAY-38
			LOG.warn(
					"Missing command factory! At least one command factory bean should be bound to the current spring application context");
			commandFactoryList = emptyList();
		}
	}

	public void setCommandFactoryList(final Collection<CommandFactory> commandFactoryList)
	{
		this.commandFactoryList = commandFactoryList;
	}
}
