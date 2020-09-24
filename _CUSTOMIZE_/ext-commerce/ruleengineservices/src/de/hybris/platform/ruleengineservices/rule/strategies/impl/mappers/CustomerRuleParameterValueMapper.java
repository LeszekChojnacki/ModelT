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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.ModelLoadingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Optional;
import java.util.function.Function;

import static de.hybris.platform.servicelayer.user.UserConstants.ANONYMOUS_CUSTOMER_UID;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


public class CustomerRuleParameterValueMapper implements RuleParameterValueMapper<CustomerModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(CustomerRuleParameterValueMapper.class);

	private UserService userService;

	private ModelService modelService;

	private Function<UserModel, String> userIdentifierProvider;

	@Override
	public String toString(final CustomerModel customer)
	{
		validateParameterNotNull(customer, "Object cannot be null");
		return getUserIdentifierProvider().apply(customer);
	}

	@Override
	public CustomerModel fromString(final String value)
	{
		validateParameterNotNull(value, "String value cannot be null");

		return lookupCustomerByPK(value)
				.orElseGet(() -> lookupCustomerByUID(value)
						.orElseThrow(() -> new RuleParameterValueMapperException("Cannot find customer with the UID: " + value)));
	}

	protected Optional<CustomerModel> lookupCustomerByPK(final String value)
	{
		try
		{
			return Optional.ofNullable(getModelService().get(PK.parse(value)));
		}
		catch (final PK.PKException | ModelLoadingException exc)
		{
			LOG.debug("Exception caught. Return Optional.empty()", exc);
			return Optional.empty();
		}
	}

	protected Optional<CustomerModel> lookupCustomerByUID(final String value)
	{
		try
		{
			return Optional.ofNullable(ANONYMOUS_CUSTOMER_UID.equals(value) ? getUserService().getAnonymousUser()
					: getUserService().getUserForUID(value, CustomerModel.class));
		}
		catch (final UnknownIdentifierException | ClassMismatchException exc)
		{
			LOG.debug("Exception caught. Return Optional.empty()", exc);
			return Optional.empty();
		}
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected Function<UserModel, String> getUserIdentifierProvider()
	{
		return userIdentifierProvider;
	}

	@Required
	public void setUserIdentifierProvider(final Function<UserModel, String> userIdentifierProvider)
	{
		this.userIdentifierProvider = userIdentifierProvider;
	}
}
