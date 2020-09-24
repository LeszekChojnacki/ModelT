/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ticket.resolver;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * This class is responsible for to retrieve the Customer Cart for the code supplied.
 *
 */
public class DefaultOrderResolver implements TicketAssociatedObjectResolver
{
	protected static final String ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE = "Order with guid %s not found for current user in current BaseStore";

	private UserService userService;
	private BaseStoreService baseStoreService;
	private CustomerAccountService customerAccountService;

	@Override
	public AbstractOrderModel getObject(final String code, final String userUid, final String siteUid)
	{

		final BaseStoreModel baseStoreModel = StringUtils.isEmpty(siteUid) ? baseStoreService.getCurrentBaseStore()
				: baseStoreService.getBaseStoreForUid(siteUid);
		try
		{
			return StringUtils.isBlank(userUid) ? customerAccountService.getOrderForCode(
					(CustomerModel) userService.getCurrentUser(), code, baseStoreModel) : customerAccountService.getOrderForCode(
					(CustomerModel) userService.getUserForUID(userUid), code, baseStoreModel);
		}
		catch (final ModelNotFoundException e)
		{
			throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE, code));
		}
	}

	/**
	 * @return the userService
	 */
	protected UserService getUserService()
	{
		return userService;
	}

	/**
	 * @return the baseStoreService
	 */
	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * @return the customerAccountService
	 */
	protected CustomerAccountService getCustomerAccountService()
	{
		return customerAccountService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * @param baseStoreService
	 *           the baseStoreService to set
	 */
	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * @param customerAccountService
	 *           the customerAccountService to set
	 */
	@Required
	public void setCustomerAccountService(final CustomerAccountService customerAccountService)
	{
		this.customerAccountService = customerAccountService;
	}
}
