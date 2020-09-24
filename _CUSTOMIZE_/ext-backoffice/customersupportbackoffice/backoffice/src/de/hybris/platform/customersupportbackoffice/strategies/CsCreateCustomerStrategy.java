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
package de.hybris.platform.customersupportbackoffice.strategies;

import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.customersupportbackoffice.data.CsCreateCustomerForm;


/**
 * Create customer strategy that used for customer support create wizard.
 */
public interface CsCreateCustomerStrategy
{
	void createCustomer(final CsCreateCustomerForm createCustomerForm) throws DuplicateUidException;
}
