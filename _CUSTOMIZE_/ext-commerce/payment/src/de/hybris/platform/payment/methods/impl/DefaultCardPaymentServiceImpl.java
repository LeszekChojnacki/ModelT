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
package de.hybris.platform.payment.methods.impl;

import de.hybris.platform.payment.AdapterException;
import de.hybris.platform.payment.commands.AuthorizationCommand;
import de.hybris.platform.payment.commands.CaptureCommand;
import de.hybris.platform.payment.commands.CreateSubscriptionCommand;
import de.hybris.platform.payment.commands.DeleteSubscriptionCommand;
import de.hybris.platform.payment.commands.EnrollmentCheckCommand;
import de.hybris.platform.payment.commands.FollowOnRefundCommand;
import de.hybris.platform.payment.commands.GetSubscriptionDataCommand;
import de.hybris.platform.payment.commands.PartialCaptureCommand;
import de.hybris.platform.payment.commands.StandaloneRefundCommand;
import de.hybris.platform.payment.commands.SubscriptionAuthorizationCommand;
import de.hybris.platform.payment.commands.UpdateSubscriptionCommand;
import de.hybris.platform.payment.commands.VoidCommand;
import de.hybris.platform.payment.commands.factory.CommandFactory;
import de.hybris.platform.payment.commands.factory.CommandFactoryRegistry;
import de.hybris.platform.payment.commands.factory.CommandNotSupportedException;
import de.hybris.platform.payment.commands.request.AuthorizationRequest;
import de.hybris.platform.payment.commands.request.CaptureRequest;
import de.hybris.platform.payment.commands.request.CreateSubscriptionRequest;
import de.hybris.platform.payment.commands.request.DeleteSubscriptionRequest;
import de.hybris.platform.payment.commands.request.EnrollmentCheckRequest;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.commands.request.PartialCaptureRequest;
import de.hybris.platform.payment.commands.request.StandaloneRefundRequest;
import de.hybris.platform.payment.commands.request.SubscriptionAuthorizationRequest;
import de.hybris.platform.payment.commands.request.SubscriptionDataRequest;
import de.hybris.platform.payment.commands.request.UpdateSubscriptionRequest;
import de.hybris.platform.payment.commands.request.VoidRequest;
import de.hybris.platform.payment.commands.result.AuthorizationResult;
import de.hybris.platform.payment.commands.result.CaptureResult;
import de.hybris.platform.payment.commands.result.EnrollmentCheckResult;
import de.hybris.platform.payment.commands.result.RefundResult;
import de.hybris.platform.payment.commands.result.SubscriptionDataResult;
import de.hybris.platform.payment.commands.result.SubscriptionResult;
import de.hybris.platform.payment.commands.result.VoidResult;
import de.hybris.platform.payment.methods.CardPaymentService;


public class DefaultCardPaymentServiceImpl implements CardPaymentService
{
	private CommandFactoryRegistry commandFactoryRegistry;

	@Override
	public AuthorizationResult authorize(final AuthorizationRequest request)
	{
		try
		{
			final CommandFactory commandFactory = commandFactoryRegistry.getFactory(request.getCard(), false);
			final AuthorizationCommand command = commandFactory.createCommand(AuthorizationCommand.class);
			final AuthorizationResult result = command.perform(request);
			result.setPaymentProvider(commandFactory.getPaymentProvider());

			return result;
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);

		}
	}

	@Override
	public AuthorizationResult authorize(final SubscriptionAuthorizationRequest request)
	{
		try
		{
			CommandFactory commandFactory;
			if (request.getPaymentProvider() == null)
			{
				commandFactory = commandFactoryRegistry.getFactory(null, false);
			}
			else
			{
				commandFactory = commandFactoryRegistry.getFactory(request.getPaymentProvider());
			}
			final SubscriptionAuthorizationCommand command = commandFactory.createCommand(SubscriptionAuthorizationCommand.class);
			final AuthorizationResult result = command.perform(request);
			result.setPaymentProvider(commandFactory.getPaymentProvider());

			return result;
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);

		}
	}

	@Override
	public CaptureResult capture(final CaptureRequest request)
	{

		try
		{
			final CaptureCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(CaptureCommand.class);

			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}

	}

	@Override
	public CaptureResult partialCapture(final PartialCaptureRequest request)
	{
		try
		{
			final PartialCaptureCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(PartialCaptureCommand.class);
			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}

	}

	@Override
	public EnrollmentCheckResult enrollmentCheck(final EnrollmentCheckRequest request)
	{
		final CommandFactory commandFactory = commandFactoryRegistry.getFactory(request.getCard(), true);


		try
		{
			final EnrollmentCheckCommand command = commandFactory.createCommand(EnrollmentCheckCommand.class);
			final EnrollmentCheckResult result = command.perform(request);

			result.setPaymentProvider(commandFactory.getPaymentProvider());

			return result;

		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}

	}

	@Override
	public RefundResult refundFollowOn(final FollowOnRefundRequest request)
	{
		try
		{
			final FollowOnRefundCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(FollowOnRefundCommand.class);

			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}
	}

	@Override
	public RefundResult refundStandalone(final StandaloneRefundRequest request)
	{
		try
		{
			final CommandFactory commandFactory;
			if (request.getPaymentProvider() == null)
			{
				commandFactory = commandFactoryRegistry.getFactory(request.getCard(), false);

			}
			else
			{
				commandFactory = commandFactoryRegistry.getFactory(request.getPaymentProvider());
			}

			final StandaloneRefundCommand command = commandFactory.createCommand(StandaloneRefundCommand.class);
			final RefundResult result = command.perform(request);
			result.setPaymentProvider(commandFactory.getPaymentProvider());
			return result;
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}
	}

	@Override
	public VoidResult voidCreditOrCapture(final VoidRequest request)
	{
		try
		{
			final VoidCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(VoidCommand.class);
			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}
	}

	@Override
	public SubscriptionResult createSubscription(final CreateSubscriptionRequest request)
	{
		try
		{
			final CreateSubscriptionCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(CreateSubscriptionCommand.class);

			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}
	}

	@Override
	public SubscriptionResult updateSubscription(final UpdateSubscriptionRequest request)
	{
		try
		{
			final UpdateSubscriptionCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(UpdateSubscriptionCommand.class);

			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}
	}

	@Override
	public SubscriptionDataResult getSubscriptionData(final SubscriptionDataRequest request)
	{
		try
		{
			final GetSubscriptionDataCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(GetSubscriptionDataCommand.class);

			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}
	}

	@Override
	public SubscriptionResult deleteSubscription(final DeleteSubscriptionRequest request)
	{
		try
		{
			final DeleteSubscriptionCommand command = commandFactoryRegistry.getFactory(request.getPaymentProvider())
					.createCommand(DeleteSubscriptionCommand.class);

			return command.perform(request);
		}
		catch (final CommandNotSupportedException e)
		{
			throw new AdapterException(e.getMessage(), e);
		}
	}
	
	public CommandFactoryRegistry getCommandFactoryRegistry()
	{
		return commandFactoryRegistry;
	}

	/**
	 * @param commandFactoryRegistry
	 * 		the commandFactoryRegistry to set
	 */
	public void setCommandFactoryRegistry(final CommandFactoryRegistry commandFactoryRegistry)
	{
		this.commandFactoryRegistry = commandFactoryRegistry;
	}
}
