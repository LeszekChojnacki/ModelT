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
package de.hybris.platform.customersupport.impl;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customersupport.CommerceCustomerSupportService;


/**
 * The default implementation returns no any data. Customer Support mode is off always.
 */
public class DefaultCommerceCustomerSupportService implements CommerceCustomerSupportService
{

	@Override
	public boolean isCustomerSupportAgentActive()
	{
		return false;
	}

	@Override
	public UserModel getEmulatedCustomer()
	{
		return null;
	}

	@Override
	public UserModel getAgent()
	{
		return null;
	}
}
