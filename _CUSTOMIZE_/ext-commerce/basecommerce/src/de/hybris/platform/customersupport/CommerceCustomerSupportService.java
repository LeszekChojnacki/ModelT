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
package de.hybris.platform.customersupport;

import de.hybris.platform.core.model.user.UserModel;


/**
 * The interface defines methods to access information about Customer Support (Assisted Service) mode.
 */
public interface CommerceCustomerSupportService
{
	/**
	 * Returns true if Customer Support mode is on (Customer Support Agent is active).
	 * 
	 * @return boolean
	 */
	boolean isCustomerSupportAgentActive();

	/**
	 * Returns Emulated Customer if Customer Support mode is on and any Customer is emulated.
	 * 
	 * @return UserModel
	 */
	UserModel getEmulatedCustomer();

	/**
	 * Returns Agent if Customer Support mode is on.
	 * 
	 * @return UserModel
	 */
	UserModel getAgent();
}
